package org.mmu.task7;

import org.mmu.task7.events.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mmu.task7.MainForm.IS_DEBUG;

/**
 * Класс-синглтон текущего состояния игры
 * */
public final class GameState
{
    public boolean isPlayerMovesFirst()
    {
        return playerSymbol == X_SYMBOL;
    }
    
    //region 'Типы данных'
    
    private interface AICellNumberGenerator
    {
        int generateCellNumber();
    }
    
    /**
     * Событие изменения состояния Игры
     */
    public interface GameStateChangedEventListener extends EventListener
    {
        void handleEvent(GameStateChangedEventBase evt);
    }
    
    /**
     * Вспомогательный класс с методами для преобразования номера строки и столбца в номер ячейки и наоборот
     */
    static final class Utils
    {
        static int[] convertCellNumberToCoords(int number)
        {
            return new int[] { number / Current.boardSize, number % Current.boardSize };
        }
        
        static int convertCoordsToCellNumber(int rowIndex, int columnIndex)
        {
            return rowIndex * Current.boardSize + columnIndex;
        }
        
        static int getRowIndexFromCellNumber(int cellNumber)
        {
            return cellNumber / Current.boardSize;
        }
    
        static int getColumnIndexFromCellNumber(int cellNumber)
        {
            return cellNumber % Current.boardSize;
        }
        
        // прячем конструктор от внешнего кода, т.к. в Java можно создавать
        // экземпляры вложенных статических классов! То, что класс помечен спецификатором static, в данном случае значит
        // лишь то, что его экземпляры могут быть созданы Без создания экземпляра класса-родителя (GameState)
        private Utils() {}
    }
    
    /**
     * Класс "Среднего" ИИ - ходит по соседним клеткам стараясь выиграть, а когда до выигрыша ИИ или Игрока остаётся один ход, то выбирает его.
     */
    private static final class NormalAI implements AICellNumberGenerator
    {
        public static final NormalAI instance = new NormalAI();
        
        private NormalAI() {}
        
        @Override
        public int generateCellNumber()
        {
            return generateCellNumber(true);
        }
        
        /**
         * Метод генерации к-т клетки для текущего хода ПК
         * @param isCheckNearbyCellWinAbility Определяет, будут ли к-ты соседней клетки проверяться на возможность выигрыша,
         *                                    False - будет проверяться только возможность выигрыша текущим ходом ИИ или Игрока.
         */
        private int generateCellNumber(boolean isCheckNearbyCellWinAbility)
        {
            int cellNumber;
            cellNumber = getCpuWinCellNumber();
            if (cellNumber < 0)
            {
                cellNumber = getPlayerWinCellNumber();
            }
            if (cellNumber < 0)
            {
                cellNumber = LowAI.instance.generateNearbyCoords(isCheckNearbyCellWinAbility);
            }
            return cellNumber;
        }
    
