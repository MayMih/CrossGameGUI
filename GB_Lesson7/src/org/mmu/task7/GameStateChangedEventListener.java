package org.mmu.task7;

import org.mmu.task7.events.GameStateChangedEventBase;

import java.util.EventListener;

/**
 * Событие изменения состояния Игры
 */
public interface GameStateChangedEventListener extends EventListener
{
    void handleEvent(GameStateChangedEventBase evt);
}
