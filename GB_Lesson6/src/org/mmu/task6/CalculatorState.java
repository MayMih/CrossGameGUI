package org.mmu.task6;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * Статический класс хранящий состояние Калькулятора
 *
 * @implNote    Попытка реализовать источник класс-событий на Java
 * */
public class CalculatorState
{
    /**
     * Событие изменения состояния калькулятора
     *
     * @apiNote     Генерируется при изменении всех свойств, кроме {@link CalculatorState#lastOperation}
     * */
    public interface CalcStateChangedEventListener extends EventListener
    {
        void handleEvent(CalcStateChangedEvent evt);
    }
    
    private static double operandA;
    private static double operandB;
    
    private static double result;
    private static CalcT6.Operation curOperation = CalcT6.Operation.None, lastOperation = CalcT6.Operation.None;
    private static ArrayList<EventListener> _listeners;
    
    
    
    //region 'Свойства'
    
    /**
     * Возвращает 1-ый операнд - он же аккумулятор результата
     * */
    public static double getOperandA()
    {
        return operandA;
    }
    
    public static void setOperandA(double value)
    {
        operandA = value;
        fireCalcStateChangedEvent(new CalcStateChangedEvent());
    }
    
    /**
     * Возвращает 2-ой операнд
     *
     * @implNote - используется для истории вычислений
     * */
    public static double getOperandB()
    {
        return operandB;
    }
    
    public static void setOperandB(double operandB)
    {
        CalculatorState.operandB = operandB;
        fireCalcStateChangedEvent(new CalcStateChangedEvent());
    }
    
    public static double getResult()
    {
        return result;
    }
    
    public static void setResult(double value)
    {
        CalculatorState.result = value;
        fireCalcStateChangedEvent(new CalcStateChangedEvent());
    }
    
    /**
     * Возвращает текущую мат. операцию, выбраную польз-м
     * */
    public static CalcT6.Operation getCurOperation()
    {
        return curOperation;
    }
    
    public static void setCurOperation(CalcT6.Operation value)
    {
        CalculatorState.curOperation = value;
        fireCalcStateChangedEvent(new CalcStateChangedEvent(true));
    }
    
    public static CalcT6.Operation getLastOperation()
    {
        return lastOperation;
    }
    
    public static void setLastOperation(CalcT6.Operation lastOperation)
    {
        CalculatorState.lastOperation = lastOperation;
        //fireCalcStateChangedEvent();
    }
    
    //endregion 'Свойства'
    
    
    
    //region 'Методы'
    
    public static void clearState()
    {
        operandA = operandB = result = Double.NaN;
        fireCalcStateChangedEvent(new CalcStateChangedEvent());
        curOperation = CalcT6.Operation.None;
        fireCalcStateChangedEvent(new CalcStateChangedEvent(true));
    }
    
    public static synchronized void addStateChangeListener(CalcStateChangedEventListener listener)
    {
        if (_listeners == null)
        {
            _listeners = new ArrayList<EventListener>();
            _listeners.add(listener);
        }
        else if (!_listeners.contains(listener))
        {
            _listeners.add(listener);
        }
    }
    
    public static synchronized void removeStateChangeListener(CalcStateChangedEventListener listener)
    {
        if (_listeners != null)
        {
            _listeners.remove(listener);
        }
    }
    
    private static void fireCalcStateChangedEvent(CalcStateChangedEvent evt)
    {
        for (EventListener al : _listeners)
        {
            if (al instanceof CalcStateChangedEventListener)
            {
                ((CalcStateChangedEventListener)al).handleEvent(evt);
            }
        }
    }
    

    
    //endregion 'Методы'
    
}
