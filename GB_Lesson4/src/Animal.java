/*
* 5. Создать классы Собака и Кот с наследованием от класса Животное;
* */
public class Animal
{
    protected final int maxRunDistance;
    protected final double maxJumpHeight;
    
    // 8. При попытке животного выполнить одно из этих действий, оно должно сообщить результат в консоль.
    //  (Например, dog1.run(150); -> результат: run: true);
    public boolean run(int distance)
    {
        return distance <= maxRunDistance;
    }
    
    public boolean jump(double barrierHeight)
    {
        return barrierHeight <= maxJumpHeight;
    }
    
    Animal(int _maxRunDistance, double _maxJumpDistance)
    {
        maxRunDistance = _maxRunDistance;
        maxJumpHeight = _maxJumpDistance;
    }
}

