package org.mmu.task4;

/**
 * 5. Создать класс Кот с наследованием от класса Животное;
 * */
public class Cat extends Animal
{
    public Cat(int _maxRunDistance, float _maxJumpHeight)
    {
        // 7. У каждого животного есть ограничения на действия (бег: кот – 200 м., прыжок: кот – 2 м.,
        //  плавание: кот не умеет плавать)
        super(_maxRunDistance, _maxJumpHeight);
    }
    
    /**
     * Создаёт кота с параметрами по умолчанию (бег: кот – 200 м., прыжок: кот – 2 м., плавание: кот не умеет плавать)
     * */
    public Cat() { this(200, 2); };
    
}
