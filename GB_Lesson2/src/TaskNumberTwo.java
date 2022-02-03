import java.text.MessageFormat;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 *   Класс реализующий задание номер 2
 */
public class TaskNumberTwo
{
    public static void main(String[] args)
    {
        // 1. Задать целочисленный массив, состоящий из элементов 0 и 1. С помощью цикла и условия заменить 0 на 1, 1 на 0;

        int[] binaryArray = {0, 1, 1, 0, 1, 0, 0, 0, 1};

        System.out.println("1. Исходный массив:");
        printArray(binaryArray);
        System.out.println();

        for (int i = 0; i < binaryArray.length; i++)
        {
            binaryArray[i] = (binaryArray[i] == 1 ? 0 : 1);
        }

        System.out.println("\tМассив, где единицы (1) заменены на нули (0) и наоборот:");
        printArray(binaryArray);
        System.out.println();

        // 2. Задать пустой целочисленный массив размером 8. С помощью цикла заполнить его значениями 0 3 6 9 12 15 18 21;

        int[] numbersArray = new int[8];
        System.out.println("2. Массив, чисел заполненный в цикле возрастающим рядом целых чисел 0 3 6 и т.д.:");
        for (int i = 0; i < numbersArray.length; i++)
        {
            numbersArray[i] = (i * 3);
        }
        printArray(numbersArray);
        System.out.println();


        // 3. Задать массив [ 1, 5, 3, 2, 11, 4, 5, 2, 4, 8, 9, 1 ], пройти по нему циклом, и числа, меньшие 6, умножить на 2;

        int[] numArray = {1, 5, 3, 2, 11, 4, 5, 2, 4, 8, 9, 1};
        System.out.println("3. Исходный массив:");
        printArray(numArray);
        System.out.println();

        for (int i = 0; i < numArray.length; i++)
        {
            if (numArray[i] < 6)
            {
                numArray[i] *= 2;
            }
        }
        System.out.println("\tМассив после умножения чисел < 6 на 2:");
        printArray(numArray);
        System.out.println();


        // 4. Создать квадратный двумерный целочисленный массив (количество строк и столбцов одинаковое), и с помощью цикла(-ов) заполнить его диагональные элементы единицами;

        int[][] squareArray = new int[5][5];
        System.out.println("4. Квадратный массив заполненный \"1\" по диагоналям:");
        // N.B.: Переменная j здесь избыточна, ведь оона всегда равно i
        for (int i = 0, j = 0; i < squareArray.length; i++, j++)
        {
            squareArray[i][j] = squareArray[i][squareArray.length - j - 1] = 1;
        }
        printSquareArray(squareArray);
        System.out.println();


        // 5. Задать одномерный массив и найти в нем минимальный и максимальный элементы (без помощи интернета);

        int[] randomArray = new int[10];
        Random rand = new Random();
        for (int i = 0; i < randomArray.length; i++)
        {
            randomArray[i] = -50 + rand.nextInt(101);
        }
        System.out.println("5. Одномерный массив для поиска Минимального и Максимального элементов в нём:");
        printArray(randomArray);
        System.out.println();
        // библиотечные методы для самопроверки
        int minValue = Arrays.stream(randomArray).min().getAsInt();
        int maxValue = Arrays.stream(randomArray).max().getAsInt();
        int calculatedMinValue = randomArray[0];
        int calculatedMaxValue = calculatedMinValue;
        for (int item : randomArray)
        {
            if (item < calculatedMinValue)
            {
                calculatedMinValue = item;
            }
            else if (item > calculatedMaxValue)
            {
                calculatedMaxValue = item;
            }
        }
        System.out.format("\tМинимальный элемент(библиотечный метод): %d, Максимальный элемент (библиотечный метод): %d%n\t" +
                "Минимальный элемент(расчётное) %d, Максимальный элемент(расчётное) %d %n%n", minValue, maxValue,
                calculatedMinValue, calculatedMaxValue);


        // 6. Написать метод, в который передается не пустой одномерный целочисленный массив, метод должен вернуть true,
        //      если в массиве есть место, в котором сумма левой и правой части массива равны.

        boolean wantRepeat = false;
        int[] testArray = new int[10];
        String userInput;
        Scanner scn = new Scanner(System.in);
        try
        {
            do
            {
                int i = 0;
                System.out.println("6. Введите через пробел целые числа для поиска точки равновесия (нажмите \"R\", " +
                    System.lineSeparator() + "чтобы использовать случайный массив чисел от (-15) до (15), Enter - закончить ввод):");

                userInput = scn.nextLine().trim();
                if (userInput.equalsIgnoreCase("R"))
                {
                    testArray = rand.ints(testArray.length, -15, 16).toArray();
                }
                else
                {
                    String[] inputElems = userInput.split(" ", testArray.length);
                    for (String str : inputElems)
                    {
                        try
                        {
                            testArray[i++] = Integer.parseInt(str);
                        }
                        catch (NumberFormatException e)
                        {
                            testArray[i++] = 0;
                        }
                    }
                }
                System.out.println("\tБудет использован следующий массив (нечисловые элементы заменены 0):");
                printArray(testArray);
                System.out.println();
                boolean hasBalance = hasBalancePoint(testArray);
                System.out.println(MessageFormat.format("\tДанный массив{0} имеет точк{1} равновесия",
                        hasBalance ? "" : " не", hasBalance ? "у" : "и"));
                boolean inCorrectInput = false;
                do
                {
                    System.out.println("\tХотите повторить? [Yes(Y), No(N)] (Y):");
                    userInput = scn.nextLine().trim();
                    wantRepeat = userInput.equalsIgnoreCase("Y") || userInput.equalsIgnoreCase("Yes") || userInput.isEmpty();
                    inCorrectInput = wantRepeat ? false : (!userInput.equalsIgnoreCase("N") && !userInput.equalsIgnoreCase("No"));
                }
                while (inCorrectInput);
                Arrays.fill(testArray, 0);
            }
            while (wantRepeat);
        }
        catch (Exception ex)
        {
            System.err.println("\tНеожиданная ошибка ввода:");
            ex.printStackTrace();
        }

        // 7. Написать метод, которому на вход подается одномерный массив и число n (может быть положительным или отрицательным),
        //  при этом метод должен сместить все элементы массива на n позиций. Нельзя пользоваться вспомогательными массивами.

        try
        {
            System.out.println("7. Смещение элементов массива на указанное кол-во позиций:");
            userInput = "";
            do
            {
                System.out.println("\tСгенерирован следующий массив:");
                testArray = rand.ints(testArray.length, 0, 101).toArray();
                printArray(testArray);
                System.out.println();
                System.out.println("\tНа сколько позиций Вы хотите его сместить (можно вводить отрицательные числа):");
                int shiftPosCount = 0;
                try
                {
                    shiftPosCount = scn.nextInt();
                    shiftArray(testArray, shiftPosCount);
                    System.out.format("\tРезультат сдвига на (%d) позиций", shiftPosCount).println();
                    printArray(testArray);
                    System.out.println();
                }
                catch (InputMismatchException e)
                {
                    System.err.println("\tВы ввели не целое число!");
                    System.err.println(e.toString());
                }
                finally
                {
                    scn.skip(".*\n");
                }
                boolean inCorrectInput = false;
                do
                {
                    System.out.println("\tХотите повторить? [Yes(Y), No(N)] (Y):");
                    userInput = scn.nextLine().trim();
                    wantRepeat = userInput.equalsIgnoreCase("Y") || userInput.equalsIgnoreCase("Yes") || userInput.isEmpty();
                    inCorrectInput = !wantRepeat && !userInput.equalsIgnoreCase("N") && !userInput.equalsIgnoreCase("No");
                }
                while (inCorrectInput);
            }
            while (wantRepeat);
        }
        catch (Exception ex)
        {
            System.err.println("\tНеожиданная ошибка ввода:");
            ex.printStackTrace();
        }

        scn.close();
    }



