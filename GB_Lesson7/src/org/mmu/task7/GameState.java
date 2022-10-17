package org.mmu.task7;

import org.mmu.task7.aiengine.*;
import org.mmu.task7.events.*;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mmu.task7.MainForm.IS_DEBUG;

/**
 * Класс-синглтон текущего состояния игры
 */
public final class GameState implements Serializable, AutoCloseable
{
    
    //region 'Поля и константы'
    
    /**
     * Версия объекта - нужно менять при каждом изменении состава полей, иначе могут быть проблемы при десериализации!
     */
    private static final long serialVersionUID = 1L;
    
    private static final ArrayList<GameStateChangedEventListener> _listeners = new ArrayList<>();
    public static final char X_SYMBOL = 'X', ZERO_SYMBOL = 'O', EMPTY_CELL_SYMBOL = ' ';
    public static final int DEFAULT_BOARD_SIZE = 4;
    public static final AILevel DEFAULT_AI_LEVEL = AILevel.AboveNormal;
    public static final Random rand = new Random();
    // порядок выполнения инициализаторов (в отличие от C#), см. здесь: https://intuit.ru/studies/higher_education/3406/courses/64/lecture/1886?page=3
    private static GameState current;
    
    private AILevel aiLevel = DEFAULT_AI_LEVEL;
    private char playerSymbol = X_SYMBOL, cpuSymbol = ZERO_SYMBOL;
    private char[][] gameBoard = new char[DEFAULT_BOARD_SIZE][DEFAULT_BOARD_SIZE];
    private boolean isStarted = false;
    private int boardSize = DEFAULT_BOARD_SIZE;
    /**
     * Текущий выбранный движок ИИ
     */
    private transient AICellNumberGenerator aiEngine;
    /**
     * Списки ходов ПК и Игрока
     */
    private ArrayList<Integer> cpuTurnsHistory, playerTurnsHistory;
    
    //endregion 'Поля и константы'
    
    
    
    
    //region 'Свойства'
    
    /**
     * Возвращает текущий "движок" ИИ реализующий виртуального противника
     */
    private AICellNumberGenerator getAiEngine()
    {
        switch (aiLevel)
        {
            case Stupid:
            {
                aiEngine = StupidAI.instance;
                break;
            }
            case Low:
            {
                aiEngine = LowAI.instance;
                break;
            }
            case BelowNormal:
            {
                aiEngine = BelowNormalAI.instance;
                break;
            }
            case Normal:
            {
                aiEngine = NormalAI.instance;
                break;
            }
            case AboveNormal:
            {
                aiEngine = AboveNormalAI.instance;
                break;
            }
            case Unknown:
            {
                aiEngine = null;
                // при неизвестном уровне интеллекта - пропуск хода ("мозг отсутствует")
                if (IS_DEBUG)
                {
                    System.out.println("DEBUG: CPU opponent has been turned off");
                }
            }
            default:
            {
                aiEngine = null;
                throw new IllegalArgumentException("Неизвестный уровень ИИ - " + aiLevel);
            }
        }
        return aiEngine;
    }
    
    public int getBoardSize()
    {
        return boardSize;
    }
    
    /**
     * При установке нового размера поля сбрасываем текущее состояние Игры
     */
    public void setBoardSize(int value)
    {
        if (value != this.boardSize)
        {
            this.boardSize = value;
            gameBoard = new char[this.boardSize][this.boardSize];
            getCpuTurnsHistory().ensureCapacity(boardSize * boardSize);
            getPlayerTurnsHistory().ensureCapacity(boardSize * boardSize);
            Reset();
            fireGameStateChangedEvent(new BoardSizeChangedEvent(this, value));
        }
    }
    
    /**
     * Возвращает текущий уровень ИИ
     */
    public AILevel getAiLevel()
    {
        return aiLevel;
    }
    
    /**
     * Сеттер уровня ИИ
     *
     * @param aiLevel Желаемый уровень ИИ
     *
     * @exception IllegalArgumentException При неизвестном уровне ИИ
     */
    public synchronized void setAiLevel(AILevel aiLevel)
    {
        if (this.aiLevel != aiLevel)
        {
            this.aiLevel = aiLevel;
            fireGameStateChangedEvent(new AILevelChangedEvent(this, aiLevel));
        }
    }
    
