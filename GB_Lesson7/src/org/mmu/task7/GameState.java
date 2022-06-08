package org.mmu.task7;

import org.mmu.task7.events.AILevelChangedEvent;
import org.mmu.task7.events.BoardSizeChangedEvent;
import org.mmu.task7.events.GameStateChangedEventBase;
import org.mmu.task7.events.PlayerSymbolChangedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;

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
    public static final char DEFAULT_PLAYER_SYMBOL = 'X', DEFAULT_CPU_SYMBOL = 'O';
    public static final int DEFAULT_BOARD_SIZE = 3;
    
    private static final AILevel DEFAULT_AI_LEVEL = AILevel.Low;
    
    public static final GameState Current = new GameState();
    
    private AILevel aiLevel = DEFAULT_AI_LEVEL;
    private int boardSize = DEFAULT_BOARD_SIZE;
    private char playerSymbol = DEFAULT_PLAYER_SYMBOL, cpuSymbol = DEFAULT_CPU_SYMBOL;
    private static final char EMPTY_CELL_SYMBOL = ' ';//'□';
    private char[][] gameBoard = new char[DEFAULT_BOARD_SIZE][DEFAULT_BOARD_SIZE];
    private boolean isStarted = false;
    
    /**
     * Счётчик ходов ПК
     *
     * @implNote    является индексом для массива {@link #_cpuTurnsHistory}, поэтому отображает кол-во ходов меньшее на 1
     */
    private int _cpuTurnsCounter;
    /**
     * Список ходов ПК
     * @implNote  Хотя тут и логичнее использовать список, т.к. кол-во ходов заранее неизвестно, но производительность списков
     *      в Java под вопросом, учитывая обязательную упаковку/распаковку...
     * */
    private ArrayList<Integer> _cpuTurnsHistory = new ArrayList<Integer>(boardSize * boardSize);
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
            _cpuTurnsHistory.ensureCapacity(boardSize * boardSize);
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
    
    public void setPlayerSymbol(char newPlayerSymbol)
    {
        this.playerSymbol = newPlayerSymbol;
        fireGameStateChangedEvent(new PlayerSymbolChangedEvent(this, playerSymbol));
    }
    
    public char getPlayerSymbol()
    {
        return playerSymbol;
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
    
    public synchronized void Reset()
    {
        /*Arrays.stream(_cpuTurnsHistory).forEach(x -> Arrays.fill(x, 0));
        Arrays.fill(_cpuTurnsHistory, 0);*/
        _cpuTurnsHistory.clear();
        isStarted = false;
        initBoard();
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
    public char getValueAt(int cellNumber)
    {
        return getSymbolAt(cellNumber / boardSize, cellNumber % boardSize);
    }
    
    /**
     * Устанавливает указанный символ по указанным координатам
     * @return True, если символ совпадает с одним из известных
     * */
    public boolean setSymbolAt(int rowIndex, int colIndex, char value)
    {
        if (value != DEFAULT_PLAYER_SYMBOL && value != DEFAULT_CPU_SYMBOL && value != EMPTY_CELL_SYMBOL)
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
     * @implNote    сканирует только одну строку и столбец и возможно одну из диагоналей
     * */
    private boolean checkWin(char targetSymbol, int x, int y)
    {
        boolean isWinFound = false;
        // сначала проверяем диагонали (но только, если там есть хотя бы один нужный символ - иначе нет смысла)
        boolean isMainFault = false, isAuxFault = false;
        if (x == y)
        {
            for (int i = 0; i < boardSize && !isMainFault; i++)
            {
                isMainFault = (gameBoard[i][i] != targetSymbol);
            }
            isWinFound = !isMainFault;
        }
        else if (x == boardSize - 1 - y)
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
                    isRowFault = (gameBoard[x][i] != targetSymbol);
                }
                if (!isColFault)
                {
                    isColFault = (gameBoard[i][y] != targetSymbol);
                }
            }
            isWinFound = !isColFault || !isRowFault;
        }
        return isWinFound;
    }
    
    //endregion 'Методы'
    
    
    

}
