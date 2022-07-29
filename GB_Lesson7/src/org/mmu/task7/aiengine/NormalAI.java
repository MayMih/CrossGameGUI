package org.mmu.task7.aiengine;

import org.mmu.task7.GameState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Класс "Среднего" ИИ - ходит по клеткам линии, стараясь выиграть, а когда до выигрыша ИИ или Игрока остаётся один ход,
 *      то выбирает его.
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
        return generateCellNumber(true, false);
    }
    
    /**
     * Метод генерации к-т клетки для текущего хода ПК
     *
     * @param isCheckNearbyCellWinAbility Определяет, будут ли к-ты соседней клетки проверяться на возможность выигрыша,
     *                                    False - будет проверяться только возможность выигрыша текущим ходом ИИ или Игрока.
     * @param isCheckMultilineWinCell Определяет, будет ли производиться поиск ячеек выигрышных
     *                                сразу по нескольким линиям (True) или только по одной (False)
     */
    int generateCellNumber(boolean isCheckNearbyCellWinAbility, boolean isCheckMultilineWinCell)
    {
        Map.Entry<Integer, Integer> ratedCellNumber;
        int cellNumber;
        if (!isCheckMultilineWinCell && GameState.Current.getBoardSize() <= 3)
        {
            cellNumber = getCpuWinCellNumber();
            if (cellNumber >= 0)
            {
                return cellNumber;
            }
            ratedCellNumber = new AbstractMap.SimpleEntry<>(cellNumber, 1);
        }
        else
        {
            ratedCellNumber = this.getBestCell(isCheckMultilineWinCell);
        }
        if (ratedCellNumber.getKey() < 0 || ratedCellNumber.getValue() > 1)   // если клетка НЕ победная, то проверяем игрока
        {
            cellNumber = getPlayerWinCellNumber();
            if (cellNumber >= 0)
            {
                return cellNumber;
            }
            ratedCellNumber = new AbstractMap.SimpleEntry<>(cellNumber, 1);
        }
        if (ratedCellNumber.getKey() < 0)
        {
            return LowAI.instance.generateNearbyCoords(isCheckNearbyCellWinAbility);
        }
        else
        {
            return ratedCellNumber.getKey();
        }
    }
    
    /**
     * Метод получения ячейки с наиболее выгодным ходом (для победы ИИ)
     *
     * @param isGetMultilineWinCell Определяет, будет ли производиться поиск ячейки, которая может дать выигрыш сразу по
     *                          нескольким линиям (True) или первой попавшейся, дающей выигрыш по линии (False).
     *
     * @implNote Учитывает ситуацию, когда клетка сразу участвует в победных строке и столбце
     * @return Пара (номер ячейки, рейтинг), где, чем меньше <b>рейтинг</b>, тем лучше: "1" - один шаг до победы
     */
    AbstractMap.SimpleEntry<Integer, Integer> getBestCell(boolean isGetMultilineWinCell)
    {
        class AvailableTurnsAnalyzer
        {
            private final List<Integer> availableTurns = new ArrayList<>();
            private final List<Integer> cpuHistory = GameState.Current.getCpuTurnsHistory();
            // Ключ - номер свободной ячейки, Значение - кол-во свободных ячеек в строке/ряду/диагонали
            public final List<Map.Entry<Integer, Integer>> ratedTurns = new ArrayList<>();
            
            private void updateTurnsAndScore(IntStream ideal)
            {
                availableTurns.clear();
                availableTurns.addAll(ideal.boxed().collect(Collectors.toList()));
                availableTurns.stream().filter(x -> !cpuHistory.contains(x)).findAny().ifPresent(x ->
                        ratedTurns.add(new AbstractMap.SimpleEntry<>(x, availableTurns.size()))
                );
            }
        }
    
        final List<Integer> playerTurns = GameState.Current.getPlayerTurnsHistory();
        final ArrayList<Integer> checkedRows = new ArrayList<>();
        final ArrayList<Integer> checkedCols = new ArrayList<>();
        final AvailableTurnsAnalyzer turnsAnalyzer = new AvailableTurnsAnalyzer();
        
        boolean isMainDiagChecked = false;
        boolean isAuxDiagChecked = false;
        
        for (int i = 0; i < GameState.Current.getBoardSize(); i++)
        {
            if (!GameState.Current.checkCell(i))
            {
                continue;
            }
            final int rowNumber = GameState.Utils.getRowIndexFromCellNumber(i);
            // пытаемся получить случайную клетку из потенциально выигрышной строки
            if (!checkedRows.contains(rowNumber) && GameState.Utils.getIdealRow(i).noneMatch(playerTurns::contains))
            {
                turnsAnalyzer.updateTurnsAndScore(GameState.Utils.getIdealRow(i));
                checkedRows.add(rowNumber);
            }
            final int colNumber = GameState.Utils.getColumnIndexFromCellNumber(i);
            // пытаемся получить случайную клетку из потенциально выигрышного столбца
            if (!checkedCols.contains(colNumber) && GameState.Utils.getIdealColumn(i).noneMatch(playerTurns::contains))
            {
                turnsAnalyzer.updateTurnsAndScore(GameState.Utils.getIdealColumn(i));
                checkedCols.add(colNumber);
            }
            if (!isMainDiagChecked && GameState.Utils.isMainDiagCoords(rowNumber, colNumber) && GameState.Utils.
                    getIdealMainDiag().noneMatch(playerTurns::contains))
            {
                turnsAnalyzer.updateTurnsAndScore(GameState.Utils.getIdealMainDiag());
                isMainDiagChecked = true;
            }
            if (!isAuxDiagChecked && GameState.Utils.isAuxDiagCoords(rowNumber, colNumber) && GameState.Utils.
                    getIdealAuxDiag().noneMatch(playerTurns::contains))
            {
                turnsAnalyzer.updateTurnsAndScore(GameState.Utils.getIdealAuxDiag());
                isAuxDiagChecked = true;
            }
        }
        // выбираем из списка доступных ходов ту клетку, которая имеет наименьшее кол-во пустых клеток на линии
        Optional<Map.Entry<Integer, Integer>> opt = turnsAnalyzer.ratedTurns.stream().min(Map.Entry.comparingByValue());
//                Comparator.comparingInt(
//                Map.Entry::getValue));
        int minValue = opt.isPresent() ? opt.get().getValue() : -1;
        if (isGetMultilineWinCell)
        {
            // выбираем из списка записи с минимальным рейтингом и ищем среди них тот номер клетки, что встречается
            // наибольшее число раз
            Optional<Integer> optCellNumber = turnsAnalyzer.ratedTurns.stream().filter(x -> x.getValue().equals(minValue)).
                    collect(Collectors.groupingBy(Map.Entry::getKey)).entrySet().stream().max(Comparator.comparingInt(x ->
                            x.getValue().size())).map(Map.Entry::getKey);
    
            return new AbstractMap.SimpleEntry<>(optCellNumber.orElse(-1), minValue);
        }
        else
        {
            return new AbstractMap.SimpleEntry<>(opt.isPresent() ? opt.get().getKey() : -1, minValue);
        }
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
