package org.mmu.task5;

import org.mmu.task4.Cat;

import java.util.StringJoiner;

/**
 * 1. Класс кота из прошлого ДЗ расширить функционалом потребления пищи.
 * */
public class CatExtended extends Cat
{
    /**
     * У каждого кота есть аппетит, т.е. количество еды, которое он съедает за один раз;
     * */
    public final int minimumMealQuantity;
    
    /**
     * 5. Каждому коту нужно добавить поле сытость (когда создаем котов, они голодны). Если коту удалось поесть (хватило еды), сытость = true;
     * */
    private boolean isFull = false;
    
    private CatExtended()
    {
        minimumMealQuantity = 0;
    };
    
    public CatExtended(int _minMealQuantity)
    {
        super();
        minimumMealQuantity = _minMealQuantity;
    }
    
    public boolean isFull()
    {
        return isFull;
    }
    
    @Override
    public String toString()
    {
        return new StringJoiner(", ", "(ИД: " + hashCode() + ") [", "]")
                .add("Аппетит: " + minimumMealQuantity)
                .add("Сыт: " + (isFull ? "Да" : "Нет"))
                .toString();
    }
    
    /**
     * 3. Метод из первого пункта ДЗ должен взаимодействовать с миской, т.е., конкретный кот ест из конкретной миски, уменьшая объём еды в ней;
     *
     * @return  Возвращает True, если удалось поесть из указанной миски, False, если кот сыт или в миске недостаточно еды.
     *
     * @implNote    Считаем, что если коту мало еды в тарелке, то он её просто не трогает, то есть не может быть наполовину сыт
     *  (это сделано для упрощения логики программы);
     * */
    public boolean eatFrom(Dish myDish)
    {
        if (isFull)
        {
            return false;
        }
        int remainingMeal = myDish.getMealQuantity() - this.minimumMealQuantity;
        isFull = remainingMeal >= 0;
        if (isFull)
        {
            try
            {
                // N.B. по идее эта проверка для однопоточного кода не нужна - ведь мы только, что убедились, что пищи хватит
                myDish.setMealQuantity(myDish.getMealQuantity() - this.minimumMealQuantity);
            }
            catch (ArithmeticException e)
            {
                isFull = false;
            }
        }
        return isFull;
    }
}
