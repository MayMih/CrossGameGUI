package org.mmu.task7.aiengine;

import org.mmu.task7.BoardUtils;
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
    
    @Override
    public int getAsInt()
    {
        return generateCellNumber();
    }
    
    /**
     * Метод генерации случайной пары "подходящих" координат
     *
     * @apiNote Предварительно нужен вызов {@link GameState#noMoreWinMoves()} иначе возможно зацикливание!
     *
     * @return Номер ячейки в квадратной таблице
     */
    int generateRandomEmptyCellCoords()
    {
        int rowIndex, colIndex;
        final int boardSize = GameState.getCurrent().getBoardSize();
        do
        {
            rowIndex = GameState.rand.nextInt(boardSize);
            colIndex = GameState.rand.nextInt(boardSize);
        }
        while (!GameState.getCurrent().checkCoords(rowIndex, colIndex));
        return BoardUtils.convertCoordsToCellNumber(rowIndex, colIndex);
    }
    
}
