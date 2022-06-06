package org.mmu.task7.events;

/**
 * Класс-аргумент для событий изменения состояния Игры
 * */
public class BoardSizeChangedEvent extends GameStateChangedEventBase
{
    public final int size;
    
    public BoardSizeChangedEvent(Object source, int newSize)
    {
        super(source);
        size = newSize;
    }
}