        /**
         * Метод поиска выигрышной ячейки
         *
         * @param turnsHistory - История ходов (ПК либо Игрока)
         */
        private int getWinCellNumber(ArrayList<Integer> turnsHistory)
        {
            class WinCellFinder     // локальный класс - обёртка для повторяющейся "лямбды"
            {
                int findWinCell(IntStream ideal, List<Integer> source)
                {
                    return ideal.filter(x -> Current.checkCell(x) && !source.contains(x)).findFirst().orElse(-1);
                }
            }
        
            int result = -1;
            if (turnsHistory == null || turnsHistory.size() <= 1)
            {
                return result;
            }
            // для каждого эл-та из Истории считаем кол-во клеток, не хватающих до победы
            ArrayList<Integer> checkedRows = new ArrayList<>();
            ArrayList<Integer> checkedCols = new ArrayList<>();
            boolean isMainDiagChecked = false;
            boolean isAuxDiagChecked = false;
            WinCellFinder cellFinder = new WinCellFinder();
            final int boardsize = Current.boardSize;
        
            for (Integer cellNumber : turnsHistory)
            {
                int rowNumber = Utils.getRowIndexFromCellNumber(cellNumber);
                if (checkedRows.contains(rowNumber))
                {
                    continue;
                }
                // если до победы, например по строке, не хватает одной клетки и она пуста, то она и есть выигрышная
                List<Integer> sameRowHistoryElements = turnsHistory.stream().filter(x -> Utils.convertCellNumberToCoords(x)[0] == rowNumber).
                        collect(Collectors.toList());
                if (sameRowHistoryElements.size() == boardsize - 1)
                {
                    try (IntStream idealRow = IntStream.iterate(Utils.convertCoordsToCellNumber(rowNumber, 0),
                            x -> x + 1).limit(boardsize))
                    {
                        result = cellFinder.findWinCell(idealRow, sameRowHistoryElements);
                        if (result >= 0)
                        {
                            return result;
                        }
                    }
                }
                int collNumber = Utils.getColumnIndexFromCellNumber(cellNumber);
                if (checkedCols.contains(collNumber))
                {
                    continue;
                }
                List<Integer> sameColHistoryElements = turnsHistory.stream().filter(x -> Utils.convertCellNumberToCoords(x)[1] == collNumber).
                        collect(Collectors.toList());
                if (sameColHistoryElements.size() == boardsize - 1)
                {
                    try (IntStream idealColumn = IntStream.iterate(Utils.convertCoordsToCellNumber(0, collNumber),
                            x -> x + Current.boardSize).limit(Current.boardSize))
                    {
                        result = cellFinder.findWinCell(idealColumn, sameColHistoryElements);
                        if (result >= 0)
                        {
                            return result;
                        }
                    }
                }
                // если это элемент главной диагонали и её ещё не проверяли
                if ((collNumber == rowNumber) && !isMainDiagChecked)
                {
                    // Наблюдение: номера ячеек на главной диагонали отличаются на (N + 1)
                    List<Integer> mainDiagHistoryElements = turnsHistory.stream().filter(x -> Math.abs(x - cellNumber) % (boardsize + 1) == 0).
                            collect(Collectors.toList());
                    if (mainDiagHistoryElements.size() == boardsize - 1)
                    {
                        try (IntStream idealMainDiag = IntStream.iterate(0, x -> x + boardsize + 1).limit(boardsize))
                        {
                            result = cellFinder.findWinCell(idealMainDiag, mainDiagHistoryElements);
                            if (result >= 0)
                            {
                                return result;
                            }
                        }
                    }
                    isMainDiagChecked = true;
                }
                // если это элемент побочной диагонали и её ещё не проверяли
                if (((collNumber + rowNumber) == (boardsize - 1)) && !isAuxDiagChecked)
                {
                    List<Integer> auxDiagHistoryElements = turnsHistory.stream().filter(x ->
                                    Utils.getRowIndexFromCellNumber(x) + Utils.getColumnIndexFromCellNumber(x) == (boardsize - 1)).
                            collect(Collectors.toList());
                    if (auxDiagHistoryElements.size() == boardsize - 1)
                    {
                        // Наблюдение: номера ячеек на побочной диагонали отличаются на (N - 1)
                        try (IntStream idealAuxDiag = IntStream.iterate(boardsize - 1, x -> x + boardsize - 1).limit(boardsize))
                        {
                            result = cellFinder.findWinCell(idealAuxDiag, auxDiagHistoryElements);
                            if (result >= 0)
                            {
                                return result;
                            }
                        }
                    }
                    isAuxDiagChecked = true;
                }
                checkedRows.add(rowNumber);
                checkedCols.add(collNumber);
            }
            return -1;
        }
    
        /**
         * Проверяет, не может ли ПК выиграть своим следующим ходом, если ДА - возвращает номер одной из выигрышных клеток.
         *
         * @return Номер выигрышной клетки или (-1), если таких клеток нет.
         */
        private int getCpuWinCellNumber()
        {
            return getWinCellNumber(Current.getCpuTurnsHistory());
        }
    
        /**
         * Проверяет, не может ли Игрок выиграть своим следующим ходом, если ДА - возвращает номер одной из выигрышных клеток.
         *
         * @return Номер выигрышной клетки или (-1), если таких клеток нет.
         */
        private int getPlayerWinCellNumber()
        {
            return getWinCellNumber(Current.getPlayerTurnsHistory());
        }
    
    }
    
