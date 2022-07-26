package org.mmu.task7.aiengine;

import org.mmu.task7.GameState;

/**
 * Класс очень "Тупого" ИИ - ходит по случайным пустым клеткам на поле
 */
public final class StupidAI implements AICellNumberGenerator
{
    public static final StupidAI instance = new StupidAI();
    
    private StupidAI()
    {
    }
    
    /**
     * Синоним метода {@link #generateRandomEmptyCellCoords()}
     */
    @Override
    public int generateCellNumber()
    {
        return generateRandomEmptyCellCoords();
    }
    
    /**
     * Метод генерации случайной пары "подходящих" координат
     *
     * @return Номер ячейки в квадратной таблице
     */
    int generateRandomEmptyCellCoords()
    {
        int rowIndex, colIndex;
        do
        {
            rowIndex = GameState.rand.nextInt(GameState.Current.getBoardSize());
            colIndex = GameState.rand.nextInt(GameState.Current.getBoardSize());
        }
        while (!GameState.Current.checkCoords(rowIndex, colIndex));
        return GameState.Utils.convertCoordsToCellNumber(rowIndex, colIndex);
    }
}
