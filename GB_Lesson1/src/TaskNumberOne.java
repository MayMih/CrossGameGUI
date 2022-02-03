import java.text.MessageFormat;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

/**
*   Класс реализующий задание номер 1
*/
public class TaskNumberOne
{
    static final Scanner _scn = new Scanner(System.in);

    /**
     * 1. Создать пустой проект в IntelliJ IDEA и прописать метод <b>main()</b>
     */
    public static void main(String[] args)
    {
        // 2. Создать переменные всех пройденных типов данных, и инициализировать их значения;
        boolean isTruth = true;
        byte b = 0xF;
        char ch = '#';
        short shrt = -32768;
        int i = 19201080;
        // текущее время в мс. с 01.01.1970 - начало "эры UNIX"
        long l = System.currentTimeMillis();
        float f = 3.14f;
        // случайное число от 0.0 до 1.0
        double d = Math.random();

        // дробные числа вводим в "интернациональном" формате - через точку (.)
        _scn.useLocale(Locale.ROOT);

        System.out.println("Пункт №3");
        System.out.print("Результат вычисления выражения 3 * (2 + (5 / 4)) = ");
        System.out.println(calculateExpression(3, 2, 5, 6));

        System.out.println("Пункт №44");
        try
        {
            System.out.println("Введите первое слагаемое:");
            float x = _scn.nextFloat();
            System.out.println("Введите второе слагаемое:");
            float y = _scn.nextFloat();
            String mes = MessageFormat.format("Сумма чисел {0} и {1} {2}принадлежит диапазону от 10 до 20 (включительно)",
                    x, y, testRange(x, y) ? "" : "не ");
            System.out.println(mes);
        }
        catch (InputMismatchException ime)
        {
            System.err.format("Вы должны вводить числа!%n").println(ime.toString());
            _scn.skip(".*\n");  // пропускаем некорректный ввод, иначе он снова будет прочитан при следующем вызове
        }

        System.out.println("Пункт №5");
        System.out.print("Введите целое число: ");
        try
        {
            i = _scn.nextInt();
            printSign(i);

            System.out.println("Пункт №6");
            System.out.print("Введите целое число: ");
            i = _scn.nextInt();
            System.out.format("Число (%d) отрицательное: %b", i, isNegative(i)).println();
        }
        catch (InputMismatchException ime)
        {
            System.err.format("Ошибка: Вы могли ввести только целое число!%n%n");
            ime.printStackTrace();
        }
        finally
        {
            _scn.skip(".*\n");  // пропускаем некорректный ввод, иначе он снова будет прочитан при следующем вызове
        }

        System.out.println("Пункт №7");
        System.out.print("Кого нужно поприветствовать? Введите имя: ");
        String s = _scn.nextLine().trim();
        printGreeting(s);

        System.out.println("Пункт №8");
        printYearLeapStatus();
    }


    /**
     * 3. Метод, вычисляющий выражение a * (b + (c / d))
     */
    static double calculateExpression(double a, double b, double c, double d)
    {
        return a * (b + (c / d));
    }

    /**
     * 4. Метод, принимающий на вход два числа, и проверяющий, что их сумма лежит в пределах от 10 до 20 (включительно)
     *
     * @return True - сумма входит в диапазон (10 < x <= 20), иначе False
     */
    static boolean testRange(float first, float second)
    {
        double sum = (first + second);
        return (sum > 10) && (sum <= 20) ? true : false;
    }

    /**
     * 5. Метод, которому в качестве параметра передается целое число, печатает в консоль слово "положительное", если
     * ему передали такое число (или ноль), иначе печатает слово "отрицательное"
     */
    static void printSign(int number)
    {
        System.out.format("Число (%d) " + (number >= 0 ? "положительное" : "отрицательное"), number).println();
    }

    /**
     * 6. Метод, которому в качестве параметра передается целое число, метод должен вернуть true, если число отрицательное;
     */
    static boolean isNegative(int number)
    {
        return number < 0;
    }

    /**
     * 7. Метод выводит на консоль сообщение «Привет, указанное_имя!»
     *
     * @param personName - строка "указанное_имя"
     */
    static void printGreeting(String personName)
    {
        System.out.format("Привет, %s!", personName).println();
    }

    /**
     * 8. Метод, который определяет, является ли год (введённый с консоли) високосным, и выводит сообщение в консоль
     */
    static void printYearLeapStatus()
    {
        System.out.print("Введите год: ");
        try
        {
            int year = _scn.nextInt();
            if (year <= 0)
            {
                System.out.println("Год должен быть целым числом больше нуля! Проверьте ввод!!");
                return;
            }
            boolean isLeap = ((year % 400) == 0) || (((year % 4) == 0) && ((year % 100) != 0));
            System.out.format("Год \"%d\" %sявляется високосным%n", year, (!isLeap ? "не " : ""));
        }
        catch (InputMismatchException ime)
        {
            System.err.println("Похоже Вы ввели не число:" + System.lineSeparator() + ime.toString());
            _scn.skip(".*\n");  // пропускаем некорректный ввод, иначе он снова будет прочитан при следующем вызове
        }
    }
}
