package org.mmu.task7.aiengine;

/**
 * TODO: подумать - нужен ли вообще это интерфейс, т.к. по сути это обычный {@link java.util.function.IntSupplier}
 */
public interface AICellNumberGenerator
{
    int generateCellNumber();
}