    /**
     * Класс ИИ "Ниже Среднего" - ходит по соседним клеткам Не стараясь выиграть, Но когда до выигрыша ИИ или Игрока остаётся один ход, то выбирает его.
     */
    private static final class BelowNormalAI implements AICellNumberGenerator
    {
        public static final BelowNormalAI instance = new BelowNormalAI();
    
        private BelowNormalAI() {}
        
        @Override
        public int generateCellNumber()
        {
            return NormalAI.instance.generateCellNumber(false);
        }
    }
    
    /**
     * Класс "Низкого" ИИ - ходит по соседним клеткам, не учитывая больше никаких условий.
     */
    private static final class LowAI implements AICellNumberGenerator
    {
        public static final LowAI instance = new LowAI();
    
        private LowAI() {}
        
        @Override
        public int generateCellNumber()
        {
            return generateNearbyCoords(false);
        }
        
        /**
         * Генерирует номер клетки соседней с одной из имеющихся у ПК, если таких нет, то случайные координаты пустой клетки
         * @param isCheckWinAbility Определяет, будут ли сгенерированные к-ты проверяться на их полезность для выигрыша (True)
         */
        private int generateNearbyCoords(boolean isCheckWinAbility)
        {
            if (Current.getCpuTurnsHistory().isEmpty())
            {
                return StupidAI.instance.generateRandomEmptyCellCoords();
            }
            ArrayList<Integer> futureCpuTurnsHistory = null;
            
            if (isCheckWinAbility)
            {
                futureCpuTurnsHistory = new ArrayList<>(Current.getCpuTurnsHistory());
            }
            // сначала пытаемся сгенерировать соседнюю точку
            int resultCell = -1;
            for (Integer baseTurnCellNumber : Current.getCpuTurnsHistory())
            {
                // получаем опорную точку, относительно которой будем пытаться делать ход
                List<Integer> freeCells = Current.getEmptyCellsInRegion(baseTurnCellNumber);
                if (!isCheckWinAbility && !freeCells.isEmpty())
                {
                    return freeCells.get(0);    // если проверка полезности хода не требуется, то возвращаем первую попавшуюся соседнюю клетку
                }
                // ищем наиболее оптимальный ход (который может в дальнейшем привести к победе)
                for (Integer emptyCell : freeCells)
                {
                    resultCell = emptyCell;                     // запоминаем пустую соседнюю клетку-кандидат, т.к. более полезных может и не быть
                    futureCpuTurnsHistory.add(emptyCell);
                    //TODO: метод получения выигрышной клетки будет хорошо работать только для поля 3 х 3, т.к. здесь любая соседняя клетка
                    //  с большой вероятностью будет создавать выигрышную серию - для больших полей нужен другой метод оценки полезности хода!!
                    int winCell = NormalAI.instance.getWinCellNumber(futureCpuTurnsHistory);
                    if (winCell >= 0)
                    {
                        return winCell;     // для игры 3 х 3 возврат несоседней потенциально выигрышной клетки выглядит даже лучше (хитрее), но для
                                            // больших досок нужно возвращать соседнюю, либо случайную клетку из выигрышной линии!
                    }
                    else
                    {
                        futureCpuTurnsHistory.remove(emptyCell);
                    }
                }
            }
            // если же походить в соседнюю клетку не смогли (видимо все они заняты), то делаем случайный ход
            if (resultCell == -1)
            {
                return StupidAI.instance.generateRandomEmptyCellCoords();
            }
            return resultCell;
        }
    }
    
    /**
     * Класс очень "Тупого" ИИ - ходит по случайным пустым клеткам на поле
     */
    private static final class StupidAI implements AICellNumberGenerator
    {
        public static final StupidAI instance = new StupidAI();
    
        private StupidAI() {}
        
        /**
         * Синоним метода {@link #generateRandomEmptyCellCoords()}
         */
        @Override
        public int generateCellNumber()
        {
            return generateRandomEmptyCellCoords();
        }
        
