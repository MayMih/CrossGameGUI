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
        Map.Entry<Integer, Long> ratedCellNumber;
        int cellNumber;
        if (!isCheckMultilineWinCell && GameState.current.getBoardSize() <= 3)   //"упрощённый" алгоритм для малого поля
        {
            cellNumber = getCpuWinCellNumber();
            if (cellNumber >= 0)
            {
                return cellNumber;
            }
            ratedCellNumber = new AbstractMap.SimpleEntry<>(cellNumber, 1L);
        }
        else
        {
            ratedCellNumber = getBestCell(isCheckMultilineWinCell);        // для полей 4х4 и более нужен подсчёт очков клетки
        }
        if (ratedCellNumber.getKey() < 0 || ratedCellNumber.getValue() > 1)   //если клетка НЕ победная, то проверяем игрока
        {
            cellNumber = getPlayerWinCellNumber();
            if (cellNumber >= 0)
            {
                return cellNumber;
            }
        }
        // подводим итоги: если не удалось найти выигрышную (или потенциально выигрышную для 4х4]) клетку, то генерируем соседнюю
        if (ratedCellNumber.getKey() < 0)
        {
            //assert GameState.current.getBoardSize() <= 3 : "Избыточный поиск соседних координат"; // возможно, если нет истории ходов
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
     *
     * @return Пара (номер ячейки, рейтинг), где, чем меньше <b>рейтинг</b>, тем лучше: "1" - один шаг до победы
     *          ИЛИ (-1), если нет истории ходов ПК.
     */
    AbstractMap.SimpleEntry<Integer, Long> getBestCell(boolean isGetMultilineWinCell)
    {
        final List<Integer> cpuTurns = GameState.current.getCpuTurnsHistory();
        final List<Integer> playerTurns = GameState.current.getPlayerTurnsHistory();
        if (cpuTurns.isEmpty())
        {
            return new AbstractMap.SimpleEntry<>(-1, -1L);
        }
        // Класс-счётчик пустых клеток на линии (линия должна быть предварительно проверена
        class AvailableTurnsAnalyzer
        {
            // Ключ - номер свободной ячейки, Значение - кол-во свободных ячеек в строке/ряду/диагонали (содержит повторения)
            public final List<Map.Entry<Integer, Long>> ratedTurns = new ArrayList<>();
        
            private void updateTurnsAndScore(int cellNumber, IntStream ideal)
            {
                long emptyCellsCount = ideal.filter(x -> !cpuTurns.contains(x)).count();
                if (emptyCellsCount > 0)
                {
                    ratedTurns.add(new AbstractMap.SimpleEntry<>(cellNumber, emptyCellsCount));
                }
            }
        }
        
        final ArrayList<Integer> checkedRows = new ArrayList<>();
        final ArrayList<Integer> checkedCols = new ArrayList<>();
        final AvailableTurnsAnalyzer turnsAnalyzer = new AvailableTurnsAnalyzer();
        final int boardSize = GameState.current.getBoardSize();
        // если на диагоналях есть хотя бы одна клетка заполненная Игроком, то считаем эту диагональ уже проверенной (т.е. неподходящей)
        boolean isMainDiagChecked = GameState.Utils.getIdealMainDiag().anyMatch(playerTurns::contains);
        boolean isAuxDiagChecked = GameState.Utils.getIdealAuxDiag().anyMatch(playerTurns::contains);
        // проверяем все ячейки в таблице
        for (int i = 0; i < boardSize * boardSize; i++)
        {
            if (!GameState.current.checkCell(i))
            {
                continue;
            }
            final int rowNumber = GameState.Utils.getRowIndexFromCellNumber(i);
            // пытаемся получить случайную клетку из потенциально выигрышной строки
            if (!checkedRows.contains(rowNumber))
            {
                // приходится два раза пересоздавать IntStream, т.к. к стримам в Java можно применять только одну терминальную операцию!
                if (GameState.Utils.getIdealRow(rowNumber).noneMatch(playerTurns::contains))
                {
                    turnsAnalyzer.updateTurnsAndScore(i, GameState.Utils.getIdealRow(rowNumber));
                }
                checkedRows.add(rowNumber);
            }
            final int colNumber = GameState.Utils.getColumnIndexFromCellNumber(i);
            // пытаемся получить случайную клетку из потенциально выигрышного столбца
            if (!checkedCols.contains(colNumber))
            {
                if (GameState.Utils.getIdealColumn(colNumber).noneMatch(playerTurns::contains))
                {
                    turnsAnalyzer.updateTurnsAndScore(i, GameState.Utils.getIdealColumn(colNumber));
                }
                checkedCols.add(colNumber);
            }
            if (!isMainDiagChecked && GameState.Utils.isMainDiagCoords(rowNumber, colNumber))
            {
                turnsAnalyzer.updateTurnsAndScore(i, GameState.Utils.getIdealMainDiag());
                isMainDiagChecked = true;
            }
            if (!isAuxDiagChecked && GameState.Utils.isAuxDiagCoords(rowNumber, colNumber))
            {
                turnsAnalyzer.updateTurnsAndScore(i, GameState.Utils.getIdealAuxDiag());
                isAuxDiagChecked = true;
            }
        }
        // выбираем из списка доступных ходов ту клетку, которая имеет наименьшее кол-во пустых клеток на линии
        Optional<Map.Entry<Integer, Long>> opt = turnsAnalyzer.ratedTurns.stream().min(Map.Entry.comparingByValue());
        long minValue = opt.isPresent() ? opt.get().getValue() : -1;
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
            private int findWinCell(IntStream ideal, List<Integer> source)
            {
                return ideal.filter(x -> GameState.current.checkCell(x) && !source.contains(x)).findAny().orElse(-1);
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
        
        final int boardsize = GameState.current.getBoardSize();
        
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
        return getWinCellNumber(GameState.current.getCpuTurnsHistory());
    }
    
    /**
     * Проверяет, не может ли Игрок выиграть своим следующим ходом, если ДА - возвращает номер одной из выигрышных клеток.
     *
     * @return Номер выигрышной клетки или (-1), если таких клеток нет.
     */
    private int getPlayerWinCellNumber()
    {
        return getWinCellNumber(GameState.current.getPlayerTurnsHistory());
    }
    
}
