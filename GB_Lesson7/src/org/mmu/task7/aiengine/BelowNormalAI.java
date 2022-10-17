package org.mmu.task7.aiengine;

/**
 * Класс ИИ "Ниже Среднего" - ходит по соседним клеткам Не стараясь выиграть, Но когда до выигрыша ИИ или Игрока остаётся один ход, то выбирает его.
 */
public final class BelowNormalAI implements AICellNumberGenerator
{
    public static final BelowNormalAI instance = new BelowNormalAI();
    
    private BelowNormalAI()
    {
    }
    
    @Override
    public int generateCellNumber()
    {
        return NormalAI.instance.generateCellNumber(false, false);
    }
    
    @Override
    public int getAsInt()
    {
        return generateCellNumber();
    }
}
