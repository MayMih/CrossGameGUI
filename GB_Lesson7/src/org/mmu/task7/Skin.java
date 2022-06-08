package org.mmu.task7;

import javax.swing.*;

/**
 * Перечисление возможных стилей интерфейса
 * */
public enum Skin
{
    System("Стиль ОС по умолчанию", UIManager.getSystemLookAndFeelClassName()),
    /**
     * Стандартный кроссплатформенный стиль интерфейса
     * */
    Metal("Стандартный кроссплатформенный стиль интерфейса", UIManager.getCrossPlatformLookAndFeelClassName()),
    /**
     * Современный кроссплатформенный стиль интерфейса (начиная с Java 6)
     * */
    Nimbus("Современный кроссплатформенный стиль интерфейса (начиная с Java 6)"),
    /**
     * Сторонний бесплатный стиль - светлая тема -  (минимум Java 8)
     * @see <a href=https://www.formdev.com/flatlaf/>FlatLaf - Flat Look and Feel</>
     * */
    FlatLight("Сторонний бесплатный стиль - светлая тема - (минимум Java 8)", "com.formdev.flatlaf.FlatLightLaf"),
    /**
     * Сторонний бесплатный стиль - тёмная тема - (минимум Java 8)
     * @see <a href=https://www.formdev.com/flatlaf/themes/>FlatLaf - Flat Look and Feel</>
     * */
    FlatDark("Сторонний бесплатный стиль - светлая тема - (минимум Java 8)", "com.formdev.flatlaf.FlatDarkLaf"),
    /**
     * Сторонний бесплатный стиль - тёмная тема - (минимум Java 8)
     * @see <a href=https://www.formdev.com/flatlaf/themes/>FlatLaf - Flat Look and Feel</>
     * */
    FlatIdea("Сторонний бесплатный стиль - светлая тема в духе ItelliJ Idea 2019.2+ - (минимум Java 8)",
            "com.formdev.flatlaf.FlatIntelliJLaf"),
    /**
     * Сторонний бесплатный стиль - тёмная тема - (минимум Java 8)
     * @see <a href=https://www.formdev.com/flatlaf/themes/>FlatLaf - Flat Look and Feel</>
     * */
    FlatDracula("Сторонний бесплатный стиль - тёмная тема в духе ItelliJ Idea 2019.2+ - (минимум Java 8)",
            "com.formdev.flatlaf.FlatDarculaLaf");
    
    private String lafClassPath;
    
    final String toolTip;
    
    Skin() { this(""); };
    
    Skin(String description)
    {
        toolTip = description;
    }
    
    Skin(String description, String lafPath)
    {
        this(description);
        lafClassPath = lafPath;
    }
    
    public void setLAFClassPath(String path)
    {
        lafClassPath = path;
    }
    
    public String getLAFClassPath()
    {
        return lafClassPath;
    }
}
