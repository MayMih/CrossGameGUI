import java.text.MessageFormat;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * Главный класс игры в "Крестики-нолики "
 * @author mayur
 * */
public class CrossGame
{
    /**
     * Возможные уровни Искусственного интеллекта противника
     */
    private enum AILevel
    {
        /**
         * Неизвестный уровень - значение по умолчанию для пустых полей этого типа
         */
        Unknown,
        /*
         * Тупой - Ходит случайным образом
         * */
        Stupid,
        /**
         * Низкий - ходит по клеткам соседним со своими
         */
        Low,
        /**
         * Ниже Среднего - ходит по клеткам соседним со своими и проверяет, не будет ли следующий ход игрока выигрышным
         * (если да, то пытается препятствовать выигрышу вместо своего хода)
         */
        BelowNormal,
        /**
         * *Алгоритм с подсчётом очков для каждой клетки (определение выгодности хода)
         */
        Normal
    }



    //region 'Поля и константы'

    private static final char EMPTY_CELL_SYMBOL = '□';
    private static final Scanner _scn = new Scanner(System.in);
    private static final Random _rand = new Random();

    private static int _boardSize = 3;
    private static char _playerSymbol = 'X', _cpuSymbol = 'O';
    private static AILevel _aiLevel = AILevel.Unknown;
    private static char[][] _gameBoard;

    //endregion 'Поля и константы'



    /**
     * Точка входа в программу - запуск игры - разбор параметров командной строки (настроек программы)
     */
    public static void main(String[] args)
    {
        if (!parseCommandLine(args))
        {
            System.exit(-1);
        }
        System.out.println("Выбраны слежующие параметры:");
        System.out.println(MessageFormat.format("\tРазмер поля: {0}x{0}", _boardSize));
        System.out.printf("\tСложность ИИ: %s (%d)%n%n", _aiLevel, _aiLevel.ordinal());

        boolean inCorrectInput = false;
        char userSymbol = _playerSymbol;
        String userInput;

        // первоначальное создание и заполнение доски
        _gameBoard = new char[_boardSize][_boardSize];
        boolean wantPlayMore = false;
        do
        {
            do
            {
                System.out.println("Выберите сторону (X|O), \"X\" ходит первым и выбран по умолчанию (Enter - подтвердить выбор):");
                userInput = _scn.nextLine();
                userSymbol = userInput.isEmpty() ? 'x' : userInput.trim().toLowerCase().charAt(0);
                inCorrectInput = (userSymbol != 'x') && (userSymbol != 'o') && (userSymbol != '0') &&
                     // проверяем русские буквы                         
                     (userSymbol != 'х') && (userSymbol != 'о');
            }
            while (inCorrectInput);
            _playerSymbol = (userSymbol == 'x') || (userSymbol == 'х') ? 'X' : 'O';
            _cpuSymbol = (_playerSymbol == 'X') ? 'O' : 'X';

            _aiLevel = AILevel.Unknown;
            System.out.println("DEBUG: CPU opponent turned off");

            initBoard(_gameBoard);
            if (_cpuSymbol == 'X')
            {
                cpuTurn();
            }
            printBoard(_gameBoard);
            boolean isPlayerWin, isCpuWin;
            do
            {
                isPlayerWin = playerTurn(_scn);
                printBoard(_gameBoard);
                if (isPlayerWin)
                {
                    System.out.printf("Игра окончена - выиграли \"%s\"!%n", _playerSymbol);
                    break;
                }
                isCpuWin = cpuTurn();
                printBoard(_gameBoard);
                if (isCpuWin)
                {
                    System.out.printf("Игра окончена - выиграли \"%s\"!%n", _cpuSymbol);
                    break;
                }
            }
            while (!noMoreMoves());
            do
            {
                System.out.append(System.lineSeparator()).println("Хотите повторить? [Yes(Y), No(N)] (Y):");
                userInput = _scn.nextLine().trim();
                wantPlayMore = userInput.equalsIgnoreCase("Y") || userInput.equalsIgnoreCase("Yes") || userInput.isEmpty();
                inCorrectInput = !wantPlayMore && (!userInput.equalsIgnoreCase("N") && !userInput.equalsIgnoreCase("No"));
            }
            while (inCorrectInput);
        }
        while (wantPlayMore);
        _scn.close();
        System.exit(0);
    }

    


    //region 'Методы'

