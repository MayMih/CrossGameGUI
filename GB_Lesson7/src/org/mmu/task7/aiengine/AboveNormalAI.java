package org.mmu.task7.aiengine;

/**
 * Класс ИИ "Выше Среднего" - ходит по клеткам линий, с учётом их выгодности, учитывает возможность выигрыша по двум линиям.
 *
 * @implNote Производит лишь частичный подсчёт выгодности (на основе кол-ва свободных клеток в линии, но не учитывает
 *              возможность помешать противнику, кроме ситуации, когда игроку осталась одна клетка до победы)
 *
 */
public final class AboveNormalAI implements AICellNumberGenerator
{
    public static final AboveNormalAI instance = new AboveNormalAI();
    
    private AboveNormalAI() {}
    
    @Override
    public int generateCellNumber()
    {
        return NormalAI.instance.generateCellNumber(true, true);
    }
    
    @Override
    public int getAsInt()
    {
        return generateCellNumber();
    }
}