        /**
         * Метод генерации случайной пары "подходящих" координат
         *
         * @return Номер ячейки в квадратной таблице
         */
        private int generateRandomEmptyCellCoords()
        {
            int rowIndex, colIndex;
            do
            {
                rowIndex = _rand.nextInt(Current.boardSize);
                colIndex = _rand.nextInt(Current.boardSize);
            }
            while (!Current.checkCoords(rowIndex, colIndex));
            return Utils.convertCoordsToCellNumber(rowIndex, colIndex);
        }
    }
    
    //endregion 'Типы данных'
    
    
    
    //region 'Поля и константы'
    
    public static final char X_SYMBOL = 'X', ZERO_SYMBOL = 'O', EMPTY_CELL_SYMBOL = ' ';
    public static final int DEFAULT_BOARD_SIZE = 3;
    public static final GameState Current = new GameState();
    
    // ?! Похоже в Java статические поля инициализируется ПОСЛЕ полей экземпляра ?!
    private static final Random _rand = new Random();
    
    private final AILevel DEFAULT_AI_LEVEL = AILevel.Normal;
    private AILevel aiLevel = DEFAULT_AI_LEVEL;
    private char playerSymbol = X_SYMBOL, cpuSymbol = ZERO_SYMBOL;
    private char[][] gameBoard = new char[DEFAULT_BOARD_SIZE][DEFAULT_BOARD_SIZE];
    private boolean isStarted = false;
    private int boardSize = DEFAULT_BOARD_SIZE;
    /**
     * Текущий выбранный движок ИИ
     */
    private AICellNumberGenerator aiEngine;
    
    /**
     * Список ходов ПК
     *
     * @implNote Хотя тут и логичнее использовать список (т.к. кол-во ходов заранее неизвестно), но производительность
     * списков в Java под вопросом, учитывая обязательную упаковку/распаковку...
     */
    private ArrayList<Integer> cpuTurnsHistory, playerTurnsHistory;
    
    private static ArrayList<GameStateChangedEventListener> _listeners;
    
    //endregion 'Поля и константы'
    
    
    
    //region 'Свойства'
    
    /**
     * Возвращает текущий "движок" ИИ реализующий виртуального противника
     */
    private AICellNumberGenerator getAiEngine()
    {
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
     * @param aiLevel   Желаемый уровень ИИ
     * @exception IllegalArgumentException При неизвестном уровне ИИ
     */
    public synchronized void setAiLevel(AILevel aiLevel)
    {
        boolean aiLevelHasChanged = this.aiLevel != aiLevel;
        if (aiLevelHasChanged || aiEngine == null)
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
            if (aiLevelHasChanged)
            {
                this.aiLevel = aiLevel;
                fireGameStateChangedEvent(new AILevelChangedEvent(this, aiLevel));
            }
        }
    }
    
