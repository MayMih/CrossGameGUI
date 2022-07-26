package org.mmu.task7.aiengine;

import org.mmu.task7.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Класс "Среднего" ИИ - ходит по соседним клеткам стараясь выиграть, а когда до выигрыша ИИ или Игрока остаётся один ход, то выбирает его.
 */
public final class NormalAI implements AICellNumberGenerator
{
    public static final NormalAI instance = new NormalAI();
    
    private NormalAI()
    {
    }
    
    @Override
    public int generateCellNumber()
    {
        return generateCellNumber(true);
    }
    
    /**
     * Метод генерации к-т клетки для текущего хода ПК
     *
     * @param isCheckNearbyCellWinAbility Определяет, будут ли к-ты соседней клетки проверяться на возможность выигрыша,
     *                                    False - будет проверяться только возможность выигрыша текущим ходом ИИ или Игрока.
     */
    int generateCellNumber(boolean isCheckNearbyCellWinAbility)
    {
        int cellNumber;
        cellNumber = getCpuWinCellNumber();
        if (cellNumber < 0)
        {
            cellNumber = getPlayerWinCellNumber();
        }
        if (cellNumber < 0)
        {
            cellNumber = LowAI.instance.generateNearbyCoords(isCheckNearbyCellWinAbility);
        }
        return cellNumber;
    }
    
    /**
     * Метод поиска выигрышной ячейки
     *
     * @param turnsHistory - История ходов (ПК либо Игрока)
     */
    int getWinCellNumber(List<Integer> turnsHistory)
    {
        class WinCellFinder     // локальный класс - обёртка для повторяющейся "лямбды"
        {
            int findWinCell(IntStream ideal, List<Integer> source)
            {
                return ideal.filter(x -> GameState.Current.checkCell(x) && !source.contains(x)).findFirst().orElse(-1);
            }
        }
        
        int result = -1;
        if (turnsHistory == null || turnsHistory.size() <= 1)
        {
            return result;
        }
        // для каждого эл-та из Истории считаем кол-во клеток, не хватающих до победы
        ArrayList<Integer> checkedRows = new ArrayList<>();
        ArrayList<Integer> checkedCols = new ArrayList<>();
        boolean isMainDiagChecked = false;
        boolean isAuxDiagChecked = false;
        WinCellFinder cellFinder = new WinCellFinder();
        
        final int boardsize = GameState.Current.getBoardSize();
        
        for (Integer cellNumber : turnsHistory)
        {
            int rowNumber = GameState.Utils.getRowIndexFromCellNumber(cellNumber);
            if (checkedRows.contains(rowNumber))
            {
                continue;
            }
            // если до победы, например по строке, не хватает одной клетки и она пуста, то она и есть выигрышная
            List<Integer> sameRowHistoryElements = turnsHistory.stream().filter(x -> GameState.Utils.convertCellNumberToCoords(x)[0] == rowNumber).
                    collect(Collectors.toList());
            if (sameRowHistoryElements.size() == boardsize - 1)
            {
                try (IntStream idealRow = GameState.Utils.getIdealRow(rowNumber))
                {
                    result = cellFinder.findWinCell(idealRow, sameRowHistoryElements);
                    if (result >= 0)
                    {
                        return result;
                    }
                }
            }
            int collNumber = GameState.Utils.getColumnIndexFromCellNumber(cellNumber);
            if (checkedCols.contains(collNumber))
            {
                continue;
            }
            List<Integer> sameColHistoryElements = turnsHistory.stream().filter(x -> GameState.Utils.convertCellNumberToCoords(x)[1] == collNumber).
                    collect(Collectors.toList());
            if (sameColHistoryElements.size() == boardsize - 1)
            {
                try (IntStream idealColumn = GameState.Utils.getIdealColumn(collNumber))
                {
                    result = cellFinder.findWinCell(idealColumn, sameColHistoryElements);
                    if (result >= 0)
                    {
                        return result;
                    }
                }
            }
            // если это элемент главной диагонали и её ещё не проверяли
            if ((collNumber == rowNumber) && !isMainDiagChecked)
            {
                // Наблюдение: номера ячеек на главной диагонали отличаются на (N + 1)
                List<Integer> mainDiagHistoryElements = turnsHistory.stream().filter(x -> Math.abs(x - cellNumber) % (boardsize + 1) == 0).
                        collect(Collectors.toList());
                if (mainDiagHistoryElements.size() == boardsize - 1)
                {
                    try (IntStream idealMainDiag = GameState.Utils.getIdealMainDiag())
                    {
                        result = cellFinder.findWinCell(idealMainDiag, mainDiagHistoryElements);
                        if (result >= 0)
                        {
                            return result;
                        }
                    }
                }
                isMainDiagChecked = true;
            }
            // если это элемент побочной диагонали и её ещё не проверяли
            if (((collNumber + rowNumber) == (boardsize - 1)) && !isAuxDiagChecked)
            {
                // Наблюдение: номера ячеек на побочной диагонали отличаются на (N - 1)
                List<Integer> auxDiagHistoryElements = turnsHistory.stream().filter(x ->
                                GameState.Utils.getRowIndexFromCellNumber(x) + GameState.Utils.getColumnIndexFromCellNumber(x) == (boardsize - 1)).
                        collect(Collectors.toList());
                if (auxDiagHistoryElements.size() == boardsize - 1)
                {
                    try (IntStream idealAuxDiag = GameState.Utils.getIdealAuxDiag())
                    {
                        result = cellFinder.findWinCell(idealAuxDiag, auxDiagHistoryElements);
                        if (result >= 0)
                        {
                            return result;
                        }
                    }
                }
                isAuxDiagChecked = true;
            }
            checkedRows.add(rowNumber);
            checkedCols.add(collNumber);
        }
        return -1;
    }
    
    /**
     * Проверяет, не может ли ПК выиграть своим следующим ходом, если ДА - возвращает номер одной из выигрышных клеток.
     *
     * @return Номер выигрышной клетки или (-1), если таких клеток нет.
     */
    private int getCpuWinCellNumber()
    {
        return getWinCellNumber(GameState.Current.getCpuTurnsHistory());
    }
    
    /**
     * Проверяет, не может ли Игрок выиграть своим следующим ходом, если ДА - возвращает номер одной из выигрышных клеток.
     *
     * @return Номер выигрышной клетки или (-1), если таких клеток нет.
     */
    private int getPlayerWinCellNumber()
    {
        return getWinCellNumber(GameState.Current.getPlayerTurnsHistory());
    }
    
}