    /**
     * Показывает запущена ли игра в текущий момент - игра считается запущенной, если сделан хотя бы один ход и ходы ещё есть.
     */
    public boolean isStarted()
    {
        return isStarted;
    }
    
    /**
     * Устанавливает признак того, что Игра запущена
     *
     * @apiNote Полезно для случаев, когда нужно отменить ход Игрока
     */
    public void setStarted(boolean started)
    {
        isStarted = started;
    }
    
    /**
     * После смены символа игрока, также заменяет {@link #cpuSymbol} на "противоположный"
     *
     * @param isXsymbol - True - символ 'X', False - символ 'O'
     */
    public void setPlayerSymbol(boolean isXsymbol)
    {
        this.playerSymbol = isXsymbol ? X_SYMBOL : ZERO_SYMBOL;
        this.cpuSymbol = isXsymbol ? ZERO_SYMBOL : X_SYMBOL;
        fireGameStateChangedEvent(new PlayerSymbolChangedEvent(this, playerSymbol));
    }
    
    public char getPlayerSymbol()
    {
        return playerSymbol;
    }
    
    public char getCpuSymbol()
    {
        return cpuSymbol;
    }
    
    public synchronized ArrayList<Integer> getCpuTurnsHistory()
    {
        if (cpuTurnsHistory == null)
        {
            cpuTurnsHistory = new ArrayList<>(boardSize * boardSize);
        }
        return cpuTurnsHistory;
    }
    
    public synchronized ArrayList<Integer> getPlayerTurnsHistory()
    {
        if (playerTurnsHistory == null)
        {
            playerTurnsHistory = new ArrayList<>(boardSize * boardSize);
        }
        return playerTurnsHistory;
    }
    
    public boolean isPlayerMovesFirst()
    {
        return playerSymbol == X_SYMBOL;
    }
    
    //endregion 'Свойства'
    
    
    
    /**
     * Скрытый конструктор - для реализации Синглтона
     *
     * @implNote TODO: Возможно стоит не ограничивать игру одним экземпляром - тогда не нужно будет постоянно актуализировать
     *     метод {@link GameState#Reset()}? Правда в этом случае придётся заново привязывать обработчик изменения состояния игры
     *     при каждом её сбросе.
     */
    private GameState()
    {
        // этот вызов обязателен, т.к. иначе не будет выбран соот-щий движок ИИ
        setAiLevel(DEFAULT_AI_LEVEL);
    }
    
    
    
    
    //region 'Методы поддержки событий'
    
    private void fireGameStateChangedEvent(GameStateChangedEventBase gameStateChangedEvent)
    {
        for (GameStateChangedEventListener el : _listeners)
        {
            el.handleEvent(gameStateChangedEvent);
        }
    }
    
    public synchronized void addStateChangeListener(GameStateChangedEventListener listener)
    {
        if (!_listeners.contains(listener))
        {
            _listeners.add(listener);
        }
    }
    
    public synchronized void removeStateChangeListener(GameStateChangedEventListener listener)
    {
        _listeners.remove(listener);
    }
    
    //endregion 'Методы поддержки событий'
    
    
    
    
    //region 'Методы'
    
    /**
     * Метод получения синглтона состояния Игры
     */
    public static GameState getCurrent()
    {
        return getCurrent("");
    }
    
    /**
     * При закрытии игры очищаем список слушателей событий
     */
    @Override
    public void close() throws Exception
    {
        _listeners.clear();
    }
    
