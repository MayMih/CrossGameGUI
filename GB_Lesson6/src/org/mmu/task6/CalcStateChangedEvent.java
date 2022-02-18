package org.mmu.task6;

import java.util.EventObject;

/**
 * Класс-аргумент для событий изменения состояния калькулятора
 * */
public class CalcStateChangedEvent extends EventObject
{
    public final boolean isOperationChange;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public CalcStateChangedEvent(Object source)
    {
        super(source);
        isOperationChange = false;
    }
    
    public CalcStateChangedEvent(Object source, boolean _isOperationChange)
    {
        super(source);
        isOperationChange = _isOperationChange;
    }
    
    public CalcStateChangedEvent()
    {
        //TODO: это скорее всего костыль - не знаю, как в таких случаях поступают в Java, т.е. когда событие должен генерировать статический класс?
        this(new Object());
    }
    
    public CalcStateChangedEvent(boolean _isOperationChange)
    {
        super(new Object());
        isOperationChange = _isOperationChange;
    }
}