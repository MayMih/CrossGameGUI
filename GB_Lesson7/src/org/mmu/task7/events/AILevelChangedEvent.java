package org.mmu.task7.events;

import org.mmu.task7.AILevel;

public class AILevelChangedEvent extends GameStateChangedEventBase
{
    public final AILevel aiLevel;
    
    public AILevelChangedEvent(Object source, AILevel newLevel)
    {
        super(source);
        aiLevel = newLevel;
    }
}
