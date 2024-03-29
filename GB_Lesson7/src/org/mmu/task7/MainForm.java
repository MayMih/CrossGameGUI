package org.mmu.task7;

import org.mmu.task7.events.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * Класс Главной (и единственной) формы приложения
 */
public class MainForm
{
    
    /*
     * Модель данных для таблицы JTable
     * */
    static class CrossGameTableModel extends AbstractTableModel
    {
        @Override
        public int getRowCount()
        {
            return GameState.getCurrent().getBoardSize();
        }
    
        @Override
        public int getColumnCount()
        {
            return GameState.getCurrent().getBoardSize();
        }
    
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return GameState.getCurrent().getSymbolAt(rowIndex, columnIndex);
        }
    
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            GameState.getCurrent().setSymbolAt(rowIndex, columnIndex, aValue.toString().charAt(0));
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }
    
        public int getSize()
        {
            return GameState.getCurrent().getBoardSize();
        }
        
        public void setSize(int newSize)
        {
            GameState.getCurrent().setBoardSize(newSize);
        }
    
        public void clearCell(int cellNumber)
        {
            GameState.getCurrent().setSymbolAt(cellNumber, GameState.EMPTY_CELL_SYMBOL);
        }
    }
    
    
    //region 'Поля и константы'
    
    private JTable tableGameBoard;
    private JPanel pMain;
    private JLabel lbPlayerSymbol;
    private JLabel lbAISymbol;
    private JLabel lbAICaption;
    private JLabel lbPlayerCaption;
    
    private final JFrame _mainFrame = new JFrame();
    private JMenuItem _miChangeBoardSize;
    private ButtonGroup _bgAI;
    
    /**
     *  Определяет, будет ли выводиться в консоль дополнительная отладочная информация (например о перехвате клавиш)
     *
     * @apiNote     TODO: раз в Java нет понятия отладочной сборки, то логичнее выводить управление этой константой во
     *  внешний "мир" (параметры запуска в консоли/файл настоек/переменные окружения и т.п.)
     * */
    static boolean IS_DEBUG;
    
    /**
     * Путь к файлу с сохранённым состоянием Игры
     */
    public static final String GAMESTATE_DAT_FILE_PATH = "gamestate.dat";
    
    private static final Color BLUE_X_COLOR = new Color(0,92,231);
    private static final Color RED_ZERO_COLOR = new Color(159,0,0);
    private static final String ABOUT_TEXT;
    private static final String ICON_FILE_RESOURCE_NAME = "/icons/interface57.png";
    private static final Skin DEFAULT_LOOK_AND_FEEL = Skin.FlatIdea;
    private static final String BOARD_SIZE_CAPTION_START = "Размер поля:";
    private static final Package _pkg;
    
    private Point _mouseDownCursorPos;
    private boolean isStateLoaded;
    
    //endregion 'Поля и константы'
    
    
    /**
     *  Статический инициализатор - задаёт текст окна "О программе" и локализацию для кнопок диалоговых окон {@link #javax.swing.JOptionPane}
     */
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
        UIManager.put("OptionPane.yesButtonText"   , "Да"    );
        UIManager.put("OptionPane.noButtonText"    , "Нет"   );
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText"    , "ОК");
    }

    /**
     * Точка входа в программу - устанавливает скин GUI, создаёт главную форму игры
     * */
    public static void main(String[] args)
    {
        if (Arrays.stream(args).anyMatch(x -> {
            String arg = x.trim();
            return (arg.startsWith("-") || arg.startsWith("/")) && (x.trim().endsWith("debug") || x.trim().endsWith("d"));
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
                Thread.setDefaultUncaughtExceptionHandler(((t, ex) -> {
                    String mes = MessageFormat.format("Неожиданная ошибка программы \"{1}\"{0}{2}",
                            System.lineSeparator(), MainForm.class.getName(), ex.toString());
                    System.err.println(mes);
                    System.err.println();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    System.err.println(sw);
                    JEditorPane jep = new JEditorPane("text/html", "<html>" + mes + "<hr/><p>" + sw.toString().
                            replace(System.lineSeparator(),"<br/>") + "</p></html>");
                    jep.setEditable(false);
                    pw.close();
                    JOptionPane.showMessageDialog(null, jep, "Ошибка", JOptionPane.ERROR_MESSAGE);
                }));
                // Создаём нашу игровую фому и навешиваем на неё обработчики событий
                MainForm mf = new MainForm().setActionListeners();
                if (!mf.isStateLoaded)
                {
                    mf.actStartNewGame.actionPerformed(new ActionEvent(mf._mainFrame, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
    }
    
    /**
     * Конструктор главной формы - в т.ч. загружает сохранённое состояние игры
     **/
    public MainForm()
    {
        // загружаем сохранённое состояние Игры (если оно есть)
        try
        {
            if (Files.exists(Paths.get(GAMESTATE_DAT_FILE_PATH)))
            {
                GameState.getCurrent(GAMESTATE_DAT_FILE_PATH);
                isStateLoaded = true;
            }
        }
        catch (RuntimeException ex)
        {
            JOptionPane.showMessageDialog(_mainFrame, "Ошибка загрузки состояния Игры\n Игра будет начата сначала!\n" +
                ex, "Ошибка загрузки", JOptionPane.WARNING_MESSAGE);
        }
        String progName = _pkg.getImplementationTitle() + "";
        _mainFrame.setTitle(progName.isEmpty() || progName.equalsIgnoreCase("null") ? "# Крестики-нолики" : progName);
        _mainFrame.setContentPane(pMain);
        // по умолчанию при закрытии ничего не делаем - это нужно для того, чтобы можно было отобразить запрос к юзеру.
        _mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        _mainFrame.setLocationByPlatform(true);
        _mainFrame.setMinimumSize(new Dimension(400, 200));
        try
        {
            final ImageIcon img = new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_FILE_RESOURCE_NAME)));
            _mainFrame.setIconImage(img.getImage());
        }
        catch (Exception ex)
        {
            System.err.println("Не найден ресурс: " + ICON_FILE_RESOURCE_NAME);
        }
        createMainMenuFor(_mainFrame);                              // ОСТОРОЖНО: здесь есть обращение к синглтону Игры
        tableGameBoard.setModel(new CrossGameTableModel());
        tableGameBoard.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableGameBoard.setAutoCreateColumnsFromModel(true);
        tableGameBoard.setCellSelectionEnabled(false);
        tableGameBoard.setColumnSelectionAllowed(false);
        tableGameBoard.setRowSelectionAllowed(false);
        //tableGameBoard.setFillsViewportHeight(true);
        tableGameBoard.setShowGrid(true);
        tableGameBoard.setFont(lbPlayerSymbol.getFont());
        
        // Собственный класс-отрисовщик ячеек - нужен для центровки текста и вывода разным цветом символов Игрока и ПК
        //  - на основе кода отсюда: https://stackoverflow.com/a/35494391/2323972
        class CenterColouredRenderer extends DefaultTableCellRenderer
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component defaultRenderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((DefaultTableCellRenderer)defaultRenderer).setHorizontalAlignment(SwingConstants.CENTER);
                if (value != null && value.toString().charAt(0) == GameState.X_SYMBOL)
                {
                    defaultRenderer.setForeground(MainForm.BLUE_X_COLOR);
                }
                else
                {
                    defaultRenderer.setForeground(MainForm.RED_ZERO_COLOR);
                }
                return defaultRenderer;
            }
        }
        tableGameBoard.setDefaultRenderer(Object.class, new CenterColouredRenderer());
        
        ((CrossGameTableModel)tableGameBoard.getModel()).setSize(GameState.getCurrent().getBoardSize());
        tableGameBoard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.onBoardSizeUpdated();
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
        if (IS_DEBUG)
        {
            mGame.add(actCancelPLayerTurn);
        }
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
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(skn.name(), skn == DEFAULT_LOOK_AND_FEEL);
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
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(level.description, level == GameState.getCurrent().getAiLevel());
            rb.addItemListener(aiLevelChangedMenuClickHandler);
            _bgAI.add(rb);
            mAILevels.add(rb);
        }
        _miChangeBoardSize = mOptions.add(changeBoardSizeClickHandler);
        final String bSize = String.valueOf(GameState.getCurrent().getBoardSize());
        _miChangeBoardSize.setText(BOARD_SIZE_CAPTION_START + " " + bSize + "x" + bSize);
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
        jf.setJMenuBar(miniBar);
    }
    
    /**
     * Метод навешивания обработчиков на все элементы формы после запуска GUI
     *
     * @return Текущий экземпляр класса формы "code behind" - НЕ создаёт его - нужно для цепочечных операций.
     * */
    private MainForm setActionListeners()
    {
        // N.B. Этот т.н. "Указатель на метод" по сути является "синтаксическим сахаром" - на самом деле здесь
        //      используется автогенерация экземпляра интерфейса с указанным методом (поэтому, например, сюда нельзя
        //      передать метод пробрасывающий checked exception).
        GameState.getCurrent().addStateChangeListener(this::gameStateChangedHandler);
        // разрешаем таскать форму за любые не интерактивные элементы
        pMain.addMouseListener(this.mouseDownHandler);
        pMain.addMouseMotionListener(this.mouseDragHandler);
        // навешиваем обработчик закрытия формы (с подтверждением закрытия от юзера)
        _mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                actExitProgram.actionPerformed(new ActionEvent(_mainFrame, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        tableGameBoard.addMouseListener(tableDoubleClickHandler);
        return this;
    }
    
    /**
     * Метод обновления экрана после изменения размера игрового Поля - в т.ч. заставляет {@link JTable} перерисовать себя.
     * */
    private void onBoardSizeUpdated()
    {
        int bSize = GameState.getCurrent().getBoardSize();
        _miChangeBoardSize.setText(BOARD_SIZE_CAPTION_START + " " + bSize + "x" + bSize);
        ((CrossGameTableModel)tableGameBoard.getModel()).fireTableStructureChanged();
        TableColumn cl = null;
        for (Enumeration<TableColumn> cols = tableGameBoard.getColumnModel().getColumns(); cols.hasMoreElements(); )
        {
            cl = cols.nextElement();
            cl.setPreferredWidth(cl.getMinWidth() * 2);
        }
        assert cl != null;
        tableGameBoard.setRowHeight(cl.getMinWidth() * 2);
        _mainFrame.pack();
    }
    
    //endregion 'Методы'
    
    
    
    
    //region 'Обработчики'
    
    /**
     * Действие "Отменить ход Игрока"
     */
    private final Action actCancelPLayerTurn = new AbstractAction("Отменить ход (игрока и ПК)")
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            List<Integer> history = GameState.getCurrent().getPlayerTurnsHistory();
            if (history.isEmpty())
            {
                JOptionPane.showMessageDialog(tableGameBoard, "Игрок ещё не делал ходов - нечего отменять!",
                    "Отказ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CrossGameTableModel tm = ((CrossGameTableModel)tableGameBoard.getModel());
            tm.clearCell(history.remove(history.size() - 1));
            history = GameState.getCurrent().getCpuTurnsHistory();
            if (!history.isEmpty())
            {
                tm.clearCell(history.remove(history.size() - 1));
            }
            GameState.getCurrent().setStarted(true);
            JOptionPane.showMessageDialog(tableGameBoard, "Можете сделать новый ход!", "Ход отменён",
                JOptionPane.INFORMATION_MESSAGE);
        }
    };
    
    /**
     * Действие - "Новая игра" - может приводить к ходу ПК (если игрок выбрал "0" в качестве своего символа)
     * */
    private final Action actStartNewGame = new AbstractAction("Новая игра")
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            final Object[] NEW_GAME_OPTIONS = new Object[] {GameState.X_SYMBOL, GameState.ZERO_SYMBOL,
                                                                    "<html>Как в прошлый раз: '<b>" +
                                                                    GameState.getCurrent().getPlayerSymbol() + "</b>'</html>"
            };
            int res = -1;
            if (GameState.getCurrent().isStarted())
            {
                res = JOptionPane.showConfirmDialog(_mainFrame, "Вы уверены, что хотите начать игру сначала?",
                        "Подтверждение", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.OK_OPTION)
                {
                    return;
                }
            }
            GameState.getCurrent().Reset();
            res = JOptionPane.showOptionDialog(tableGameBoard, "<html>Выберите сторону ('<b>X</b>' ходит первым!)</html>",
                    "Начало игры", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    NEW_GAME_OPTIONS, GameState.X_SYMBOL);
            if (res < 0)
            {
                res = NEW_GAME_OPTIONS.length - 1;
            }
            final char answer = NEW_GAME_OPTIONS[res].toString().charAt(0);
            if (answer == GameState.X_SYMBOL || answer == GameState.ZERO_SYMBOL)
            {
                GameState.getCurrent().setPlayerSymbol(answer == GameState.X_SYMBOL);
            }
            if (GameState.getCurrent().getCpuSymbol() == GameState.X_SYMBOL)
            {
                GameState.getCurrent().makeCpuTurn();
            }
        }
    };
    
    /**
     * Действие выполняемое при закрытии программы (спрашивает подтверждение пользователя, если не "включена" отладка)
     * */
    private final Action actExitProgram = new AbstractAction("Выход")
    {
        /**
         * При выходе сохраняет текущее состояние Игры
         */
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (IS_DEBUG || JOptionPane.showConfirmDialog(_mainFrame, "Вы уверены, что хотите выйти (состояние игры будет сохранено)?",
                    "Выход из игры", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
            {
                // сохраняем состояние игры
                try (GameState gameState = GameState.getCurrent(); ObjectOutputStream stateSaver = new ObjectOutputStream(
                    Files.newOutputStream(Paths.get(GAMESTATE_DAT_FILE_PATH))))
                {
                    if (!gameState.isStarted())
                    {
                        // перед сохранением сбрасываем состояние Игры, если она находится в состоянии из которого Не может
                        // быть продолжена
                        gameState.Reset();
                    }
                    stateSaver.writeObject(gameState);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(_mainFrame, "Не удалось сохранить состояние игры: \n" +
                        ex.getMessage() + System.lineSeparator() + ex, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                    System.exit(-1);
                }
                System.exit(0);
            }
        }
    };
    
    /**
     * Обработчик клика на ячейку - производит ход игрока, затем проверяет, остались ли ходы и если есть - делает ход ПК.
     *
     * @implNote  Обрабатывается именно двойной клик, т.к. с одиночным были проблемы - иногда просто выделялась ячейка,
     *      но события клика не было.
     * */
    private final MouseListener tableDoubleClickHandler = new MouseAdapter()
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            if (e.getClickCount() < 2)
            {
                return;
            }
            if (!GameState.getCurrent().isStarted())
            {
                int res = JOptionPane.showConfirmDialog(e.getComponent(), "Игра окончена - хотите начать новую?",
                        "Новая игра", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (res == JOptionPane.YES_OPTION)
                {
                    actStartNewGame.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null));
                }
                return;
            }
            final int rowIndex = tableGameBoard.getSelectedRow();
            final int colIndex = tableGameBoard.getSelectedColumn();
            if (IS_DEBUG)
            {
                System.out.println("[" + (rowIndex + 1) + "][" + (colIndex + 1) + "] clicked");
            }
            if (!GameState.getCurrent().checkCoords(rowIndex, colIndex))
            {
                JOptionPane.showMessageDialog(e.getComponent(), "Эта клетка уже занята - выберите другую!",
                        "Этот ход невозможен", JOptionPane.WARNING_MESSAGE);
                return;
            }
            final char ps = GameState.getCurrent().getPlayerSymbol();
            tableGameBoard.setValueAt(ps, rowIndex, colIndex);
            GameState.getCurrent().getPlayerTurnsHistory().add(BoardUtils.convertCoordsToCellNumber(rowIndex, colIndex));
            if (IS_DEBUG)
            {
                GameState.getCurrent().printBoard();
            }
            if (GameState.getCurrent().checkWin(ps, rowIndex, colIndex))
            {
                JOptionPane.showMessageDialog(e.getComponent(), "Победил Игрок", "Игра окончена",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else if (GameState.getCurrent().noMoreWinMoves())
            {
                JOptionPane.showMessageDialog(e.getComponent(), "Ничья - ходов больше нет!", "Игра окончена",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else if (GameState.getCurrent().makeCpuTurn())
            {
                JOptionPane.showMessageDialog(e.getComponent(), "Победил ИИ", "Игра окончена",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else if (GameState.getCurrent().noMoreWinMoves())
            {
                JOptionPane.showMessageDialog(e.getComponent(), "Ничья - ходов больше нет!", "Игра окончена",
                    JOptionPane.INFORMATION_MESSAGE);
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
                Desktop.getDesktop().browse(e.getURL().toURI());
            }
            catch (Exception ex)
            {
                System.err.append("Error: Can't go to URL: \"").append(ex.getMessage()).println("\"");
                ex.printStackTrace();
            }
        }
    };
    
    /**
     * Обработчик пункта меню "Изменить размер Поля"
    * */
    private final Action changeBoardSizeClickHandler = new AbstractAction()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String newSize = "";
            do
            {
                newSize = JOptionPane.showInputDialog((Component) e.getSource(), "Введите новый размер поля (одно число)",
                        "Изменение размера игрового поля", JOptionPane.INFORMATION_MESSAGE);
                try
                {
                    if (newSize == null)    // пользователь отказался от ввода
                    {
                        newSize = String.valueOf(((CrossGameTableModel)tableGameBoard.getModel()).getSize());
                    }
                    else
                    {
                        int boardSize = Integer.parseUnsignedInt(newSize);
                        if (boardSize >= 3)
                        {
                            ((CrossGameTableModel)tableGameBoard.getModel()).setSize(boardSize);
                        }
                        else
                        {
                            newSize = "";
                            JOptionPane.showMessageDialog(_mainFrame, "Размер поля должен быть >= 3",
                                    "Размер не задан", JOptionPane.WARNING_MESSAGE);
                        }
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
            
            if (e.getStateChange() == ItemEvent.DESELECTED)
            {
                if (IS_DEBUG)
                {
                    System.out.println("Отключён уровень ИИ: " + rb.getText());
                }
                return;
            }
            else if (IS_DEBUG)
            {
                System.out.println("Включён уровень ИИ: " + rb.getText());
            }
            AILevel lvl = Arrays.stream(AILevel.values()).filter(x -> x.description.equalsIgnoreCase(rb.getText())).
                    findAny().orElse(AILevel.Unknown);
            if (lvl == AILevel.Unknown && !IS_DEBUG)
            {
                System.err.println("Обнаружен некорректный текст пункта меню выбора уровня ИИ!");
                System.err.println(rb.getText());
            }
            else
            {
                GameState.getCurrent().setAiLevel(lvl);
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
                // тут как всегда слетает ряд шрифтов (и не только) после применение скина
                // TODO: запоминать шрифты (все?) и применять их снова после смены скина
                SwingUtilities.updateComponentTreeUI(_mainFrame);
                tableGameBoard.setShowGrid(true);
            }
            catch (Exception ex)
            {
                String mes = MessageFormat.format("Смена скина невозможна (подробности в консоли): \n {1}", ex);
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
    
    /**
     * Обработчик изменения состояния игры (в т.ч. размер поля, уровень ИИ)
     * */
    private void gameStateChangedHandler(GameStateChangedEventBase e)
    {
        if (e instanceof BoardSizeChangedEvent)
        {
            this.onBoardSizeUpdated();
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
                    break;
                }
            }
        }
        else if (e instanceof PlayerSymbolChangedEvent)
        {
            final boolean isPlayerXSymbol = GameState.getCurrent().getPlayerSymbol() == GameState.X_SYMBOL;
            lbAISymbol.setText(String.valueOf(GameState.getCurrent().getCpuSymbol()));
            lbPlayerSymbol.setText(String.valueOf(GameState.getCurrent().getPlayerSymbol()));
            lbAISymbol.setForeground(isPlayerXSymbol ? RED_ZERO_COLOR : BLUE_X_COLOR);
            lbPlayerSymbol.setForeground(isPlayerXSymbol ? BLUE_X_COLOR : RED_ZERO_COLOR);
        }
        else if (e instanceof CpuTurnCompletedEvent)
        {
            CpuTurnCompletedEvent ctc = ((CpuTurnCompletedEvent) e);
            tableGameBoard.setValueAt(lbAISymbol.getText(), ctc.rowIndex, ctc.colIndex);
        }
    }
    
    //endregion 'Обработчики'
    
    
}

