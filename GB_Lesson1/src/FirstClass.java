import java.util.Scanner;

/**
* 1st Lesson Java program
* */
public class FirstClass
{
    public static void main(String[] args)
    {
        System.out.println("Cheers! This is my \"first\" Java output text!");
        //String a = null;
        try (Scanner scn = new Scanner(System.in/* a */))
        {
            final byte MAX_ATTEMPTS_COUNT = 3;
            final String IDE_FIRST_NAME_PART = "IntelliJ";
            int i = 0;
            boolean isUserAnsweredCorrectly = false;
            do
            {
                System.out.format("Input First name part of this (Idea IDE), symbol register IS important, you have %d attempts",
                        MAX_ATTEMPTS_COUNT - i).println();
                System.out.println("(Type minus sign (-) or word \"exit\" to quit the program)");
                String userInput = scn.nextLine().trim();
                if (userInput.equals(IDE_FIRST_NAME_PART))
                {
                    isUserAnsweredCorrectly = true;
                    System.out.print("Correct! " + (i == 0 ? "First attempt answer - You are awesome!" : (i == 1 ?
                            "Second attempt answer - Good, but may be better!" : "Third attempt answer - It's OK, but next time try harder!")));
                    return;
                }
                else if (userInput.equals("-") || userInput.equals("exit"))
                {
                    return;
                }
                else if (++i < MAX_ATTEMPTS_COUNT)
                {
                    System.err.println("Incorrect! Try again");
                }
            }
            while (i < MAX_ATTEMPTS_COUNT);

            if (!isUserAnsweredCorrectly)
            {
                System.err.format("Unfortunately you gave incorrect answer(s) all %d times. %s The right answer is \"%s\"",
                        MAX_ATTEMPTS_COUNT, System.lineSeparator(), IDE_FIRST_NAME_PART).println();
            }
            System.out.println("Press any key to exit");
            System.in.read();
        }
        catch (Exception ex)
        {
            System.err.format("Неожиданная ошибка сканера консоли:%s%s", ex.toString(), System.lineSeparator());
        }
    }

}
