import java.text.MessageFormat;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * Главный класс игры в "Крестики-нолики" - Задание 3
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
        Unknown("Не задан"),
        /*
         * Тупой - Ходит случайным образом
         * */
        Stupid("Тупой - Ходит случайным образом"),
        /**
         * Низкий - ходит по клеткам соседним со своими
         */
        Low("Низкий - ходит по клеткам соседним со своими");
        /**
         * Ниже Среднего - ходит по клеткам соседним со своими и проверяет, не будет ли следующий ход игрока выигрышным
         * (если да, то пытается препятствовать выигрышу вместо своего хода - не реализовано)
         */
        //BelowNormal,  // ещё не реализовано
        /**
         * *Алгоритм с подсчётом очков для каждой клетки (определение выгодности хода)
         */
        //Normal        // ещё не реализовано
        
        /**
         * Конструктор перечисления - позволяет задать описания для элементов перечисления
         * */
        private AILevel(String _description)
        {
            description = _description;
        }
        
        public final String description;
    }



    //region 'Поля и константы'

    private static final char EMPTY_CELL_SYMBOL = '.';//'□';
    private static final Scanner _scn = new Scanner(System.in);
    private static final Random _rand = new Random();

    private static int _boardSize = 3;
    private static char _playerSymbol = 'X', _cpuSymbol = 'O';
    private static AILevel _aiLevel = AILevel.Unknown;
    private static char[][] _gameBoard;
    /**
     * Счётчик ходов ПК
     *
     * @implNote    является индексом для массива {@link #_cpuTurnsHistory}, поэтому отображает кол-во ходов меньшее на 1
     */
    private static int _cpuTurnsCounter;
    /**
     * Список ходов ПК
     * TODO: тут, конечно, логичнее использовать список, т.к. кол-во ходов заранее неизвестно (но мы пока не знаем о коллекциях),
     *  хотя производительность списков в Java под вопросом, учитывая обязательную упаковку/распаковку...
     * */
    private static int[][] _cpuTurnsHistory;

    //endregion 'Поля и константы'



    /**
     * Точка входа в программу - запуск игры - разбор параметров командной строки (настроек программы)
     */
    public static void main(String[] args)
    {
        if (!parseCommandLine(args))
        {
            printUsageRules();
            System.exit(-1);
        }
        System.out.println("Выбраны следующие параметры:");
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
            //
            //_aiLevel = AILevel.Unknown;
            //
            initBoard(_gameBoard);
            if (_cpuSymbol == 'X')
            {
                cpuTurn();
            }
            else
            {
                printBoard(_gameBoard);
            }
            boolean isPlayerWin = false, isCpuWin = false;
            do
            {
                isPlayerWin = playerTurn(_scn);
                if (isPlayerWin)
                {
                    System.out.printf("Игра окончена - выиграли \"%s\"!%n", _playerSymbol);
                    break;
                }
                isCpuWin = cpuTurn();
                if (isCpuWin)
                {
                    System.out.printf("Игра окончена - выиграли \"%s\"!%n", _cpuSymbol);
                    break;
                }
            }
            while (!noMoreMoves());
            if (!isPlayerWin && !isCpuWin)
            {
                System.out.println("Ничья!");
            }
            _scn.skip(".*\n");
            do
            {
                System.out.append(System.lineSeparator()).println("Хотите повторить? [Yes(Y), No(N)] (Y):");
                userInput = _scn.nextLine().trim();
                wantPlayMore = userInput.equalsIgnoreCase("Y") || userInput.equalsIgnoreCase("Yes") || userInput.isEmpty();
                inCorrectInput = !wantPlayMore && (!userInput.equalsIgnoreCase("N") && !userInput.equalsIgnoreCase("No"));
            }
            while (inCorrectInput);
            if (wantPlayMore && (_cpuTurnsHistory != null))
            {
                _cpuTurnsHistory = null;
                _cpuTurnsCounter = 0;
            }
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
     * Метод проверки доступных клеток в регионе (3 Х 3 относительно указанного начала координат)
     * */
    private static boolean noMoreMovesInRegion(int minX, int minY)
    {
        for (int i = minX; (i < minX + 3) && (i < _boardSize); i++)
        {
            if (i < 0)
            {
                continue;
            }
            for (int j = minY; (j < minY + 3) && (j < _boardSize); j++)
            {
                if (j < 0)
                {
                    continue;
                }
                if (_gameBoard[i][j] == EMPTY_CELL_SYMBOL)
                {
                    return false;
                }
            }
        }
        return true;
    }

    
    /**
     * Метод хода игрока - заполняет клетку введённую с клавиатуры символом игрока (включает вывод доски на консоль)
     *
     * @return - возвращает признак победы
     * */
    private static boolean playerTurn(Scanner scn)
    {
        int x = 0, y = 0;
        do
        {
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
                    System.err.println("Вы должны вводить только целые положительные числа!");
                    scn.skip(".*\n");
                }
            }
            // если числа не присвоены, значит пользователь вводил не цифры
            while (x == 0 || y == 0);
        }
        while (!checkCoords(--x, --y, _gameBoard, true));

        _gameBoard[x][y] = _playerSymbol;
        printBoard(_gameBoard);
        return checkWin(_playerSymbol, x, y);
    }

    /**
     * Метод проверки признака победы - проверяет нет ли такой линии или диагонали, что полностью заполнена указанным
     *  символом
     * @implNote    TODO: возможная оптимизация - передавать координаты последнего хода, тогда можно будет проверять
     *  только одну строку и один столбец (и возможно одну из диагоналей) вместо сканирования всей таблицы
     * */
    private static boolean checkWin(char targetSymbol)
    {
        boolean isWinFound = false;

        // сначала проверяем диагонали (но только, если там есть хотя бы один нужный символ - иначе нет смысла)

        if ((_gameBoard[0][0] == targetSymbol) || (_gameBoard[0][_boardSize - 1] == targetSymbol))
        {
            boolean isMainFault = false, isAuxFault = false;
            // сканируем сразу обе диагонали, пока не наткнёмся на несовпадение в обоих диагоналях
            for (int i = 0; i < _boardSize && (!isAuxFault || !isMainFault); i++)
            {
                if (!isMainFault)
                {
                    isMainFault = (_gameBoard[i][i] != targetSymbol);
                }
                if (!isAuxFault)
                {
                    isAuxFault = _gameBoard[i][_boardSize - 1 - i] != targetSymbol;
                }
            }
            isWinFound = !isMainFault || !isAuxFault;
        }

        // если нет победы по диагоналям, она ещё может быть по строкам или по столбцам

        for (int i = 0; i < _boardSize && !isWinFound; i++)
        {
            boolean isRowFault = false, isColFault = false;
            // проверяем сразу очередные строку и столбец, условие прекращения - несовпадение и по строке, и по столбцу
            for (int j = 0; j < _boardSize && (!isRowFault || !isColFault); j++)
            {
                if (!isRowFault)
                {
                    isRowFault = (_gameBoard[i][j] != targetSymbol);
                }
                if (!isColFault)
                {
                    isColFault = (_gameBoard[j][i] != targetSymbol);
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
    private static boolean checkWin(char targetSymbol, int x, int y)
    {
        boolean isWinFound = false;
        // сначала проверяем диагонали (но только, если там есть хотя бы один нужный символ - иначе нет смысла)
        boolean isMainFault = false, isAuxFault = false;
        if (x == y)
        {
            for (int i = 0; i < _boardSize && !isMainFault; i++)
            {
                isMainFault = (_gameBoard[i][i] != targetSymbol);
            }
            isWinFound = !isMainFault;
        }
        else if (x == _boardSize - 1 - y)
        {
            for (int i = 0; i < _boardSize && !isAuxFault; i++)
            {
                isAuxFault = (_gameBoard[i][_boardSize - 1 - i] != targetSymbol);
            }
            isWinFound = !isAuxFault;
        }
        // если на диагоналях победы нет - сканируем строку и столбец - там ещё может быть победа
        if (!isWinFound)
        {
            boolean isRowFault = false, isColFault = false;
            for (int i = 0; i < _boardSize; i++)
            {
                if (!isRowFault)
                {
                    isRowFault = (_gameBoard[x][i] != targetSymbol);
                }
                if (!isColFault)
                {
                    isColFault = (_gameBoard[i][y] != targetSymbol);
                }
            }
            isWinFound = !isColFault || !isRowFault;
        }
        return isWinFound;
    }

    /**
     * Метод проверки корректности хода
     *
     * @param x - координата от 0 до {@link #_boardSize}
     * @param y - координата от 0 до {@link #_boardSize}
     * @param isUserTurn - true - ход пользователя - на экран будут выводиться сообщения о неправильных координатах
     * */
    private static boolean checkCoords(int x, int y, char[][] gameBoard, boolean isUserTurn)
    {
        if (x < 0 || x >= gameBoard.length || y < 0 || y >= gameBoard.length)
        {
            if (isUserTurn)
            {
                System.err.append("Координаты за пределами доски, числа должны быть от 1 до ").println(gameBoard.length);
            }
            return false;
        }
        else if (gameBoard[x][y] == EMPTY_CELL_SYMBOL)
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
                //TODO: Разобраться - почему-то без {@code String#valueOf} печатается код символа вместо него самого -
                // видимо специфика неявного преобразования символов в строку в Java
                System.out.printf(" %s ", (i < 0 && j < 0) ? " " : (i < 0 ? j + 1 : (j < 0 ? i + 1 : String.valueOf(gameBoard[i][j]))));
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Метод хода ИИ противника (включает вывод доски на консоль)
     *
     * @return  возвращает True, если обнаружена победа ИИ
     *
     * @apiNote создаёт массив {@link #_cpuTurnsHistory}, если он ещё не был создан
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
                int[] res = makeRandomTurn();
                x = res[0];
                y = res[1];
                break;
            }
            case Low:
            {
                int[] res;
                if (_cpuTurnsHistory == null)
                {
                    res = makeRandomTurn();
                    x = res[0];
                    y = res[1];
                    _cpuTurnsHistory = new int[_boardSize * _boardSize][];
                    _cpuTurnsHistory[_cpuTurnsCounter++] = res;
                }
                else
                {
                    boolean isCoordsValid = false;
                    // сначала пытаемся сгенерировать соседнюю точку
                    for (int i = 0; (i < _cpuTurnsCounter + 1) && !isCoordsValid; i++)
                    {
                        // выбираем случайную опорную точку, относительно которой будем пытаться делать ход
                        int[] baseTurnCoords = _cpuTurnsHistory[_rand.nextInt(_cpuTurnsCounter)];
                        x = baseTurnCoords[0];
                        y = baseTurnCoords[1];
                        if (!noMoreMovesInRegion(x - 1, y - 1))
                        {
                            do
                            {
                                // генерируем случайный коэффициент от (-1) до 1 для получения соседних координат
                                x += -1 + _rand.nextInt(3);
                                y += -1 + _rand.nextInt(3);
                                isCoordsValid = checkCoords(x, y, _gameBoard, false);
                                if (isCoordsValid)
                                {
                                    _cpuTurnsHistory[_cpuTurnsCounter++] = new int[] {x, y};
                                }
                                else
                                {
                                    x = baseTurnCoords[0];
                                    y = baseTurnCoords[1];
                                }
                            }
                            while (!isCoordsValid);
                        }
                    }
                    // если же походить в соседнюю клетку не выходит - видимо они заняты, то делаем случайный ход
                    if (!isCoordsValid)
                    {
                        res = makeRandomTurn();
                        x = res[0];
                        y = res[1];
                        _cpuTurnsHistory[_cpuTurnsCounter++] = res;
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
        _gameBoard[x][y] = _cpuSymbol;
        printBoard(_gameBoard);
        return checkWin(_cpuSymbol, x, y);
    }

    /**
     *  Метод генерации случайной пары "подходящих" координат
     *
     * @return  Массив из двух элементов, номер строки и номер столбца
     *
     * @implNote TODO: подумать, возможно логичнее возвращать объект вроде Point, но стандартный Point в Java жёстко завязан на геометрию (AWT)
     * */
    private static int[] makeRandomTurn()
    {
        int x = 0, y = 0;
        do
        {
            x = _rand.nextInt(_boardSize);
            y = _rand.nextInt(_boardSize);
        }
        while (!checkCoords(x, y, _gameBoard, false));
        int[] res = new int[2];
        res[0] = x;
        res[1] = y;
        return res;
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
                    boolean isSizeParamFound = testArg.trim().matches("^/s=\\d+$") || testArg.trim().matches("/size=\\d+");
                    boolean isAiLevelParamFound = testArg.trim().matches("^/a=\\d+$") || testArg.trim().matches("/ai_level=\\d+");
                    int testVal = 0;
                    if (isSizeParamFound || isAiLevelParamFound)
                    {
                        testVal = Integer.parseInt(testArg.substring(testArg.indexOf("=") + 1));
                    }

                    if (isSizeParamFound)
                    {
                        if (testVal < 2)
                        {
                            System.err.println("Размер доски должен быть целым положительным числом больше 2");
                            return false;
                        }
                        else
                        {
                            _boardSize = testVal;
                        }
                    }
                    else if (isAiLevelParamFound)
                    {
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
                            return false;
                        }
                    }
                    else
                    {
                        System.err.format("Неизвестный параметр: \"%s\"%n%n", testArg);
                        return false;
                    }
                }
            }
            catch (NumberFormatException nfex)
            {
                System.err.format("Ошибка разбора параметров запуска программы: \"%s\"%n%n", nfex.toString());
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
        //String aiLevels = Arrays.toString(Arrays.stream(AILevel.values()).skip(1).map(x -> x.ordinal()).toArray());
        System.out.format("Использование: %s {/s|/size}=<размер_поля> {/a|/ai_level}=<%s>%n", CrossGame.class.getSimpleName(), /*aiLevels*/ getAILevels());
        System.out.format("%nНапример: %s /s=3 /a=1 - запустит игру с полем размера 3х3 и минимальным интеллектом противника (рандом)%n",
                CrossGame.class.getSimpleName());
        System.out.println();
        System.out.println("Возможные уровни интеллекта:");
        System.out.println(Arrays.toString(Arrays.stream(AILevel.values()).skip(1).map(x ->
                x.toString() + " (" + x.ordinal() + ")\t - \t" + x.description + System.lineSeparator()).toArray()));
        System.out.println();
        // альтернативная (скоростная?) реализация в виде метода
        //System.out.format("%nВозможные уровни интеллекта:%n%s%n", getAILevels(System.lineSeparator(), true, true));
    }

    /**
     * Метод печати возможных уровней ИИ в виде строки разделённой вертикальными чертами '|'
     * */
    private static String getAILevels()
    {
        return getAILevels('|');
    }

    /**
    * Метод печати числовых значений перечисления в виде строки разделённой указанным символом
    * */
    private static String getAILevels(String separator)
    {
        return getAILevels(separator, false, false);
    }
    /**
     * Метод печати в виде строки разделённой указанным символом
     *
     * @param isIncludeValueNamePrefixes     True - предварять числовые значения названием элемента перечисления
     * @param isIncludeDescriptions         True - Завершать строки описаниями элементов перечисления
     * */
    private static String getAILevels(String separator, boolean isIncludeValueNamePrefixes, boolean isIncludeDescriptions)
    {
        AILevel[] levels = AILevel.values();
        StringBuilder sb = new StringBuilder(levels.length);
        // пропускаем значение {@link #AILevel.Unknown}
        for (int i = 1; i < levels.length; i++)
        {
            AILevel lvl = levels[i];
            if (isIncludeValueNamePrefixes)
            {
                sb.append(MessageFormat.format("{0} ({1})", lvl.toString(), lvl.ordinal()));
            }
            else
            {
                sb.append(lvl.ordinal());
            }
            if (isIncludeDescriptions)
            {
                sb.append("\t - \t").append(lvl.description);
            }
            sb.append(separator);
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
