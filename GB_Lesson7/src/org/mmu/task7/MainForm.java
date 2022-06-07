package org.mmu.task7;

import org.mmu.task7.events.AILevelChangedEvent;
import org.mmu.task7.events.BoardSizeChangedEvent;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Random;


public class MainForm
{
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
        FlatIdea("Сторонний бесплатный стиль - светлая тема в духе ItelliJ Idea 2019.2+ - (минимум Java 8)",
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
    
    private JTable tableGameBoard;
    private JPanel pMain;
    private JLabel lbPlayerSymbol;
    private JLabel lbAISymbol;
    private JLabel lbAICaption;
    private JLabel lbPlayerCaption;
    
    private JFrame _mainFrame = new JFrame();
    private JMenuItem _miChangeBoardSize;
    private ButtonGroup _bgAI;
    
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
    private static final String BOARD_SIZE_CAPTION_START = "Размер поля:";
    private static final Package _pkg;
    private static final Random _rand = new Random();
    
    private Point _mouseDownCursorPos;
    
    //endregion 'Поля и константы'
    
    
    /**
     * Статический конструктор - задаёт текст окна "О программе"
     * */
    static
    {
        IS_DEBUG = false;
        _pkg = MainForm.class.getPackage();
        ABOUT_TEXT = MessageFormat.format("<html><body style='background: transparent'>" +
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
                    UIManager.setLookAndFeel(MainForm.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                }
                catch (Exception e)
                {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    System.err.println("Не удалось установить стиль интерфейса по умолчанию: " +
                            MainForm.DEFAULT_LOOK_AND_FEEL.name() + " Classpath: " + MainForm.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                    System.err.println();
                    System.err.println(e.toString());
                    e.printStackTrace();
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
                // Создаём нашу игровую фому и навешиваем на неё обработчики событий
                try
                {
                    new MainForm().setActionListeners();
                }
                catch (Exception ex)
                {
                    String mes = MessageFormat.format("Не удалось создать главный класс игры \"{1}\"{0}{2}",
                            System.lineSeparator(), MainForm.class.getName(), ex.toString());
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
    public MainForm()
    {
        String progName = _pkg.getImplementationTitle() + "";
        _mainFrame.setTitle(progName.isEmpty() || progName.equalsIgnoreCase("null") ? "# \"Крестики-нолики\"" : progName);
        _mainFrame.setContentPane(pMain);
        // по умолчанию при закрытии ничего не делаем, вроде как это нужно для того, чтобы можно было отобразить запрос
        //_mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
        TableModel dataModel = new AbstractTableModel()
        {
            @Override
            public int getRowCount()
            {
                return GameState.Current.getBoardSize();
            }
    
            @Override
            public int getColumnCount()
            {
                return GameState.Current.getBoardSize();
            }
    
            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                return GameState.Current.getSymbolAt(rowIndex, columnIndex);
            }
            
            /*
            * Возвращает содержимое клетки по её номеру
            * */
            public char getValueAt(int cellNumber)
            {
                int bSize = GameState.Current.getBoardSize();
                return GameState.Current.getSymbolAt(cellNumber / bSize, cellNumber % bSize);
            }
    
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };
        tableGameBoard.setModel(dataModel);
        tableGameBoard.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableGameBoard.setAutoCreateColumnsFromModel(true);
        tableGameBoard.setCellSelectionEnabled(false);
        tableGameBoard.setColumnSelectionAllowed(false);
        tableGameBoard.setRowSelectionAllowed(false);
        //tableGameBoard.setFillsViewportHeight(true);
        tableGameBoard.setShowGrid(true);
        createMainMenuFor(_mainFrame);
        lbAICaption.setMinimumSize(lbPlayerCaption.getSize());
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
        JMenu mGame = new JMenu("Игра");
        mGame.add(actStartNewGame);
        mGame.add(actExitProgram);
        miniBar.add(mGame);
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
        _bgAI = new ButtonGroup();
        for (Object lvl : (IS_DEBUG ? AILevel.values() : Arrays.stream(AILevel.values()).skip(1).toArray()))
        {
            AILevel level = (AILevel)lvl;
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(level.description, level == GameState.Current.getAiLevel());
            rb.addItemListener(aiLevelChangedMenuClickHandler);
            _bgAI.add(rb);
            mAILevels.add(rb);
        }
        _miChangeBoardSize = mOptions.add(changeBoardSizeClickHandler);
        _miChangeBoardSize.setText(BOARD_SIZE_CAPTION_START + " " + GameState.Current.getBoardSize() + "x" + GameState.Current.getBoardSize());
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
     * Метод навешивания обработчиков на все элементы формы после запуска GUI
     * */
    private void setActionListeners()
    {
        GameState.Current.addStateChangeListener(e -> {
            if (e instanceof BoardSizeChangedEvent)
            {
                this.updateBoardSize(((BoardSizeChangedEvent) e).size);
            }
            else if (e instanceof AILevelChangedEvent)
            {
                AILevel lvl = ((AILevelChangedEvent) e).aiLevel;
                Enumeration<AbstractButton> buttons = this._bgAI.getElements();
                for (int i = 0; i < this._bgAI.getButtonCount(); i++)
                {
                    AbstractButton ab = buttons.nextElement();
                    if (!ab.isSelected() && (ab instanceof JRadioButtonMenuItem) && ab.getText().equalsIgnoreCase(lvl.description))
                    {
                        ab.setSelected(true);
                    }
                }
            }
        });
        // разрешаем таскать форму за любые не интерактивные элементы
        pMain.addMouseListener(this.mouseDownHandler);
        pMain.addMouseMotionListener(this.mouseDragHandler);
        _mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                actExitProgram.actionPerformed(new ActionEvent(_mainFrame, ActionEvent.ACTION_PERFORMED, null));
            }
        });
    }
    
    /**
     * Метод установки нового размера игрового Поля
     * */
    private void updateBoardSize(int newSize)
    {
        int bSize = GameState.Current.getBoardSize();
        _miChangeBoardSize.setText(BOARD_SIZE_CAPTION_START + " " + bSize + "x" + bSize);
    }
    
    //endregion 'Методы'
    
    
    
    
    //region 'Обработчики'
    
    /**
     * Действие - "Новая игра"
     * */
    private final Action actStartNewGame = new AbstractAction("Новая игра")
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (GameState.Current.isStarted())
            {
                int res = JOptionPane.showConfirmDialog((Component) e.getSource(), "Вы уверены, что хотите начать игру сначала?",
                        "Подтверждение", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION)
                {
                    GameState.Current.Reset();
                }
                Object[] options = new Object[] { GameState.DEFAULT_PLAYER_SYMBOL, GameState.DEFAULT_CPU_SYMBOL };
                res = JOptionPane.showOptionDialog((Component) e.getSource(), "Выберите сторону (\"X\" ходит первым!)",
                        "Выбор стороны", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        options, GameState.DEFAULT_PLAYER_SYMBOL);
                GameState.Current.setPlayerSymbol((char)options[res]);
            }
        }
    };
    
    /**
     * Действие выполняемое при закрытии программы
     * */
    private final Action actExitProgram = new AbstractAction("Выход")
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (JOptionPane.showConfirmDialog(_mainFrame, "Вы уверены, что хотите выйти?","Выход из игры",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
            {
                System.exit(0);
            }
        }
    };
    
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
    private final Action changeBoardSizeClickHandler = new AbstractAction()
    {
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
                        newSize = String.valueOf(GameState.Current.getBoardSize());
                    }
                    else
                    {
                        int boardSize = Integer.parseUnsignedInt(newSize);
                        GameState.Current.setBoardSize(boardSize);
                        updateBoardSize(boardSize);
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
    private final ItemListener aiLevelChangedMenuClickHandler = new ItemListener()
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
                GameState.Current.setAiLevel(lvl);
            }
        }
    };
    
    /**
     * Обработчик меню "Сменить скин"
     */
    private final ItemListener skinChangedMenuClickHandler = new ItemListener()
    {
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            try
            {
                JMenuItem rb = (JMenuItem)e.getItemSelectable();
                Skin skn = Skin.valueOf(rb.getText());
                UIManager.setLookAndFeel(skn.getLAFClassPath());
                SwingUtilities.updateComponentTreeUI(_mainFrame);
                tableGameBoard.setShowGrid(true);
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
    private final MouseListener mouseDownHandler = new MouseAdapter()
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
    private final MouseMotionListener mouseDragHandler = new MouseMotionListener()
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

