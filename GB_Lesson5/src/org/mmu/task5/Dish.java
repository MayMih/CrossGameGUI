package org.mmu.task5;

import java.util.StringJoiner;

/**
 * 2. Кот должен есть из миски.
 *  Создайте такую сущность, которая будет обладать объёмом и едой в ней, а также методами наполнения и получения информации о количестве еды;
 * */
public class Dish
{
    private int mealQuantity;
    
    public final int volume;
    
    
    public Dish(int _mealQuantity, int _volume)
    {
        mealQuantity = _mealQuantity;
        volume = _volume;
    }
    
    
    public int getMealQuantity()
    {
        return mealQuantity;
    }
    
    /**
     * Мутатор кол-ва пищи в миске
     *
     * @implNote    При попытке установить кол-во пищи больше объёма миски кол-во пищи будет установлено равным объёму.
     * @exception   ArithmeticException - выбрасывается при попытке установить отрицательное кол-во пищи.
     * */
    public void setMealQuantity(int value) throws ArithmeticException
    {
        if (value < 0)
        {
            throw new ArithmeticException("Новый объём пищи не может быть отрицательным!");
        }
        mealQuantity = Math.min(value, this.volume);
    }
    
    @Override
    public String toString()
    {
        return new StringJoiner(", ", "(ИД: " + this.hashCode() + ") [", "]")
                .add("Текущее кол-во пищи: " + mealQuantity)
                .add("Объём миски: " + volume)
                .toString();
    }
    
    /**
     * Пытается добавить пищу в тарелку - возвращает True, если пищу добавить удалось (новое значение >= 0)
     * */
    public boolean addMeal(int value)
    {
        try
        {
            this.setMealQuantity(mealQuantity + value);
            return true;
        }
        catch (ArithmeticException ex)
        {
            return false;
        }
    }
}
