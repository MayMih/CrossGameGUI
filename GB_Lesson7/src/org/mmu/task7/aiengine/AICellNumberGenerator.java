package org.mmu.task7.aiengine;

import java.util.function.IntSupplier;

/**
 * Обёртка вокруг {@link java.util.function.IntSupplier}, чтобы предоставить более "красивое" имя метода.
 */
public interface AICellNumberGenerator extends IntSupplier
{
    int generateCellNumber();
}