    /**
     * Метод ищет пустые клетки, если таковых нет, возвращает True
     * @apiNote - Имеет смысл только, если предварительно было проверено условие победы ({@link #checkWin(char)})
     * */
    private static boolean noMoreMoves()
    {
        for (char[] row : _gameBoard)
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
     * Метод хода игрока - заполняет клетку введённую с клавиатуры символом игрока
     *
     * @return - возвращает признак победы
     * */
    private static boolean playerTurn(Scanner scn)
    {
        int x = 0, y = 0;
        do
        {
            System.out.println("Введите координаты нового хода (номер строки, затем номер столбца, можно через пробел):");
            try
            {
                x = scn.nextInt();
                y = scn.nextInt();
            }
            catch (InputMismatchException ex)
            {
                System.err.print("Вы должны вводить только целые положительные числа!");
                continue;
            }
        }
        while (!isCoordsValid(--x, --y, _gameBoard, EMPTY_CELL_SYMBOL, true));

        _gameBoard[x][y] = _playerSymbol;
        return checkWin(_playerSymbol);
    }

    /**
     * Метод проверки признака победы - проверяет нет ли такой линии или диагонали, что полностью заполнена указанным
     *  символом
     * */
    private static boolean checkWin(char playerSymbol)
    {
        // сначала проверяем диагонали
        boolean mainSymbolChanged = false, auxSymbolChanged = false;
        for (int i = 1; i < _gameBoard.length && !mainSymbolChanged && !auxSymbolChanged; i++)
        {
            if (!mainSymbolChanged)
            {
                mainSymbolChanged = (_gameBoard[i][i] != _gameBoard[i - 1][i - 1]);
            }
            if (!auxSymbolChanged)
            {
                auxSymbolChanged = (_gameBoard[i][_gameBoard.length - i - 1] != _gameBoard[i - 1][_gameBoard.length - i - 2]);
            }
        }
        boolean isWinFound = !mainSymbolChanged || !auxSymbolChanged;
        if (isWinFound)
        {
            return true;
        }
        else
        {
            mainSymbolChanged = auxSymbolChanged = false;
            // проверяем строки и столбцы поочереди 
            for (int i = 0; i < _gameBoard.length; i++)
            {
                for (int j = 1; j < _gameBoard[i].length; j++)
                {
                    // проверяем сразу очередные строку и столбец
                    mainSymbolChanged = (_gameBoard[i][j] != _gameBoard[i][j - 1]);
                    auxSymbolChanged = (_gameBoard[j][i] != _gameBoard[j - 1][i]);
                    if (mainSymbolChanged && auxSymbolChanged)
                    {
                        return false;
                    }
                }
            }
//            if (!mainSymbolChanged)
//            {
//                // проверяем столбцы поочереди
//                for (int i = 0; i < gameBoard.length; i++)
//                {
//                    for (int j = 1; j < gameBoard[i].length; j++)
//                    {
//                        auxSymbolChanged = (gameBoard[j][i] != gameBoard[j - 1][i]);
//                    }
//                }
//            }
        }
        isWinFound = !mainSymbolChanged || !auxSymbolChanged;
        return isWinFound;
    }

    /**
     * Метод проверки корректности хода
     *
     * @param x - координата от 0 до {@link #_boardSize}
     * @param y - координата от 0 до {@link #_boardSize}
     * @param isUserTurn - true - ход пользователя - на экран будут выводиться сообщения о неправильных координатах
     * */
    private static boolean isCoordsValid(int x, int y, char[][] gameBoard, char emptyCellSymbol, boolean isUserTurn)
    {
        if (x < 0 || x >= gameBoard.length || y < 0 || y >= gameBoard.length)
        {
            if (isUserTurn)
            {
                System.err.append("Координаты за пределами доски, числа должны быть от 1 до ").println(gameBoard.length);
            }
            return false;
        }
        else if (gameBoard[x][y] == emptyCellSymbol)
        {
            return true;
        }
        else
        {
            if (isUserTurn)
            {
                System.err.println("Эта клетка уже занята!");
            }
            return false;
        }
    }

    /**
     * Метод печати игровой доски - каждый символ выводится с отступами в один пробел с каждой стороны. Первым рядом
     *  и первым столбцом выводятся номера строк и столбцов соот-но.
     * */
    private static void printBoard(char[][] gameBoard)
    {
        System.out.println();
        for (int i = -1; i < _boardSize; i++)
        {
            for (int j = -1; j < _boardSize; j++)
            {
                System.out.printf(" %s ", (i < 0 && j < 0) ? " " : (i < 0 ? j + 1 : (j < 0 ? i + 1 : String.valueOf(gameBoard[i][j]))));
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Метод хода ИИ противника
     *
     * @return - возвращает True, если обнаружена победа ИИ
     * */
    private static boolean cpuTurn()
    {
        // генерация координат хода ИИ
        int x = 0, y = 0;
        // TODO: можно избавится от switch'a, когда мы узнаем о возможности передачи ф-ий как параметров
        switch (_aiLevel)
        {
            case Stupid:
            {
                do
                {
                    x = _rand.nextInt(_boardSize);
                    y = _rand.nextInt(_boardSize);
                }
                while (!isCoordsValid(x, y, _gameBoard, EMPTY_CELL_SYMBOL, false));
                break;
            }
            case Low:
            {
                break;
            }
            case BelowNormal:
            {
                break;
            }
            default:
            {
                // при неизвестном уровне интеллекта - пропуск хода ("мозг отсутствует")
                return false;
            }
        }
        _gameBoard[x][y] = _cpuSymbol;
        return checkWin(_cpuSymbol);
    }

    /**
     * Метод заполнения квадратного массива указанной размерности, заполоненного пустым символов '□'
     * */
    private static void initBoard(char[][] gameBoard)
    {
        for (char row[] : gameBoard)
        {
            Arrays.fill(row, EMPTY_CELL_SYMBOL);
        }
    }

    /**
     * Метод разбора командной строки
     *
     * @return - False - ошибка разбора - нужно выходить из программы
     * */
    private static boolean parseCommandLine(String[] args)
    {
        if (args.length == 0)
        {
            printUsageRules();
            return false;
        }
        else
        {
            try
            {
                for (String testArg : args)
                {
                    if (testArg.trim().matches("^/s=\\d+$") || testArg.trim().matches("/size=\\d+"))
                    {
                        _boardSize = Integer.parseInt(testArg.substring(testArg.indexOf("=") + 1));
                    }
                    else if (testArg.trim().matches("^/a=\\d+$") || testArg.trim().matches("/ai_level=\\d+"))
                    {
                        int testVal = Integer.parseInt(testArg.substring(testArg.indexOf("=") + 1));
                        for (AILevel enumItem : AILevel.values())
                        {
                            if (testVal > 0 && enumItem.ordinal() == testVal)
                            {
                                _aiLevel = enumItem;
                                break;
                            }
                        }
                        if (_aiLevel == AILevel.Unknown)
                        {
                            System.err.format("Неизвестный уровень ИИ: \"%s\"%n%n", testVal);
                            printUsageRules();
                            return false;
                        }
                    }
                    else
                    {
                        System.err.format("Неизвестный параметр: \"%s\"%n%n", testArg);
                        printUsageRules();
                        return false;
                    }
                }
            }
            catch (NumberFormatException nfex)
            {
                System.err.format("Ошибка разбора параметров запуска программы: \"%s\"%n%n", nfex.toString());
                printUsageRules();
                return false;
            }
            return true;
        }
    }

    /**
     * Метод вывода в консоль правил использования программы (список возможных параметров командной строки)
     * */
    private static void printUsageRules()
    {
        System.out.format("Использование: %s {/s|/size}=<размер_поля> {/a|/ai_level}=<%s>%n", CrossGame.class.getSimpleName(), getAILevels());
        System.out.format("%nНапример: %s /s=3 /a=1 - запустит игру с полем размера 3х3 и минимальным интеллектом противника (рандом)%n",
                CrossGame.class.getSimpleName());
        System.out.format("%nВозможные уровни интеллекта:%n%s%n", getAILevels("\t\t"));
        System.out.println(Arrays.toString(Arrays.stream(AILevel.values()).skip(1).toArray()));
    }

    /**
     * Метод печати возможных уровней ИИ в виде строки разделённой вертикальными чертами '|'
     * */
    private static String getAILevels()
    {
        return getAILevels('|');
    }

    /**
    * Метод печати в виде строки разделённой указанным сиволом
    * */
    private static String getAILevels(String separator)
    {
        AILevel[] levels = AILevel.values();
        StringBuilder sb = new StringBuilder(levels.length);
        for (int i = 1; i < levels.length; i++)
        {
            sb.append(levels[i].ordinal()).append(separator);
        }
        String result = sb.toString();
        return result.substring(0, result.lastIndexOf(separator));
    }
    private static String getAILevels(char separator)
    {
        return getAILevels(Character.toString(separator));
    }

    //endregion 'Методы'
}
