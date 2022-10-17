package org.mmu.task7;

import java.util.stream.IntStream;

/**
 * Вспомогательный класс с методами для преобразования номера строки и столбца в номер ячейки и наоборот
 */
public final class BoardUtils
{
    // прячем конструктор от внешнего кода, т.к. в Java можно создавать
    // экземпляры вложенных статических классов! То, что класс помечен спецификатором static, в данном случае значит
    // лишь то, что его экземпляры могут быть созданы Без создания экземпляра класса-родителя (GameState)
    private BoardUtils()
    {
    }
    
    public static int[] convertCellNumberToCoords(int number)
    {
        return new int[] {number / GameState.getCurrent().getBoardSize(), number % GameState.getCurrent().getBoardSize()};
    }
    
    public static int convertCoordsToCellNumber(int rowIndex, int columnIndex)
    {
        return rowIndex * GameState.getCurrent().getBoardSize() + columnIndex;
    }
    
    public static int getRowIndexFromCellNumber(int cellNumber)
    {
        return cellNumber / GameState.getCurrent().getBoardSize();
    }
    
    public static int getColumnIndexFromCellNumber(int cellNumber)
    {
        return cellNumber % GameState.getCurrent().getBoardSize();
    }
    
    /**
     * Метод получения идеального набора ячеек, приводящего к победе по строке, содержащей указанную ячейку
     *
     * @param cellNumber Номер выигрышной строки, которую нужно сгенерировать
     *
     * @return Идеальный набор ячеек
     *
     * @apiNote ВНИМАНИЕ: "идеальная" последовательность будет содержать в т.ч. и Занятые ячейки!
     */
    public static IntStream getIdealRowForCellNumber(int cellNumber)
    {
        return getIdealRow(getRowIndexFromCellNumber(cellNumber));
    }
    
    /**
     * Метод получения идеального набора ячеек, приводящего к победе по строке, содержащей указанную ячейку
     *
     * @param rowNumber Координата выигрышной строки, которую нужно сгенерировать
     *
     * @return Идеальный набор ячеек
     *
     * @apiNote ВНИМАНИЕ: "идеальная" последовательность будет содержать в т.ч. и Занятые ячейки!
     */
    public static IntStream getIdealRow(int rowNumber)
    {
        return IntStream.iterate(convertCoordsToCellNumber(rowNumber, 0), x -> x + 1).limit(GameState.getCurrent().getBoardSize());
    }
    
    /**
     * Метод получения идеального набора ячеек, приводящего к победе по строке, содержащей указанную ячейку
     *
     * @param collNumber Координата выигрышного столбца, который нужно сгенерировать
     *
     * @return Идеальный набор ячеек
     *
     * @apiNote ВНИМАНИЕ: "идеальная" последовательность будет содержать в т.ч. и Занятые ячейки!
     */
    public static IntStream getIdealColumn(int collNumber)
    {
        final int board_size = GameState.getCurrent().getBoardSize();
        return IntStream.iterate(convertCoordsToCellNumber(0, collNumber), x -> x + board_size).limit(board_size);
    }
    
    /**
     * Метод получения идеального набора ячеек, приводящего к победе по главной диагонали, содержащей указанную ячейку
     *
     * @return Идеальный набор ячеек
     *
     * @apiNote ВНИМАНИЕ: "идеальная" последовательность будет содержать в т.ч. и Занятые ячейки!
     */
    public static IntStream getIdealMainDiag()
    {
        final int board_size = GameState.getCurrent().getBoardSize();
        // Наблюдение: номера ячеек на главной диагонали отличаются на (N + 1)
        return IntStream.iterate(0, x -> x + board_size + 1).limit(board_size);
    }
    
    /**
     * Метод получения идеального набора ячеек, приводящего к победе по побочной диагонали, содержащей указанную ячейку
     *
     * @return Идеальный набор ячеек
     *
     * @apiNote ВНИМАНИЕ: "идеальная" последовательность будет содержать в т.ч. и Занятые ячейки!
     */
    public static IntStream getIdealAuxDiag()
    {
        final int board_size = GameState.getCurrent().getBoardSize();
        // Наблюдение: номера ячеек на побочной диагонали отличаются на (N - 1)
        return IntStream.iterate(board_size - 1, x -> x + board_size - 1).limit(board_size);
    }
    
    /**
     * Метод проверки того, являются ли клетки <b>родственными</b>, т.е. есть ли у них совпадающие к-ты строки или столбца
     *
     * @param cellA - первая проверяемая ячейка
     * @param cellB - вторая проверяемая ячейка
     *
     * @return Признак того, принадлежать ли ячейки одной строке/столбцу
     */
    public static boolean isSameRowOrColumnCells(int cellA, int cellB)
    {
        int[] c1 = convertCellNumberToCoords(cellA);
        int[] c2 = convertCellNumberToCoords(cellB);
        return c1[0] == c2[0] || c1[1] == c2[1];
    }
    
    public static boolean isSameRowCells(int cellA, int cellB)
    {
        return getRowIndexFromCellNumber(cellA) == getColumnIndexFromCellNumber(cellB);
    }
    
    public static boolean isSameColCells(Integer cellA, Integer cellB)
    {
        return getColumnIndexFromCellNumber(cellA) == getColumnIndexFromCellNumber(cellB);
    }
    
    public static boolean isMainDiagCoords(int rowNumber, int colNumber)
    {
        return colNumber == rowNumber;
    }
    
    public static boolean isAuxDiagCoords(int rowNumber, int colNumber)
    {
        return (colNumber + rowNumber) == (GameState.getCurrent().getBoardSize() - 1);
    }
}
