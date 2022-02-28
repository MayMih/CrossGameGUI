package org.mmu.task7;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class GameForm
{
    
    /**
     * Возможные уровни Искусственного интеллекта противника
     */
    private enum AILevel
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

    /**
     * Перечисление возможных стилей интерфейса
     * */
    enum Skin
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
        FlatIdea("Сторонний бесплатный стиль - свелая тема в духе ItelliJ Idea 2019.2+ - (минимум Java 8)",
                "com.formdev.flatlaf.FlatIntelliJLaf"),
        /**
         * Сторонний бесплатный стиль - тёмная тема - (минимум Java 8)
         * @see <a href=https://www.formdev.com/flatlaf/themes/>FlatLaf - Flat Look and Feel</>
         * */
        FlatDracula("Сторонний бесплатный стиль - тёмная тема в духе ItelliJ Idea 2019.2+ - (минимум Java 8)",
                "com.formdev.flatlaf.FlatDarculaLaf");
        
        private String lafClassPath;
        private final String toolTip;
        
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
    
    
    //region 'Поля и константы'
    
    private JTable tableGameField;
    private JPanel pMain;
    
    private JFrame _mainFrame = new JFrame();
    
    /**
     *  Определяет, будет ли выводиться в консоль дополнительная отладочная информация (например о перехвате клавиш)
     *
     * @apiNote     TODO: раз в Java нет понятия отладочной сборки, то логичнее выводить управление этой константой во
     *  внешний "мир" (параметры запуска в консоли/файл настоек/переменные окружения и т.п.)
     * */
    private static boolean IS_DEBUG;
    private static final String ABOUT_TEXT;
    private static final String ICON_FILE_RESOURCE_NAME = "/icons/interface57.png";
    private static final Skin DEFAULT_LOOK_AND_FEEL = Skin.FlatIdea;
    private static final AILevel DEFAULT_AI_LEVEL = AILevel.Low;
    private static final int DEFAULT_BOARD_SIZE = 3;
    private static final String BOARD_SIZE_CAPTION_START = "Размер поля:";
    private static final Package _pkg;
    private static final Random _rand = new Random();
    
    private int _boardSize = DEFAULT_BOARD_SIZE;
    private Point _mouseDownCursorPos;
    private AILevel _aiLevel = DEFAULT_AI_LEVEL;
    
    //endregion 'Поля и константы'
    
    
    /**
     * Статический конструктор - задаёт текст окна "О программе"
     * */
    static
    {
        IS_DEBUG = false;
        _pkg = GameForm.class.getPackage();
        ABOUT_TEXT = MessageFormat.format("<html><body>" +
                "<h1>{0}</h1><h2>Версия: {1}</h2><h3>Автор: {2}</h3><p>Создана в 2022 г. в рамках выполнения задания №7 " +
                "курса GeekBrains \"Основы Java. Интерактивный курс\"</p>" +
                "<p>В качестве возможных тем оформления интерфейса задействован открытый проект <b>FlatLaf</b> компании <i>FormDev</i></p>" +
                "<p>Ссылки: <a href=https://www.formdev.com/flatlaf/>FlatLaf - Flat Look and Feel</a></p>" +
                "<p>Лицензия на FlatLaf: <a href=https://github.com/JFormDesigner/FlatLaf/blob/master/LICENSE>Apache 2.0 License</a></p>" +
                "</body></html>", _pkg.getSpecificationTitle(), _pkg.getSpecificationVersion(), _pkg.getSpecificationVendor());
    }
    
    /**
     * Точка входа в программу - устанавливает скин GUI, создаёт главную форму игры
     * */
    public static void main(String[] args)
    {
        if (Arrays.stream(args).anyMatch(x -> {
            String arg = x.trim();
            return (arg.startsWith("-") || arg.startsWith("/")) && (x.trim().endsWith("debug") ||
                    x.trim().endsWith("d"));
        }))
        {
            IS_DEBUG = true;
        }
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Устанавливаем более современный кросплатформенный "скин" Nimbus для интерфейса
                //  (см.: <a href=https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/nimbus.html></a>)
                try
                {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                    {
                        if (Skin.Nimbus.name().equalsIgnoreCase(info.getName()))
                        {
                            Skin.Nimbus.setLAFClassPath(info.getClassName());
                            break;
                        }
                    }
                    UIManager.setLookAndFeel(GameForm.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                }
                catch (Exception e)
                {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    System.err.println("Не удалось установить стиль интерфейса по умолчанию: " +
                            GameForm.DEFAULT_LOOK_AND_FEEL.name() + " Classpath: " + GameForm.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                    System.err.println();
                    System.err.println(e.toString());
                    e.printStackTrace();
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
                // Создаём нашу игровую фому и навешиваем на неё обработчики событий
                try
                {
                    new GameForm().setActionListeners();
                }
                catch (Exception ex)
                {
                    String mes = MessageFormat.format("Не удалось создать главный класс игры \"{1}\"{0}{2}",
                            System.lineSeparator(), GameForm.class.getName(), ex.toString());
                    System.err.println(mes);
                    System.err.println();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, mes, "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    /**
     * Конструктор главной формы
     * */
    public GameForm()
    {
        String progName = _pkg.getImplementationTitle() + "";
        _mainFrame.setTitle(progName.isEmpty() || progName.equalsIgnoreCase("null") ? "# \"Крестики-нолики\"" : progName);
        _mainFrame.setContentPane(pMain);
        _mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _mainFrame.setLocationByPlatform(true);
        _mainFrame.setMinimumSize(new Dimension(480, 240));
        try
        {
            ImageIcon img = new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_FILE_RESOURCE_NAME)));
            _mainFrame.setIconImage(img.getImage());
        }
        catch (Exception ex)
        {
            System.err.println("Не найден ресурс: " + ICON_FILE_RESOURCE_NAME);
        }
        createMainMenuFor(_mainFrame);
        _mainFrame.pack();
        _mainFrame.setVisible(true);
    }
    
    
    //region 'Методы'
    
    /**
     * Метод создания главного меню программы
     * */
    private void createMainMenuFor(JFrame jf)
    {
        JMenuBar miniBar = new JMenuBar();
        JMenuItem miNewGame = new JMenuItem("Новая игра");
        miniBar.add(miNewGame);
        JMenu mOptions = new JMenu("Опции");
        miniBar.add(mOptions);
        JMenu mSkins = new JMenu("Скин");
        mOptions.add(mSkins);
        JMenu mAILevels = new JMenu("Уровень ИИ");
        mOptions.add(mAILevels);
        ButtonGroup bgSkins = new ButtonGroup();
        for (Skin skn : Skin.values())
        {
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(skn.name(), skn == this.DEFAULT_LOOK_AND_FEEL);
            rb.setToolTipText(skn.toolTip);
            rb.addItemListener(skinChangedMenuClickHandler);
            bgSkins.add(rb);
            mSkins.add(rb);
        }
        mOptions.add(mSkins);
        ButtonGroup bgAI = new ButtonGroup();
        for (AILevel lvl : AILevel.values())
        {
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lvl.description, lvl == this.DEFAULT_AI_LEVEL);
            rb.addItemListener(aiLevelChangedMenuClickHandler);
            bgAI.add(rb);
            mAILevels.add(rb);
        }
        JMenuItem miChangeBoardSize = mOptions.add(changeBoardSizeClickHandler);
        JEditorPane txtAbout = new JEditorPane("text/html", ABOUT_TEXT);
        txtAbout.addHyperlinkListener(this.hyperlinkClickHandler);
        txtAbout.setEditable(false);
        //TODO: похоже установка фона полей ввода не срабатывает для скина Nimbus
        // возможное решение: {@see https://stackoverflow.com/a/33446134/2323972}
        txtAbout.setBackground(SystemColor.control);
        JMenuItem miAbout = mOptions.add(new AbstractAction("О программе")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog((Component) e.getSource(), txtAbout, "О программе", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        //miniBar.add(miAbout);
        jf.setJMenuBar(miniBar);
    }
    
    /**
     * Метод навешивания обработчиков
     * */
    private void setActionListeners()
    {
    
    }
    
    //endregion 'Методы'
    
    
    
    
    //region 'Обработчики'
    
    /**
     * Обработчик ссылок в окне "О программе"
     * */
    private final HyperlinkListener hyperlinkClickHandler = e -> {
        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
        {
            try
            {
                Desktop.getDesktop().browse(e.getURL().toURI());    // roll your own link launcher or use Desktop if J6+
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    
    /**
     * Обработчик меню изменения размера Поля
    * */
    private final Action changeBoardSizeClickHandler = new AbstractAction(BOARD_SIZE_CAPTION_START + " " +
            DEFAULT_BOARD_SIZE + "x" + DEFAULT_BOARD_SIZE)
    {
        private void updateBoardSize()
        {
            JOptionPane.showMessageDialog(pMain, "Нужно подумать - разрешать ли менять размер доски посреди партии",
                    "Ещё не реализовано!", JOptionPane.QUESTION_MESSAGE);
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String newSize = "";
            do
            {
                newSize = JOptionPane.showInputDialog((Component) e.getSource(), "Введите новый размер поля",
                        "Изменение размера игрового поля", JOptionPane.INFORMATION_MESSAGE);
                try
                {
                    if (newSize == null)    // пользователь отказался от ввода
                    {
                        newSize = String.valueOf(_boardSize);
                    }
                    else
                    {
                        _boardSize = Integer.parseUnsignedInt(newSize);
                        updateBoardSize();
                    }
                }
                catch (NumberFormatException nex)
                {
                    JOptionPane.showMessageDialog((Component) e.getSource(), "Введите неотрицательное целое число!",
                            "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
                    newSize = "";
                }
            }
            while (newSize.isEmpty());
        }
    };
    
    /**
     * Обработчик изменения уровня ИИ
     */
    private ItemListener aiLevelChangedMenuClickHandler = new ItemListener()
    {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            JMenuItem rb = (JMenuItem)e.getItemSelectable();
            AILevel lvl = Arrays.stream(AILevel.values()).filter(x -> x.description.equalsIgnoreCase(rb.getText())).
                    findAny().orElse(AILevel.Unknown);
            if (lvl == AILevel.Unknown && !IS_DEBUG)
            {
                System.err.println("Обнаружен некорректный текст пункта меню выбора уровня ИИ!");
                System.err.println(rb.getText());
            }
            else
            {
                _aiLevel = lvl;
            }
        }
    };
    
    /**
     * Обработчик меню "Сменить скин"
     */
    private ItemListener skinChangedMenuClickHandler = new ItemListener()
    {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            try
            {
                JMenuItem rb = (JMenuItem)e.getItemSelectable();
                Skin skn = Skin.valueOf(rb.getText());
                //TODO: почему-то после смены стиля сбивается ширина пунктов меню - нужен принудительный autosize
                UIManager.setLookAndFeel(skn.getLAFClassPath());
                SwingUtilities.updateComponentTreeUI(_mainFrame);
            }
            catch (Exception ex)
            {
                String mes = MessageFormat.format("Смена скина невозможна (подробности в консоли): {0}{1}",
                        System.lineSeparator(), ex);
                System.err.println(mes);
                System.err.println();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(_mainFrame, mes, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    
    /**
     * Обработчик зажатия кнопки на компоненте - нужен для определения стартовой позиции курсора, с которой начинается
     *  перетаскивание окна
     *
     * @implNote почему-то получилось, только когда я разнёс по разным Прослушивателям обработку событий
     *          первоначального зажатия кнопки мыши и Перемещения мыши в зажатом состоянии.
     * */
    private MouseListener mouseDownHandler = new MouseAdapter()
    {
        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            // определяем начальную позицию курсора, с которой началось перетаскивание
            _mouseDownCursorPos = e.getLocationOnScreen();
        }
    };
    
    /**
     * Обработчик перетаскивания окна - нужен для организации перетаскивания окна за произвольные компоненты
     * */
    private MouseMotionListener mouseDragHandler = new MouseMotionListener()
    {
        @Override
        public void mouseDragged(MouseEvent e)
        {
            _mainFrame.setLocation(_mainFrame.getX() + (e.getXOnScreen() - _mouseDownCursorPos.x), _mainFrame.getY() +
                    + (e.getYOnScreen() - _mouseDownCursorPos.y));
            _mouseDownCursorPos = e.getLocationOnScreen();
        }
        
        @Override
        public void mouseMoved(MouseEvent e)
        {
        }
    };
    
    //endregion 'Обработчики'
    
    
}

