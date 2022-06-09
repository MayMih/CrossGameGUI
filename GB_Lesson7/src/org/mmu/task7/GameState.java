package org.mmu.task7;

import org.mmu.task7.events.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Random;

/**
 * Класс-синглтон текущего состояния игры
 * */
public final class GameState
{
    /**
     * Событие изменения состояния Игры
     * */
    public interface GameStateChangedEventListener extends EventListener
    {
        void handleEvent(GameStateChangedEventBase evt);
    }
    
    
    
    //region 'Поля и константы'
    
    public static final char X_SYMBOL = 'X', ZERO_SYMBOL = 'O';
    public static final int DEFAULT_BOARD_SIZE = 3;
    public static final GameState Current = new GameState();
    
    // ?! Похоже в Java статические поля инициализируется ПОСЛЕ полей экземпляра ?!
    private static final Random _rand = new Random();
    private static final char EMPTY_CELL_SYMBOL = ' ';//'□';
    
    private final AILevel DEFAULT_AI_LEVEL = AILevel.Low;
    
    private AILevel aiLevel = DEFAULT_AI_LEVEL;
    private int boardSize = DEFAULT_BOARD_SIZE;
    private char playerSymbol = X_SYMBOL, cpuSymbol = ZERO_SYMBOL;
    private char[][] gameBoard = new char[DEFAULT_BOARD_SIZE][DEFAULT_BOARD_SIZE];
    private boolean isStarted = false;
    
    /**
     * Список ходов ПК
     * @implNote  Хотя тут и логичнее использовать список (т.к. кол-во ходов заранее неизвестно), но производительность
     *  списков в Java под вопросом, учитывая обязательную упаковку/распаковку...
     * */
    private ArrayList<Integer> cpuTurnsHistory;
   
    private static ArrayList<GameStateChangedEventListener> _listeners;
    
    //endregion 'Поля и константы'
    
    
    
    
    //region 'Свойства'
    
    public int getBoardSize()
    {
        return boardSize;
    }
    
    /**
     * При установке нового размера поля сбрасываем текущее состояние Игры
     * */
    public void setBoardSize(int value)
    {
        if (value != this.boardSize)
        {
            this.boardSize = value;
            gameBoard = new char[this.boardSize][this.boardSize];
            getCpuTurnsHistory().ensureCapacity(boardSize * boardSize);
            Reset();
            fireGameStateChangedEvent(new BoardSizeChangedEvent(this, value));
        }
    }
    
    public AILevel getAiLevel()
    {
        return aiLevel;
    }
    
    public void setAiLevel(AILevel aiLevel)
    {
        this.aiLevel = aiLevel;
        fireGameStateChangedEvent(new AILevelChangedEvent(this, aiLevel));
    }
    
    /**
     * Показывает запущена ли игра в текущий момент - игра считается запущенной, если сделан хотя бы один ход и ходы ещё есть.
     * */
    public boolean isStarted()
    {
        return isStarted && !isNoMoreMoves();
    }
    
    /**
     * После смены символа игрока, также заменяет {@link #cpuSymbol} на "противоположный"
     *
     * @param isXsymbol - True - символ 'X', False - символ 'O'
     * */
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
    
    private synchronized ArrayList<Integer> getCpuTurnsHistory()
    {
        if (cpuTurnsHistory == null)
        {
            cpuTurnsHistory = new ArrayList<>(boardSize * boardSize);
        }
        return cpuTurnsHistory;
    }
    
    public char getCpuSymbol()
    {
        return cpuSymbol;
    }
    
    //endregion 'Свойства'
    
    
    /**
     * Скрытый конструктор - для реализации Синглтона
     * @implNote    TODO: Возможно стоит не ограничивать игру одним экземпляром - тогда не нужно будет постоянно актуализировать
     *  метод {@link GameState#Reset()}? Правда в этом случае придётся заново привязывать обработчик изменения состояния игры
     *  при каждом её сбросе, зато станет возможным хранение нескольких состояний (например для отмены хода или поддержки сохранений).
     * */
    private GameState()
    {
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
        if (_listeners == null)
        {
            _listeners = new ArrayList<GameStateChangedEventListener>();
            _listeners.add(listener);
        }
        else if (!_listeners.contains(listener))
        {
            _listeners.add(listener);
        }
    }
    
    public synchronized void removeStateChangeListener(GameStateChangedEventListener listener)
    {
        if (_listeners != null)
        {
            _listeners.remove(listener);
        }
    }
    
    //endregion 'Методы поддержки событий'
    
    
    
    //region 'Методы'
    
