package org.mmu.task7.aiengine;

import org.mmu.task7.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс "Низкого" ИИ - ходит по соседним клеткам, не учитывая больше никаких условий.
 */
public final class LowAI implements AICellNumberGenerator
{
    public static final LowAI instance = new LowAI();
    
    private LowAI()
    {
    }
    
    @Override
    public int generateCellNumber()
    {
        return generateNearbyCoords(false);
    }
    
    /**
     * Генерирует номер клетки соседней с одной из имеющихся у ПК, если таких нет, то случайные координаты пустой клетки
     *
     * @param isCheckWinAbility Определяет, будут ли сгенерированные к-ты проверяться на их полезность для выигрыша (True)
     */
    int generateNearbyCoords(boolean isCheckWinAbility)
    {
        if (GameState.Current.getCpuTurnsHistory().isEmpty())
        {
            return StupidAI.instance.generateRandomEmptyCellCoords();
        }
        ArrayList<Integer> futureCpuTurnsHistory = null;
        
        if (isCheckWinAbility)
        {
            futureCpuTurnsHistory = new ArrayList<>(GameState.Current.getCpuTurnsHistory());
        }
        // сначала пытаемся сгенерировать соседнюю точку
        int resultCell = -1;
        for (Integer baseTurnCellNumber : GameState.Current.getCpuTurnsHistory())
        {
            // получаем опорную точку, относительно которой будем пытаться делать ход
            List<Integer> freeCells = GameState.Current.getEmptyCellsInRegion(baseTurnCellNumber);
            if (!isCheckWinAbility && !freeCells.isEmpty())
            {
                return freeCells.get(0);    // если проверка полезности хода не требуется, то возвращаем первую попавшуюся соседнюю клетку
            }
            // ищем наиболее оптимальный ход (который может в дальнейшем привести к победе)
            for (Integer emptyCell : freeCells)
            {
                resultCell = emptyCell;                             // запоминаем пустую соседнюю клетку-кандидат, т.к. более полезных может и не быть.
                if (GameState.Current.getBoardSize() <= 3)          // Для размера 3х3 можно обойтись алгоритмом поиска выигрышной клетки на базе
                                                                    // мнимой истории ходов
                {
                    futureCpuTurnsHistory.add(emptyCell);
                    //TODO: метод получения выигрышной клетки будет хорошо работать только для поля 3 х 3, т.к. здесь любая соседняя клетка
                    //  с большой вероятностью будет создавать выигрышную серию - для больших полей нужен другой метод оценки полезности хода!!
                    int winCell = NormalAI.instance.getWinCellNumber(futureCpuTurnsHistory);
                    if (winCell >= 0)
                    {
                        return winCell;     // для игры 3 х 3 возврат несоседней потенциально выигрышной клетки выглядит даже лучше (хитрее), но для
                                            // больших досок нужно возвращать соседнюю, либо случайную клетку из выигрышной линии!
                    }
                    else
                    {
                        futureCpuTurnsHistory.remove(emptyCell);
                    }
                }
                else        // случай большого поля (4х4 и более)
                {
                    //Utils.getIdealRow()
                }
            }
        }
        // если же походить в соседнюю клетку не смогли (видимо все они заняты), то делаем случайный ход
        if (resultCell == -1)
        {
            return StupidAI.instance.generateRandomEmptyCellCoords();
        }
        return resultCell;
    }
}
