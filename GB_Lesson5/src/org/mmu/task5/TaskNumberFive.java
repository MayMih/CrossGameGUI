package org.mmu.task5;

import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * 6. Создать массив котов и одну тарелку с едой, попросить всех котов покушать из этой тарелки и потом вывести информацию о сытости котов в консоль;
 * */
public class TaskNumberFive
{
    private static final Random _rand = new Random();
    private static final Scanner _scn = new Scanner(System.in);
    
    public static void main(String[] args)
    {
        final int mealQuant = _rand.nextInt(1000);
        // создаём тарелку с произвольным кол-вом пищи в пределах условной 1000 единиц (чтобы числа не были астрономическими)
        //  и задаём её объём в пределах от 1 до 4 начальных порций пищи.
        final Dish singleDish = new Dish(mealQuant, mealQuant + _rand.nextInt(mealQuant * 3));
        System.out.println();
        System.out.println("Создана единственная миска: " + singleDish);
        
        CatExtended[] catsArray = new CatExtended[10];
        System.out.println("=================================================");
        System.out.println();
        System.out.printf("Создан массив из %d котов:", catsArray.length);
        System.out.println();
        for (int i = 0; i < catsArray.length; i++)
        {
            catsArray[i] = new CatExtended(_rand.nextInt(mealQuant));
            System.out.println(catsArray[i]);
        }
        System.out.println("=================================================");
        System.out.println();
        
        for (CatExtended testCat : catsArray)
        {
            // 7. Когда еда в тарелке кончается, нужно оповещать об этом и наполнять её едой.
            while (!testCat.eatFrom(singleDish))
            {
                System.out.printf("Очередной кот (%s) отказался есть из миски (%n %s %n) - недостаточно пищи!%n%n",
                        testCat, singleDish);
                boolean inCorrectInput = false;
                do
                {
                    System.out.println("Сколько еды вы хотите добавить в миску?");
                    try
                    {
                        inCorrectInput = !singleDish.addMeal(_scn.nextInt());
                        if (inCorrectInput)
                        {
                            System.err.println("Не удалось наполнить миску, проверьте ввод!");
                        }
                        else
                        {
                            System.out.println("Миска успешно наполнена - новое кол-во пищи: " + singleDish.getMealQuantity());
                        }
                    }
                    catch (InputMismatchException iex)
                    {
                        inCorrectInput = true;
                        System.err.println("Вы можете ввести только целое положительное число!");
                        _scn.skip(".*\n");
                    }
                }
                while (inCorrectInput);
            }
            System.out.printf("Очередной кот (%s) успешно поел из миски - пищи осталось: %d %n", testCat, singleDish.getMealQuantity());
        }
        
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        _scn.close();
    }
}