    /**
     * Метод сброса игры - проставляет признак того, что игра началась (см.: {@link #isStarted()})
     * */
    public synchronized void Reset()
    {
        getCpuTurnsHistory().clear();
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
     * @return True, если символ совпадает с одним из известных
     * */
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
     * @return True, если символ совпадает с одним из известных
     * */
    public boolean setSymbolAt(int cellNumber, char value)
    {
        return setSymbolAt(cellNumber / boardSize, cellNumber % boardSize, value);
    }
    
    
    /**
     * Метод заполнения квадратного массива указанной размерности, заполоненного пустым символов '□'
     * */
    private void initBoard()
    {
        for (char row[] : gameBoard)
        {
            Arrays.fill(row, EMPTY_CELL_SYMBOL);
        }
    }
    
    /**
     * Метод ищет пустые клетки, если таковых нет, возвращает True
     * @apiNote - Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin(char)})
     * */
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
     * Метод проверки признака победы - проверяет нет ли такой линии или диагонали, что полностью заполнена указанным
     *  символом
     * @deprecated Неоптимальная устаревшая версия - каждый раз сканирует всю таблицу - используйте {@link #checkWin(char, int, int)}
     * */
    private boolean checkWin(char targetSymbol)
    {
        boolean isWinFound = false;
        
        // сначала проверяем диагонали (но только, если там есть хотя бы один нужный символ - иначе нет смысла)
        
        if ((gameBoard[0][0] == targetSymbol) || (gameBoard[0][boardSize - 1] == targetSymbol))
        {
            boolean isMainFault = false, isAuxFault = false;
            // сканируем сразу обе диагонали, пока не наткнёмся на несовпадение в обоих диагоналях
            for (int i = 0; i < boardSize && (!isAuxFault || !isMainFault); i++)
            {
                if (!isMainFault)
                {
                    isMainFault = (gameBoard[i][i] != targetSymbol);
                }
                if (!isAuxFault)
                {
                    isAuxFault = gameBoard[i][boardSize - 1 - i] != targetSymbol;
                }
            }
            isWinFound = !isMainFault || !isAuxFault;
        }
        
        // если нет победы по диагоналям, она ещё может быть по строкам или по столбцам
        
        for (int i = 0; i < boardSize && !isWinFound; i++)
        {
            boolean isRowFault = false, isColFault = false;
            // проверяем сразу очередные строку и столбец, условие прекращения - несовпадение и по строке, и по столбцу
            for (int j = 0; j < boardSize && (!isRowFault || !isColFault); j++)
            {
                if (!isRowFault)
                {
                    isRowFault = (gameBoard[i][j] != targetSymbol);
                }
                if (!isColFault)
                {
                    isColFault = (gameBoard[j][i] != targetSymbol);
                }
            }
            isWinFound = !isRowFault || !isColFault;
        }
        return isWinFound;
    }
    /**
     * Метод проверки признака победы (оптимизированная версия)
     *
     * @apiNote     Устанавливает {@link #isStarted()}
     * @implNote    сканирует только одну строку и столбец и возможно одну из диагоналей
     * */
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
     * @implNote - Сбрасывает признак запуска игры {@link #isStarted()}, если победил компьютер
     * @return  возвращает True, если обнаружена победа ИИ
     *
     * */
    public boolean makeCpuTurn()
    {
        // генерация координат хода ИИ
        int cellNumber = -1, rowIndex = 0, colIndex = 0;
        switch (getAiLevel())
        {
            case Stupid:
            {
                cellNumber = generateRandomValidCoords();
                break;
            }
            case Low:
            {
                if (getCpuTurnsHistory().isEmpty())
                {
                    cellNumber = generateRandomValidCoords();
                    getCpuTurnsHistory().add(cellNumber);
                }
                else
                {
                    boolean isCoordsValid = false;
                    // сначала пытаемся сгенерировать соседнюю точку
                    for (int i = 0; (i <= getCpuTurnsHistory().size()) && !isCoordsValid; i++)
                    {
                        // выбираем случайную опорную точку, относительно которой будем пытаться делать ход
                        int baseTurnCellNumber = getCpuTurnsHistory().get(_rand.nextInt(getCpuTurnsHistory().size()));
                        rowIndex = baseTurnCellNumber / boardSize;
                        colIndex = baseTurnCellNumber % boardSize;
                        if (!noMoreMovesInRegion(rowIndex - 1, colIndex - 1))
                        {
                            do
                            {
                                // генерируем случайный коэффициент от (-1) до 1 для получения соседних координат
                                rowIndex += -1 + _rand.nextInt(3);
                                colIndex += -1 + _rand.nextInt(3);
                                isCoordsValid = checkCoords(rowIndex, colIndex);
                                if (isCoordsValid)
                                {
                                    cellNumber = convertCoordsToCellNumber(rowIndex, colIndex);
                                    getCpuTurnsHistory().add(cellNumber);
                                }
                                else
                                {
                                    rowIndex = getRowIndexFromCellNumber(baseTurnCellNumber);
                                    colIndex = getColumnIndexFromCellNumber(baseTurnCellNumber);
                                }
                            }
                            while (!isCoordsValid);
                        }
                    }
                    // если же походить в соседнюю клетку не выходит - видимо они заняты, то делаем случайный ход
                    if (!isCoordsValid)
                    {
                        cellNumber = generateRandomValidCoords();
                        getCpuTurnsHistory().add(cellNumber);
                    }
                }
                break;
            }
//            case BelowNormal:
//            {
//                break;
//            }
            case Unknown:
            {
                break;
            }
            default:
            {
                // при неизвестном уровне интеллекта - пропуск хода ("мозг отсутствует")
                System.out.println("DEBUG: CPU opponent turned off");
                return false;
            }
        }
        rowIndex = getRowIndexFromCellNumber(cellNumber);
        colIndex = getColumnIndexFromCellNumber(cellNumber);
        gameBoard[rowIndex][colIndex] = cpuSymbol;
        fireGameStateChangedEvent(new CpuTurnCompletedEvent(this, rowIndex, colIndex, boardSize));
        return checkWin(cpuSymbol, rowIndex, colIndex);
    }
    
    /**
     * Метод хода Игрока
     *
     * @implNote Сбрасывает признак запуска игры {@link #isStarted()}, если победил Игрок
     * @return  Возвращает True, если обнаружена победа Игрока
     *
     * @exception IllegalArgumentException Выбрасывается, если ячейка уже занята
     * @exception IndexOutOfBoundsException Выбрасывается, если ячейка не существует - находится за границами поля
     * */
    private boolean makePlayerTurn(int rowIndex, int colIndex) throws IllegalArgumentException, IndexOutOfBoundsException
    {
        if (gameBoard[rowIndex][colIndex] != EMPTY_CELL_SYMBOL)
        {
            String mes = "Обнаружена попытка сделать недопустимый ход - ячейка [" + rowIndex + "][" + colIndex + "] уже занята!";
            System.err.println(mes);
            throw new IllegalArgumentException(mes);
        }
        else if (rowIndex < 0 || colIndex < 0 || rowIndex >= gameBoard.length || colIndex >= gameBoard.length)
        {
            String mes = "Обнаружена попытка сделать недопустимый ход - ячейка [" + rowIndex + "][" + colIndex + "] не существует!";
            System.err.println(mes);
            throw new IndexOutOfBoundsException(mes);
        }
        gameBoard[rowIndex][colIndex] = playerSymbol;
        isStarted = !checkWin(playerSymbol, rowIndex, colIndex);
        return !isStarted;
    }
    
    /**
     * Метод ищет пустые клетки, если таковых нет, возвращает True
     * @implNote - Сбрасывает признак запуска игры {@link #isStarted()}, если ходов больше нет
     * @apiNote - Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin(char)})
     * */
    public boolean noMoreMoves()
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
        isStarted = false;
        return true;
    }
    
    /**
     *  Метод генерации случайной пары "подходящих" координат
     *
     * @return  Номер ячейки в квадратной таблице
     *
     * @implNote TODO: подумать, возможно логичнее возвращать объект вроде Point, но стандартный Point в Java жёстко завязан на геометрию (AWT)
     * */
    private int generateRandomValidCoords()
    {
        int rowIndex = 0, colIndex = 0;
        do
        {
            rowIndex = _rand.nextInt(boardSize);
            colIndex = _rand.nextInt(boardSize);
        }
        while (!checkCoords(rowIndex, colIndex));
        return convertCoordsToCellNumber(rowIndex, colIndex);
    }
    
    /**
     * Метод проверки корректности хода
     *
     * @param rowIndex - координата от 0 до {@link #boardSize}
     * @param columnIndex - координата от 0 до {@link #boardSize}
     * */
    public boolean checkCoords(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || rowIndex >= gameBoard.length || columnIndex < 0 || columnIndex >= gameBoard.length)
        {
            System.err.append("Координаты за пределами доски, числа должны быть от 1 до ").println(gameBoard.length);
            return false;
        }
        else if (gameBoard[rowIndex][columnIndex] == EMPTY_CELL_SYMBOL)
        {
            return true;
        }
        else
        {
            System.out.println("Эта клетка уже занята - ход невозможен!");
            return false;
        }
    }
    
    int[] convertCellNumberToCoords(int number)
    {
       return new int[] { number / boardSize, number % boardSize };
    }
    
    int convertCoordsToCellNumber(int rowIndex, int columnIndex)
    {
        return rowIndex * boardSize + columnIndex;
    }
    
    int getRowIndexFromCellNumber(int cellNumber)
    {
        return cellNumber / boardSize;
    }
    
    int getColumnIndexFromCellNumber(int cellNumber)
    {
        return cellNumber % boardSize;
    }
    
    /**
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     * */
    private boolean noMoreMovesInRegion(int minX, int minY)
    {
        for (int i = minX; (i < minX + 3) && (i < boardSize); i++)
        {
            if (i < 0)
            {
                continue;
            }
            for (int j = minY; (j < minY + 3) && (j < boardSize); j++)
            {
                if (j < 0)
                {
                    continue;
                }
                if (gameBoard[i][j] == EMPTY_CELL_SYMBOL)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    //endregion 'Методы'
    
    
    

}
