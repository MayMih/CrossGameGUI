package org.mmu.task4;

/**
 * 5. Создать класс Собака с наследованием от класса Животное;
 * */
public class Dog extends Animal implements Swimming
{
    protected final int maxSwimDistance;
    
    public Dog(int _maxRunDistance, double _maxJumpHeight, int _maxSwimDistance)
    {
        // 7. У каждого животного есть ограничения на действия (бег: собака – 500 м., прыжок: собака – 0.5 м.,
        //  плавание: собака – 10 м.)
        super(_maxRunDistance, _maxJumpHeight);
        maxSwimDistance = _maxSwimDistance;
    }
    
    /**
     * Создаёт собаку с параметрами по умолчанию (бег: собака – 500 м., прыжок: собака – 0.5 м., плавание: собака – 10 м.)
     * */
    public Dog() { this(500, 0.5, 10); };
    
    @Override
    public boolean swim(int distance)
    {
        return distance <= maxSwimDistance;
    }
    
    @Override
    public int getMaxSwimDistance()
    {
        return maxSwimDistance;
    }
}
