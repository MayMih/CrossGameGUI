package org.mmu.task7;

/**
 * Возможные уровни Искусственного интеллекта противника
 */
public enum AILevel
{
    /**
     * Неизвестный уровень - значение по умолчанию для пустых полей этого типа
     */
    Unknown("Не задан - пропускает ходы (для отладки)"),
    /*
     * Тупой - Ходит случайным образом
     * */
    Stupid("Тупой - Ходит случайным образом"),
    /**
     * Низкий - ходит по клеткам соседним со своими
     */
    Low("Низкий - ходит по клеткам соседним со своими");
    /**
     * Ниже Среднего - ходит по клеткам соседним со своими и проверяет, не будет ли следующий ход игрока выигрышным
     * (если да, то пытается препятствовать выигрышу вместо своего хода - не реализовано)
     */
    //BelowNormal,  // ещё не реализовано
    /**
     * *Алгоритм с подсчётом очков для каждой клетки (определение выгодности хода)
     */
    //Normal        // ещё не реализовано
    
    /**
     * Конструктор перечисления - позволяет задать описания для элементов перечисления
     * */
    private AILevel(String _description)
    {
        description = _description;
    }
    
    public final String description;
}
