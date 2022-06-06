package org.mmu.task7.events;

import org.mmu.task7.events.GameStateChangedEventBase;

public class PlayerSymbolChangedEvent extends GameStateChangedEventBase
{
    public final char symbol;
    
    public PlayerSymbolChangedEvent(Object source, char newSymbol)
    {
        super(source);
        symbol = newSymbol;
    }
}
