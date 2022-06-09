package org.mmu.task7.events;

public class CpuTurnCompletedEvent extends GameStateChangedEventBase
{
    public final int cellNumber, rowIndex, colIndex;
    
    public CpuTurnCompletedEvent(Object source, int occupiedCellNumber, int boardSize)
    {
        super(source);
        cellNumber = occupiedCellNumber;
        rowIndex = occupiedCellNumber / boardSize;
        colIndex = occupiedCellNumber % boardSize;
    }
    
    public CpuTurnCompletedEvent(Object source, int _rowIndex, int _colIndex, int boardSize)
    {
        super(source);
        rowIndex = _rowIndex;
        colIndex = _colIndex;
        cellNumber = _rowIndex * boardSize + _colIndex;
    }
}
