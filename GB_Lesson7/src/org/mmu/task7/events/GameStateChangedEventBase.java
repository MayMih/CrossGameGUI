package org.mmu.task7.events;

import java.util.EventObject;

/**
 * Абстрактный класс-аргумент для событий изменения состояния Игры
 * */
public abstract class GameStateChangedEventBase extends EventObject
{
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     *
     * @throws IllegalArgumentException if source is null.
     */
    public GameStateChangedEventBase(Object source)
    {
        super(source);
    }
    
    public GameStateChangedEventBase()
    {
        //TODO: это скорее всего костыль - не знаю, как в таких случаях поступают в Java, т.е. когда событие должен генерировать статический класс?
        this(new Object());
    }
}