    /**
     * Показывает запущена ли игра в текущий момент - игра считается запущенной, если сделан хотя бы один ход и ходы ещё есть.
     */
    public boolean isStarted()
    {
        return isStarted && !isNoMoreMoves();
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
    
    //endregion 'Свойства'
    
    
    
    /**
     * Скрытый конструктор - для реализации Синглтона
     *
     * @implNote TODO: Возможно стоит не ограничивать игру одним экземпляром - тогда не нужно будет постоянно актуализировать
     * метод {@link GameState#Reset()}? Правда в этом случае придётся заново привязывать обработчик изменения состояния игры
     * при каждом её сбросе, зато станет возможным хранение нескольких состояний (например для отмены хода или поддержки сохранений).
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
        if (_listeners == null)
        {
            _listeners = new ArrayList<>();
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
     */
    public synchronized void Reset()
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
     * Метод заполнения квадратного массива указанной размерности, заполоненного пустым символов '□'
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
     * Метод проверки признака победы - проверяет нет ли такой линии или диагонали, что полностью заполнена указанным
     * символом.
     *
     * @deprecated Неоптимальная устаревшая версия - каждый раз сканирует всю таблицу - по возможности используйте {@link #checkWin(char, int, int)}
     */
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
     * @apiNote Устанавливает {@link #isStarted()}
     * @implNote сканирует только одну строку и столбец и возможно одну из диагоналей
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
     * {@link #noMoreMoves()}), иначе возможно зацикливание.
     * @implNote Сбрасывает признак запуска игры {@link #isStarted()}, т.к. содержит вызов {@link #checkWin(char, int, int)}
     */
    public boolean makeCpuTurn()
    {
        // генерация координат хода ИИ
        int cellNumber, rowIndex, colIndex;
        if (getAiEngine() == null)
        {
            // при неизвестном уровне интеллекта - пропуск хода ("мозг отсутствует")
            System.out.printf("DEBUG: CPU opponent turned off (current AI Level: %s )%n", getAiLevel());
            return false;
        }
        cellNumber = getAiEngine().generateCellNumber();
        // Хотя это действие нужно только для сложных уровней ИИ, но, если Игрок захочет изменить уровень сложности, то
        //  к этому моменту нужно будет иметь историю ходов ПК.
        getCpuTurnsHistory().add(cellNumber);
        rowIndex = Utils.getRowIndexFromCellNumber(cellNumber);
        colIndex = Utils.getColumnIndexFromCellNumber(cellNumber);
        gameBoard[rowIndex][colIndex] = cpuSymbol;
        fireGameStateChangedEvent(new CpuTurnCompletedEvent(this, rowIndex, colIndex, boardSize));
        return checkWin(cpuSymbol, rowIndex, colIndex);
    }
    
    /**
     * Метод ищет пустые клетки, если таковых нет, возвращает True
     *
     * @apiNote TODO: по идее можно считать, что ходов нет не только, когда вообще нет пустых клеток, но и когда
     * дальнейшие ходы не имеют смысла - не могут привести к победе.
     * @implNote Сбрасывает признак запуска игры {@link #isStarted()}, если ходов больше нет.
     * @implSpec Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin(char)}).
     */
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
     * Метод проверки корректности хода
     * @param cellNumber Номер ячейки, которую нужно проверять
     */
    public boolean checkCell(int cellNumber)
    {
        int rowIndex = Utils.getRowIndexFromCellNumber(cellNumber);
        int colIndex = Utils.getColumnIndexFromCellNumber(cellNumber);
        return checkCoords(rowIndex, colIndex);
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
            if (IS_DEBUG)
            {
                System.err.append("Координаты за пределами доски, числа должны быть от 1 до ").println(gameBoard.length);
            }
            return false;
        }
        else if (gameBoard[rowIndex][columnIndex] == EMPTY_CELL_SYMBOL)
        {
            return true;
        }
        else
        {
            if (IS_DEBUG)
            {
                System.out.printf("Эта клетка (%d; %d) уже занята - ход невозможен!%n", rowIndex, columnIndex);
            }
            return false;
        }
    }
    
    /**
     * Метод получения списка пустых клеток указанном в регионе (3 Х 3 относительно указанной клетки)
     * */
    private List<Integer> getEmptyCellsInRegion(int cellNumber)
    {
        return getEmptyCellsInRegion(cellNumber / boardSize, cellNumber % boardSize);
    }
    /**
     * Метод получения списка пустых клеток указанном в регионе (3 Х 3 относительно указанных координат)
     * */
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
                    result.add(Utils.convertCoordsToCellNumber(i, j));
                }
            }
        }
        return result;
    }
    
    /**
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     * */
    private boolean noMoreMovesInRegion(int minRowIndex, int minColIndex)
    {
        return noMoreMovesInRegion(minRowIndex, minColIndex, null);
    }
    /**
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     * @param ignoredCells Список клеток, которые нужно игнорировать
     * */
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
                    if (ignoredCells != null && ignoredCells.contains(Utils.convertCoordsToCellNumber(i, j)))
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
     *  и первым столбцом выводятся номера строк и столбцов соот-но.
     * */
    public void printBoard()
    {
        System.out.println();
        for (int i = -1; i < boardSize; i++)
        {
            for (int j = -1; j < boardSize; j++)
            {
                //TODO: Разобраться - почему-то без {@code String#valueOf} печатается код символа вместо него самого -
                // видимо специфика неявного преобразования символов в строку в Java
                System.out.printf(" %s ", (i < 0 && j < 0) ? " " : (i < 0 ? j + 1 : (j < 0 ? i + 1 : String.valueOf(gameBoard[i][j]))));
            }
            System.out.println();
        }
        System.out.println();
    }
    
    //endregion 'Методы'
    


}