    /**
     * Метод получения синглтона состояния Игры
     *
     * @param serializedStatеFilePath Путь к файлу с сериализованным состоянием игры.
     *
     * @return Экземпляр объекта состояния игры загруженный из файла или текущий экземпляр, если он уже создан!
     *
     * @exception RuntimeException Если загрузка не удалась
     */
    public static GameState getCurrent(String serializedStatеFilePath)
    {
        if (current != null)
        {
            return current;
        }
        if (serializedStatеFilePath == null || serializedStatеFilePath.isEmpty())
        {
            current = new GameState();
        }
        else
        {
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(serializedStatеFilePath))))
            {
                current = (GameState) ois.readObject();
            }
            catch (Exception ex)
            {
                throw new RuntimeException("Ошибка загрузки синглтона класса " + GameState.class.getSimpleName() + " из файла: \n\"" +
                    serializedStatеFilePath + "\"", ex);
            }
        }
        return current;
    }
    
    /**
     * Метод сброса игры - проставляет признак того, что игра началась (см.: {@link #isStarted()})
     */
    public void Reset()
    {
        getCpuTurnsHistory().clear();           // здесь может произойти бесполезное создание списка, если его ещё не было,
        getPlayerTurnsHistory().clear();        // что вполне нормально для малых сложностей ИИ
        initBoard();
        isStarted = true;
    }
    
    /*
     * Возвращает содержимое клетки по её координатам
     * */
    public char getSymbolAt(int rowIndex, int colIndex)
    {
        return gameBoard[rowIndex][colIndex];
    }
    
    /*
     * Возвращает содержимое клетки по её номеру
     * */
    public char getSymbolAt(int cellNumber)
    {
        return gameBoard[cellNumber / boardSize][cellNumber % boardSize];
    }
    
    /**
     * Устанавливает указанный символ по указанным координатам
     *
     * @return True, если символ совпадает с одним из известных ({@link #X_SYMBOL}, {@link #ZERO_SYMBOL}, {@link #EMPTY_CELL_SYMBOL})
     */
    public boolean setSymbolAt(int rowIndex, int colIndex, char value)
    {
        if (value != X_SYMBOL && value != ZERO_SYMBOL && value != EMPTY_CELL_SYMBOL)
        {
            return false;
        }
        gameBoard[rowIndex][colIndex] = value;
        return true;
    }
    
    /**
     * Устанавливает указанный символ в клетку по её номеру
     *
     * @return True, если символ совпадает с одним из известных
     */
    public boolean setSymbolAt(int cellNumber, char value)
    {
        return setSymbolAt(cellNumber / boardSize, cellNumber % boardSize, value);
    }
    
    
    /**
     * Метод заполнения квадратного массива указанной размерности, заполоненного пустым символов {@link #EMPTY_CELL_SYMBOL}
     */
    private void initBoard()
    {
        for (char[] row : gameBoard)
        {
            Arrays.fill(row, EMPTY_CELL_SYMBOL);
        }
    }
    
    /**
     * Метод ищет пустые клетки, если таковых нет, возвращает True
     *
     * @apiNote - Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin(char)})
     */
    private boolean isNoMoreMoves()
    {
        for (char[] row : gameBoard)
        {
            for (char ch : row)
            {
                if (ch == EMPTY_CELL_SYMBOL)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Метод проверки признака победы (оптимизированная версия)
     *
     * @apiNote Устанавливает {@link #isStarted()}
     *
     * @implNote сканирует только одну строку и столбец и возможно одну из диагоналей
     */
    private boolean checkWin(char symbol, int cellNumber)
    {
        return checkWin(symbol, BoardUtils.getRowIndexFromCellNumber(cellNumber), BoardUtils.getColumnIndexFromCellNumber(cellNumber));
    }
    
    /**
     * Метод проверки признака победы (оптимизированная версия)
     *
     * @apiNote Устанавливает {@link #isStarted()}
     *
     * @implNote Сканирует только одну строку и столбец и возможно одну из диагоналей
     */
    public boolean checkWin(char targetSymbol, int rowIndex, int colIndex)
    {
        boolean isWinFound = false;
        // сначала проверяем диагонали (но только, если там есть хотя бы один нужный символ - иначе нет смысла)
        boolean isMainFault = false, isAuxFault = false;
        if (rowIndex == colIndex)
        {
            for (int i = 0; i < boardSize && !isMainFault; i++)
            {
                isMainFault = (gameBoard[i][i] != targetSymbol);
            }
            isWinFound = !isMainFault;
        }
        else if (rowIndex == boardSize - 1 - colIndex)
        {
            for (int i = 0; i < boardSize && !isAuxFault; i++)
            {
                isAuxFault = (gameBoard[i][boardSize - 1 - i] != targetSymbol);
            }
            isWinFound = !isAuxFault;
        }
        // если на диагоналях победы нет - сканируем строку и столбец - там ещё может быть победа
        if (!isWinFound)
        {
            boolean isRowFault = false, isColFault = false;
            for (int i = 0; i < boardSize; i++)
            {
                if (!isRowFault)
                {
                    isRowFault = (gameBoard[rowIndex][i] != targetSymbol);
                }
                if (!isColFault)
                {
                    isColFault = (gameBoard[i][colIndex] != targetSymbol);
                }
            }
            isWinFound = !isColFault || !isRowFault;
        }
        isStarted = !isWinFound;
        return isWinFound;
    }
    
    /**
     * Метод хода ИИ противника
     *
     * @return Возвращает True, если обнаружена победа ИИ
     *
     * @implSpec ОСТОРОЖНО: перед вызовом данного метода нужна обязательная проверка на наличие свободных клеток (см.:
     *     {@link #noMoreWinMoves()}), иначе возможно зацикливание.
     *
     * @implNote Сбрасывает признак запуска игры {@link #isStarted()}, т.к. содержит вызов {@link #checkWin(char, int, int)}
     */
    public boolean makeCpuTurn()
    {
        // генерация координат хода ИИ
        if (getAiEngine() == null)
        {
            // при неизвестном уровне интеллекта - пропуск хода ("мозг отсутствует")
            System.out.printf("DEBUG: CPU opponent turned off (current AI Level: %s )%n", getAiLevel());
            return false;
        }
        int cellNumber = getAiEngine().generateCellNumber();
        // Хотя это действие нужно только для сложных уровней ИИ, но, если Игрок захочет изменить уровень сложности, то
        //  к этому моменту нужно будет иметь историю ходов ПК, кроме того, История нужна для работы функции Отмены хода.
        getCpuTurnsHistory().add(cellNumber);
        setSymbolAt(cellNumber, cpuSymbol);
        if (IS_DEBUG)
        {
            GameState.current.printBoard();
        }
        fireGameStateChangedEvent(new CpuTurnCompletedEvent(this, cellNumber, boardSize));
        return checkWin(cpuSymbol, cellNumber);
    }
    
    /**
     * Метод ищет пустые клетки (и линии не заполненные сразу двумя игроками), если таковых нет, возвращает True
     *
     * @implNote Сбрасывает признак запуска игры {@link #isStarted()}, если ходов больше нет.
     *
     * @implSpec Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin}).
     */
    public boolean noMoreWinMoves()
    {
        // вспомогательный класс - накапливает текущее состояние проверяемой линии
        class LineChecker
        {
            private boolean hasPlayerCell = false, hasCpuCell = false, hasEmptyCell = false;
            
            private boolean hasNoDifferentCells()
            {
                return !(hasPlayerCell && hasCpuCell);
            }
            
            /**
             * Добавляет ячейку к списку проверки
             * @param ch Проверяемая ячейка
             * @return True - всё в порядке - проверка линии может продолжаться, False - на линии обнаружены ячейки разных игроков
             */
            private boolean appendCell(char ch)
            {
                if (ch == EMPTY_CELL_SYMBOL)
                {
                    hasEmptyCell = true;
                }
                else if (ch == playerSymbol)
                {
                    hasPlayerCell = true;
                }
                if (ch == cpuSymbol)
                {
                    hasCpuCell = true;
                }
                return hasNoDifferentCells();
            }
            
            /**
             * Сбрасывает накопленное состояние объекта (знания о проверяемой линии)
             */
            public void Reset()
            {
                hasPlayerCell = hasCpuCell = hasEmptyCell = false;
            }
        }
        
        final LineChecker checker = new LineChecker();
        // проверяем строки
        for (char[] row : gameBoard)
        {
            for (char ch : row)
            {
                if (!checker.appendCell(ch))
                {
                    break;
                }
            }
            if (checker.hasNoDifferentCells() && checker.hasEmptyCell)
            {
                return false;
            }
            checker.Reset();
        }
        
        // проверяем столбцы и диагонали
        final LineChecker auxChecker = new LineChecker();
        final LineChecker mainChecker = new LineChecker();
        
        for (int i = 0; i < boardSize; i++)
        {
            checker.Reset();
            if (mainChecker.hasNoDifferentCells())
            {
                mainChecker.appendCell(gameBoard[i][i]);
            }
            if (auxChecker.hasNoDifferentCells())
            {
                auxChecker.appendCell(gameBoard[i][boardSize - i - 1]);
            }
            for (int j = 0; j < boardSize; j++)
            {
                if (!checker.appendCell(gameBoard[j][i]))
                {
                    break;
                }
            }
            if (checker.hasNoDifferentCells() && checker.hasEmptyCell)
            {
                return false;
            }
        }
        if (mainChecker.hasNoDifferentCells() && mainChecker.hasEmptyCell)
        {
            return false;
        }
        if (auxChecker.hasNoDifferentCells() && auxChecker.hasEmptyCell)
        {
            return false;
        }
        this.isStarted = false;
        return true;
    }
    
    /**
     * Метод проверки корректности хода
     *
     * @param cellNumber Номер ячейки, которую нужно проверять
     *
     * @return True - ячейка пустая
     */
    public boolean checkCell(int cellNumber)
    {
        int rowIndex = BoardUtils.getRowIndexFromCellNumber(cellNumber);
        int colIndex = BoardUtils.getColumnIndexFromCellNumber(cellNumber);
        return checkCoords(rowIndex, colIndex);
    }
    
    /**
     * Метод проверки корректности хода
     *
     * @param rowIndex    - координата от 0 до {@link #boardSize}
     * @param columnIndex - координата от 0 до {@link #boardSize}
     *
     * @return True - ячейка пустая
     */
    public boolean checkCoords(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || rowIndex >= gameBoard.length || columnIndex < 0 || columnIndex >= gameBoard.length)
        {
            if (IS_DEBUG)
            {
                System.err.append("Координаты за пределами доски, числа должны быть от 1 до ").println(gameBoard.length);
            }
            return false;
        }
        else
        {
            return gameBoard[rowIndex][columnIndex] == EMPTY_CELL_SYMBOL;
        }
    }
    
    /**
     * Метод получения списка пустых клеток в указанном регионе (3 Х 3 относительно указанной клетки)
     */
    public List<Integer> getEmptyCellsInRegion(int cellNumber)
    {
        return getEmptyCellsInRegion(cellNumber / boardSize, cellNumber % boardSize);
    }
    
    /**
     * Метод получения списка пустых клеток в регионе (3 Х 3 относительно указанных координат)
     */
    private List<Integer> getEmptyCellsInRegion(int rowNumber, int colNumber)
    {
        ArrayList<Integer> result = new ArrayList<>();
        int minRowNumber = rowNumber - 1;
        int minColNumber = colNumber - 1;
        for (int i = minRowNumber; (i < minRowNumber + 3) && (i < boardSize); i++)
        {
            if (i < 0)
            {
                continue;
            }
            for (int j = minColNumber; (j < minColNumber + 3) && (j < boardSize); j++)
            {
                if (j < 0)
                {
                    continue;
                }
                if (gameBoard[i][j] == EMPTY_CELL_SYMBOL)
                {
                    result.add(BoardUtils.convertCoordsToCellNumber(i, j));
                }
            }
        }
        return result;
    }
    
    /**
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     */
    private boolean noMoreMovesInRegion(int minRowIndex, int minColIndex)
    {
        return noMoreMovesInRegion(minRowIndex, minColIndex, null);
    }
    
    /**
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     *
     * @param ignoredCells Список клеток, которые нужно игнорировать
     */
    private boolean noMoreMovesInRegion(int minRowIndex, int minColIndex, List<Integer> ignoredCells)
    {
        for (int i = minRowIndex; (i < minRowIndex + 3) && (i < boardSize); i++)
        {
            if (i < 0)
            {
                continue;
            }
            for (int j = minColIndex; (j < minColIndex + 3) && (j < boardSize); j++)
            {
                if (j < 0)
                {
                    continue;
                }
                if (gameBoard[i][j] == EMPTY_CELL_SYMBOL)
                {
                    if (ignoredCells != null && ignoredCells.contains(BoardUtils.convertCoordsToCellNumber(i, j)))
                    {
                        continue;
                    }
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Отладочный метод вывода состояния игровой доски в консоль - каждый символ выводится с отступами в один пробел с каждой стороны. Первым рядом
     * и первым столбцом выводятся номера строк и столбцов соот-но.
     */
    public void printBoard()
    {
        System.out.println();
        for (int i = -1; i < boardSize; i++)
        {
            for (int j = -1; j < boardSize; j++)
            {
                // Внимание: без {@code String#valueOf} печатается код символа вместо него самого -
                // это специфика неявного преобразования символов в строку в Java
                System.out.printf(" %s ", (i < 0 && j < 0) ? " " : (i < 0 ? j + 1 : (j < 0 ? i + 1 : String.valueOf(gameBoard[i][j]))));
            }
            System.out.println();
        }
        System.out.println();
    }
    
    //endregion 'Методы'
    
    
}
