package org.mmu.task4;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * Класс реализующий задание номер 4 ()
 * @author  mayur
 * */
public class TaskNumberFour
{
    private final static Random _rand = new Random();
    
    final static String[] Positions = {"Инженер", "Слесарь", "Директор", "Секретарь", "Тестировщик", "Сисадмин", "Аналитик"};
    final static String[] eMailDomains = {"mail.ru", "google.com", "yandex.ru", "live.ru", "rambler.ru", "hotmail.com"};
    
    /**
     * Массив случайных имён - сгенерирован сервисом <a href="https://ciox.ru/generator-full-name">Генератор ФИО</a>}
     * */
    final static String[] Names = {
            "Борисов Оскар Антонинович",
            "Голубев Эдуард Рудольфович",
            "Герасимов Давид Витальевич",
            "Исаев Зиновий Иосифович",
            "Евдокимов Любомир Павлович",
            "Гурьев Аполлон Кириллович",
            "Миронов Бенедикт Филиппович",
            "Макаров Фрол Юлианович",
            "Яковлев Архип Макарович",
            "Кузнецов Владлен Васильевич"
    };
    
    /**
     * Точка входа в программу
     * */
    public static void main(String[] args)
    {
        Employee test = new Employee("Иванов Иван Иванович", "ИО ИО", "+7 (999) 555 4433", "iii@mail.ru", 51, 998);
        test.println();
        
        // 4. Создать массив из 5 сотрудников:
        
        Employee[] Staff = new Employee[5];
    
        for (int i = 0; i < Staff.length; i++)
        {
            String phoneNumber = String.format("+7 (%03d) %03d %04d", _rand.nextInt(1000), _rand.nextInt(1000),
                    _rand.nextInt(10000));
            String eMail = UUID.randomUUID().toString();
            eMail = eMail.substring(0, Math.min(5 + _rand.nextInt(eMail.length() / 3), eMail.length())) + "@" +
                    eMailDomains[_rand.nextInt(eMailDomains.length)];
            boolean isNameUsed = false;
            do
            {
                String pos =  Names[_rand.nextInt(Names.length)];
                for (Employee emp : Staff)
                {
                     isNameUsed = (emp != null) && emp.FIO.equals(pos);
                     if (isNameUsed)
                     {
                         break;
                     }
                }
            }
            while (isNameUsed);
            
            Staff[i] = new Employee(Names[_rand.nextInt(Names.length)], Positions[_rand.nextInt(Positions.length)],
                    phoneNumber, eMail, 20 + _rand.nextInt(50), 500 +_rand.nextInt(1000));
        }
        
        System.out.println("Список всех сотрудников:");
        System.out.println();
        printlnEmployeesArray(Staff);
        System.out.println();
        System.out.println("Список сотрудников старше 40 лет (через цикл):");
        System.out.println();
        long startTime = System.nanoTime();
        for (Employee emp : Staff)
        {
            if (emp.age > 40)
            {
                emp.print();
                System.out.println();
            }
        }
        System.out.println();
        System.out.printf("Время выполнения %s (нс)%n%n", System.nanoTime() - startTime);
        System.out.println("Список сотрудников старше 40 лет (проверка через Потоки Java 8):");
        System.out.println();
        startTime = System.nanoTime();
        System.out.println(Arrays.toString(Arrays.stream(Staff).filter(x -> x.age > 40).toArray()).
                replace(", ", System.lineSeparator()));
        System.out.printf("%nВремя выполнения %s (нс)%n", System.nanoTime() - startTime);
        System.out.println();
        
        // 5. Создать классы Собака и Кот с наследованием от класса Животное;
        
        Cat cat1 = new Cat();
        Dog dog1 = new Dog();
        
        // 8. При попытке животного выполнить одно из этих действий, оно должно сообщить результат в консоль.
        //  (Например, dog1.run(150); -> результат: run: true);
        
        int testDistance = 1 + _rand.nextInt(400);
        System.out.format("Стандартный кот пытается пробежать %d метров...%n", testDistance);
        System.out.println("Результат: " + cat1.run(testDistance));
        System.out.println();
        double testJumpDistance = Math.random() + 1.5;
        System.out.format("Стандартный кот пытается прыгнуть на %1.1f метров...%n", testJumpDistance);
        System.out.println("Результат: " + cat1.jump(testJumpDistance));
        System.out.println();
        testDistance = 1 + _rand.nextInt(1000);
        System.out.format("Стандартная собака пытается пробежать %d метров...%n", testDistance);
        System.out.println("Результат: " + dog1.run(testDistance));
        System.out.println();
        testJumpDistance = Math.random();
        System.out.format("Стандартная собака пытается прыгнуть на %1.1f метров...%n", testJumpDistance);
        System.out.println("Результат: " + dog1.jump(testJumpDistance));
        System.out.println();
        testDistance = 1 + _rand.nextInt(20);
        System.out.format("Стандартная собака пытается проплыть %d метров...%n", testDistance);
        System.out.println("Результат: " + dog1.swim(testDistance));
        System.out.println();
        
        // 9. * Добавить животным разброс в ограничениях. То есть у одной собаки ограничение на бег может быть 400 м., у другой – 600 м.
        
        final int arrLength = 10;
        System.out.printf("Генерируем %d случайных собак и кошек...%n%n", arrLength);
        Dog[] randomDogs = new Dog[arrLength];
        Cat[] randomCats = new Cat[arrLength];
        for (int i = 0; i < arrLength; i++)
        {
            randomCats[i] = new Cat(_rand.nextInt(400), _rand.nextInt(5));
            randomDogs[i] = new Dog(_rand.nextInt(1000), _rand.nextDouble() + 1.5, 1 + _rand.nextInt(20));
            System.out.printf("Тестируем кошку %d...%n%n", i + 1);
            testAnimal(randomCats[i]);
            System.out.println();
            System.out.printf("Тестируем собаку %d...%n%n", i + 1);
            testAnimal(randomDogs[i]);
            System.out.println();
        }
    }
    
    /**
     * Метод тестирования указанного животного - проверяет животное на случайных данных - выводит результат в консоль.
     * */
    private static void testAnimal(Animal someAnimal)
    {
        int testDistance = 1 + _rand.nextInt(someAnimal.maxRunDistance * 2);
        String animalName = someAnimal instanceof Cat ? "Кот" : (someAnimal instanceof Dog ? "Собака" : "Неизвестное животное");
        System.out.format("%s пытается пробежать %d метров...%n", animalName, testDistance);
        System.out.println("Результат: " + someAnimal.run(testDistance));
        System.out.println();
        double testJumpDistance = someAnimal.maxJumpHeight * 2;
        System.out.format("%s пытается прыгнуть на %1.1f метров...%n", animalName, testJumpDistance);
        System.out.println("Результат: " + someAnimal.jump(testJumpDistance));
        System.out.println();
        if (someAnimal instanceof Swimming)
        {
            Swimming swimmingAnimal = ((Swimming)someAnimal);
            testDistance = 1 + _rand.nextInt(swimmingAnimal.getMaxSwimDistance() * 2);
            System.out.format("%s пытается проплыть %d метров...%n", animalName, testDistance);
            System.out.println("Результат: " + swimmingAnimal.swim(testDistance));
            System.out.println();
        }
    }
    
    /**
     * Выводит список работников - каждого с новой строки
     * */
    private static void printlnEmployeesArray(Employee[] staff)
    {
        System.out.println(Arrays.toString(Arrays.stream(staff).toArray()).replace(", ", System.lineSeparator()));
    }
}