    /**
     * 6. Метод проверки массива на наличие точки равновесия сумм членов его левой и правой частей
     * */
    private static boolean hasBalancePoint(int[] testArray)
    {
        int leftSumm = testArray[0], rightSumm = 0;
        boolean hasBalance = false;

        // считаем сумму всех элементов массива начиная со второго, т.к. первый элемент изначально считаем левой суммой
        for (int j = testArray.length - 1; j > 0; j--)
        {
            rightSumm += testArray[j];
        }

        // возвращаем результат, если есть быстрое решение - первый элемент равен сумме всех элементов справа от него
        if (leftSumm == rightSumm)
        {
            return true;
        }

        // пропускаем первый элемент, т.к. мы его уже проверили
        for (int i = 1; i < testArray.length; i++)
        {
            leftSumm += testArray[i];
            rightSumm -= testArray[i];
            if (leftSumm == rightSumm)
            {
                hasBalance = true;
                break;
            }
        }
        return hasBalance;
    }

    /**
     * Метод сдвига массива на указанное число позиций
     *
     * @param shiftPosCount - кол-во позиций для сдвига элементов массива (может быть отрицательным)
     *
     * @implNote - Метод не использует вспомогательные массивы
     * */
    private static void shiftArray(int[] testArray, int shiftPosCount)
    {
        int newValueIndex = Math.abs(shiftPosCount);

        if (newValueIndex >= testArray.length)
        {
            Arrays.fill(testArray, 0);
            return;
        }
        else if (shiftPosCount < 0)
        {
            for (int i = 0; i < testArray.length; i++)
            {
                newValueIndex = i - shiftPosCount;
                testArray[i] = (newValueIndex < testArray.length) ? testArray[newValueIndex] : 0;
            }
        }
        else if (shiftPosCount > 0)
        {
            for (int i = testArray.length - 1; i >= 0; i--)
            {
                newValueIndex = i - shiftPosCount;
                testArray[i] = (newValueIndex >= 0) ? testArray[newValueIndex] : 0;
            }
        }
    }


    /**
     * Метод вывода двумерного массива на консоль
     *
     * @param useTabPrefix - True - использовать символ табуляции перед массивом
     * */
    private static void printSquareArray(int[][] squareArray, boolean useTabPrefix)
    {
        for (int[] row : squareArray)
        {
            printArray(row, useTabPrefix);
        }
    }

    private static void printSquareArray(int[][] squareArray)
    {
        printSquareArray(squareArray, true);
    }


    /**
     * Метод вывода в консоль произвольного массива (с переводом строки в конце)
     *
     * @param useTabPrefix - True - использовать символ табуляции перед массивом
     */
    private static void printArray(int[] numbersArray, boolean useTabPrefix)
    {
        System.out.append(useTabPrefix ? "\t" : "").append(Arrays.toString(numbersArray)).println();
    }

    /**
     * Метод вывода в консоль произвольного массива с отбивкой начала табуляцией (с переводом строки в конце)
     */
    private static void printArray(int[] numbersArray)
    {
        printArray(numbersArray, true);
    }

}
