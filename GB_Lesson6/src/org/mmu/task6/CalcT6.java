package org.mmu.task6;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

/**
 * 1. Разработать оконное приложение «Калькулятор»;
 *
 * @author mayur
 * @apiNote     Забыл научить работать калькулятор с отрицательными числами, точнее их нельзя ввести, т.к. не хватило
 *  места на форме для операции смены знака - но знак можно поменять комбинацией Ctrl + (-)!
 */
public class CalcT6
{
    /**
     *  Определяет, будет ли выводиться в консоль дополнительная отладочная информация (например о перехвате клавиш)
     *
     * @apiNote     TODO: раз в Java нет понятия отладочной сборки, то логичнее выводить управление этой константой во
     *  внешний "мир" (параметры запуска в консоли/файл настоек/переменные окружения и т.п.)
     * */
    private static final boolean IS_DEBUG;
    
    /**
     * Набор возможных операций над числами
     * */
    enum Operation
    {
        None('\u0000'),  // Возможный вариант " "
        /**
         * Специальная операция, которая позволяет отличить начальное состояние калькулятора от ситуации после Равенства
         * */
        Equality('='),
        /**
         * Умножить
         * */
        Add('+'),
        /**
         * Вычесть
         * */
        Subtract('-'),
        /**
         * Разделить
         * */
        Divide('/'),
        /**
         * Умножить
         * */
        Multiply('*'),
        /**
         * Возвести в степень
         * */
        Power('^');
        
        public final char title;
    
        Operation(char c)
        {
            title = c;
        }
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
    
    private JPanel mainPanel;
    private JTextField txtDisplay;
    private JButton btClearDisplay;
    private JButton btClearAll;
    private JButton btBackspace;
    private JButton btCalculate;
    private JButton btSeven;
    private JButton btEight;
    private JButton btNine;
    private JButton btPlus;
    private JButton btPower;
    private JButton btMinus;
    private JButton btStar;
    private JButton btSlash;
    private JButton btZero;
    private JButton btDot;
    private JPanel pInfo;
    private JLabel lbStatus;
    
    private JMenuBar _miniBar;
    private Point _mouseDownCursorPos = new Point();
    private JFrame _jf;
    private final JEditorPane _txtAbout;
    
    private static final List<Integer> _validKeyCodes = Arrays.asList(
            KeyEvent.VK_BACK_SLASH, KeyEvent.VK_SLASH,
            KeyEvent.VK_COMMA, KeyEvent.VK_DECIMAL, KeyEvent.VK_PERIOD,
            KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS,
            KeyEvent.VK_PLUS, KeyEvent.VK_ADD,
            KeyEvent.VK_ENTER, KeyEvent.VK_EQUALS,
            KeyEvent.VK_DELETE,
            KeyEvent.VK_BACK_SPACE,
            KeyEvent.VK_DIVIDE,
            KeyEvent.VK_MULTIPLY,
            KeyEvent.VK_COLON);
    
    private static final Skin DEFAULT_LOOK_AND_FEEL = Skin.Nimbus;
    private static final DecimalFormat _formatter = new DecimalFormat();
    private final Font _defaultDisplayFont;
    private final FontMetrics _defaultDisplayFontMetrics;
    private final HashMap<Object, Object> _buttonFonts;
    private final String _dot;
    private final CalculatorState _calcState = new CalculatorState();
    private static final String ABOUT_TEXT;
    private static final String SIGN_CHANGE_HOTKEY_TOOLTIP = "<html>Для смены знака нажмите <b><kbd>Ctrl</kbd> + (<kbd>-</kbd>)</b></html>";
    private static final String ICON_FILE_RESOURCE_NAME = "/basic16.png";
    private static final int MIN_TEXT_HEIGHT = 16;
    
    //endregion 'Поля и константы'
    
    
    
    static
    {
        IS_DEBUG = false;
        Package pkg = CalcT6.class.getPackage();
        ABOUT_TEXT = MessageFormat.format("<html><body style='background: transparent'>" +
                "<h1>{0}</h1><h2>Версия: {1}</h2><h3>Автор: {2}</h3><p>Создан в 2022 г. в рамках выполнения задания №6 " +
                "курса GeekBrains \"Основы Java. Интерактивный курс\"</p>" +
                "<p>В качестве возможных тем оформления интерфейса задействован открытый проект <b>FlatLaf</b> компании <i>FormDev</i></p>" +
                "<p>Ссылки: <a href=https://www.formdev.com/flatlaf/>FlatLaf - Flat Look and Feel</a></p>" +
                "<p>Лицензия на FlatLaf: <a href=https://github.com/JFormDesigner/FlatLaf/blob/master/LICENSE>Apache 2.0 License</a></p>" +
                "</body></html>", pkg.getSpecificationTitle(), pkg.getSpecificationVersion(), pkg.getSpecificationVendor());
    }
    
    
    /**
     * Точка входа - Создаём окно в отдельном, т.н. "потоке обработки событий" (вроде так принято в Swing)
     *
     * @implNote    Смысл запуска создания элементов интерфейса в отдельном потоке здесь не так очевиден, как в C# (хотя там в
     *     WinForms он толком и не возможен), т.к. Swing в некоторой степени потокобезопасен, хотя документация и не
     *     говорит об этом (множество методов имеют в своём составе конструкцию <code>{@link synchronized()}</code> и
     *         методы Swing почти никогда не проверяют из какой нити они запущены (см. {@link SwingUtilities#isEventDispatchThread()} )
     *         и соот-но не выбрасывают исключения как в C#), т.е. явно при многопоточном доступе к компоненту интерфейса
     *         ошибки возникать не будут, но возможна ситуация "гонки".
     *     <p>
     *     N.B.: Даже если создать интерфейс из основного потока, обработчики всё равно будут вызываться из доп. потока
     *     обработки событий созданного JVM автоматически.</p>
     * */
    public static void main(String[] args)
    {
        if (IS_DEBUG)
        {
            showThreadInfo();
        }
        _formatter.setDecimalSeparatorAlwaysShown(false);
        _formatter.setRoundingMode(RoundingMode.HALF_UP);
        _formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
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
                    //UIManager.setLookAndFeel(new FlatLightLaf());
                    UIManager.setLookAndFeel(CalcT6.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                }
                catch (Exception e)
                {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    System.err.println("Не удалось установить стиль интерфейса по умолчанию: " +
                            CalcT6.DEFAULT_LOOK_AND_FEEL.name() + " Classpath: " + CalcT6.DEFAULT_LOOK_AND_FEEL.getLAFClassPath());
                    System.err.println();
                    System.err.println(e.toString());
                    e.printStackTrace();
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
                // Создаём нашу фому калькулятора
                if (IS_DEBUG)
                {
                    showThreadInfo();
                }
                try
                {
                    new CalcT6().setActionListeners();
                }
                catch (Exception ex)
                {
                    String mes = MessageFormat.format("Не удалось создать главный класс программы \"{1}\"{0}{2}",
                            System.lineSeparator(), CalcT6.class.getName(), ex.toString());
                    System.err.println(mes);
                    System.err.println();
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, mes, "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    /**
     * Конструктор главного окна приложения
     *
     * @apiNote В каталоге, указанном в качестве рабочего должна лежать иконка приложения "basic16.png"
     *
     * @implNote Поля "главного" класса {@link CalcT6} соот-щие графическим компонентам (вроде {@link CalcT6#mainPanel})
     * генерируются автоматически средой IntelliJ Idea (в момент компиляции) на основе xml-файла <b>CalcT6.form</b>,
     * т.о. вручную остаётся только создать главное окно программы.
     *
     */
    public CalcT6()
    {
        if (IS_DEBUG)
        {
            showThreadInfo();
        }
        _jf = new JFrame("Калькулятор");
        //_jf.setUndecorated(true);
        _jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _jf.setContentPane(this.mainPanel);
        _jf.setResizable(false);
        try
        {
            ImageIcon img = new ImageIcon(Objects.requireNonNull(getClass().getResource(ICON_FILE_RESOURCE_NAME)));
            _jf.setIconImage(img.getImage());
        }
        catch (Exception ex)
        {
            System.err.println("Не найден ресурс: " + ICON_FILE_RESOURCE_NAME);
        }
        
        // создаём Меню
        
        _miniBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Ещё...");
        mainMenu.setToolTipText("Реализует задание 1.3");
        JCheckBoxMenuItem miShowCalcState = new JCheckBoxMenuItem(showInfoPanelMenuClickHandler);
        miShowCalcState.setState(pInfo.isVisible());
        mainMenu.add(miShowCalcState);
        JMenu skinsMenu = new JMenu("Скин");
        ButtonGroup group = new ButtonGroup();
        for (Skin skn : Skin.values())
        {
            JRadioButtonMenuItem rb = new JRadioButtonMenuItem(skn.name(), skn == this.DEFAULT_LOOK_AND_FEEL);
            rb.setToolTipText(skn.toolTip);
            rb.addItemListener(skinChangeMenuClickHandler);
            group.add(rb);
            skinsMenu.add(rb);
        }
        mainMenu.add(skinsMenu);
        JMenuItem miOpenConverter = mainMenu.add(new AbstractAction("Конвертер величин (Мили <-> Километры)")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog((Component) e.getSource(), "Задание: \"Калькулятор работает с " +
                        "двумя параметрами, вводимыми пользователем в окна ввода\" пока не реализовано, но полагаю " +
                        "я с лихвой это компенсировал объёмом самого калькулятора", "Ой", JOptionPane.WARNING_MESSAGE);
            }
        });
        mainMenu.add(aboutMenuClickHandler);
        _miniBar.add(mainMenu);
        _miniBar.setToolTipText("Пс-с-с... Меня можно таскать за это место!");
        _jf.setJMenuBar(_miniBar);
        if (IS_DEBUG)
        {
            showThreadInfo("Перед показом окна");
        }
        //TODO: Опасное действие - желательно не выдумывать свои форматы (могут совпасть с чем-то), а использовать из ОС.
        //  Но тогда возникает проблема с обратным преобразование, т.к. Double.parseDouble() - выбрасывает исключение на запятой!
        //  Нужно пользоваться дополнительным классом-обёрткой NumberFormat, что сильно удлиняет вызовы.
        _dot = btDot.getActionCommand();
        DecimalFormatSymbols dfs = _formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(_dot.charAt(0));
        _formatter.setDecimalFormatSymbols(dfs);
        txtDisplay.setToolTipText(SIGN_CHANGE_HOTKEY_TOOLTIP);
        btMinus.setToolTipText(SIGN_CHANGE_HOTKEY_TOOLTIP);
        _defaultDisplayFont = txtDisplay.getFont();
        _defaultDisplayFontMetrics = txtDisplay.getFontMetrics(_defaultDisplayFont);
        //HACK: не считаем кол-во кнопок, зная, что единственный компонент НЕкнопка в {@link mainPanel} - это {@link txtDisplay}
        _buttonFonts = new HashMap<>(mainPanel.getComponentCount() - 1);
        // запоминаем шрифты всех кнопок, т.к. они почему-то сбиваются после смены интерфейса
        Arrays.stream(mainPanel.getComponents()).filter(x -> x instanceof JButton).forEach(bt -> _buttonFonts.put(bt, bt.getFont()));
        // Не редактируемое поле ввода для диалога "О программе"
        _txtAbout = new JEditorPane("text/html", ABOUT_TEXT);
        _txtAbout.setEditable(false);
        //TODO: похоже установка фона полей ввода не срабатывает для снина Nimbus
        // возможное решение: {@see https://stackoverflow.com/a/33446134/2323972}
        _txtAbout.setBackground(lbStatus.getBackground());
        
        _jf.setLocationByPlatform(true);
        _jf.pack();
        _jf.setVisible(true);
        
        if (IS_DEBUG)
        {
            showThreadInfo("После показа окна");
        }
    }
    
    
    
    //region 'Обработчики'
    
/**
     * Обработчик меню "Сменить скин"
     */
    private ItemListener skinChangeMenuClickHandler = new ItemListener()
    {
        /**
         * Invoked when an item has been selected or deselected by the user.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         */
        @Override
        public void itemStateChanged(ItemEvent e)
        {
            try
            {
                JMenuItem rb = (JMenuItem)e.getItemSelectable();
                Skin skn = Skin.valueOf(rb.getText());
                //TODO: почему-то после смены стиля теряются настройки шрифтов {@link txtDisplay} и всех кнопок?!
                UIManager.setLookAndFeel(skn.getLAFClassPath());
                SwingUtilities.updateComponentTreeUI(_jf);
                txtDisplay.setFont(_defaultDisplayFont);
                Arrays.stream(mainPanel.getComponents()).filter(x -> x instanceof JButton).forEach(bt ->
                        bt.setFont((Font)_buttonFonts.get(bt)));
            }
            catch (Exception ex)
            {
                String mes = MessageFormat.format("Смена скина невозможна (подробности в консоли): {0}{1}",
                        System.lineSeparator(), ex);
                System.err.println(mes);
                System.err.println();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(_jf, mes, "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    
    /**
     * Обработчик меню "О программе"
     *
     * @implNote    Решение взято отсюда <a href="https://stackoverflow.com/q/8348063/2323972">clickable links in JOptionPane</a>
     * */
    private Action aboutMenuClickHandler = new AbstractAction("О программе")
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
           JOptionPane.showMessageDialog((Component) e.getSource(), _txtAbout,"О программе", JOptionPane.INFORMATION_MESSAGE);
        }
    };
    
    private Action showInfoPanelMenuClickHandler = new AbstractAction("Показывать панель состояния")
    {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e)
        {
            pInfo.setVisible(!pInfo.isVisible());
            _jf.pack();
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
            _jf.setLocation(_jf.getX() + (e.getXOnScreen() - _mouseDownCursorPos.x), _jf.getY() + + (e.getYOnScreen() - _mouseDownCursorPos.y));
            _mouseDownCursorPos = e.getLocationOnScreen();
        }
        
        @Override
        public void mouseMoved(MouseEvent e)
        {
        }
    };
    
    /**
     * Обработчик нажатия клавиш на форме
     */
    private KeyListener keyPressedHandler = new KeyAdapter()
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            if ((_validKeyCodes.contains(keyCode) || (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) ||
                    (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9)))
            {
                if (IS_DEBUG)
                {
                    System.out.println("Обработка нажатия клавиши перехвачена:");
                    System.out.println(keyCode + " '" + e.getKeyChar() + "' \"" + KeyEvent.getKeyText(keyCode) + "\"");
                }
                if (keyCode == KeyEvent.VK_DELETE)
                {
                    if (e.isShiftDown() || e.isControlDown())
                    {
                        btClearAll.doClick();
                    }
                    else
                    {
                        btClearDisplay.doClick();
                    }
                    return;
                }
                // подменяем коды на те, что прописаны в качестве полей {@link JButton#getActionCommand()}
                if (keyCode == KeyEvent.VK_ENTER)
                {
                    //TODO: Возможно здесь стоит не подменять символ, а сразу вызывать обработчик нужной кнопки - это
                    // будет быстрее, но менее универсально
                    e.setKeyChar('=');
                }
                else if (keyCode == KeyEvent.VK_COMMA || keyCode == KeyEvent.VK_DECIMAL || keyCode == KeyEvent.VK_PERIOD)
                {
                    //e.setKeyChar(_decimalSeparator);
                    e.setKeyChar('.');
                }
                else if (keyCode == KeyEvent.VK_BACK_SPACE)
                {
                    e.setKeyChar('<');
                }
                else if (keyCode == KeyEvent.VK_COLON || ((KeyEvent.getKeyText(keyCode).equals("6")) &&
                        (e.isShiftDown() || e.isControlDown())))
                {
                    e.setKeyChar('^');
                }
                else if ((keyCode == KeyEvent.VK_MINUS || keyCode == KeyEvent.VK_SUBTRACT) && e.isControlDown())
                {
                    try
                    {
                        double number = -Double.parseDouble(txtDisplay.getText().trim());
                        setDisplayText(_formatter.format(number));
                    }
                    catch (Exception ex)
                    {
                        // обработка не требуется - польз-ль попытался изменить знак НЕчисла
                    }
                    return;
                }
                // При создании кнопки на основе её текста ей автоматом задаётся некая {@link actionCommand}
                //  её мы и используем для поиска той кнопки, что нужно активировать (некоторые кнопки переопределены вручную,
                //      как, например, возведение в степень, в качестве текста которой используется HTML, а в actionCommand задан символ '^').
                Optional<Component> btn = Arrays.stream(mainPanel.getComponents()).filter(x -> (x instanceof JButton) &&
                        ((JButton)x).getActionCommand().trim().charAt(0) == e.getKeyChar()).findFirst();
                btn.ifPresent(bt -> ((JButton) bt).doClick());
            }
            else
            {
                if (IS_DEBUG && keyCode != KeyEvent.VK_SHIFT && keyCode != KeyEvent.VK_CONTROL && keyCode != KeyEvent.VK_ALT)
                {
                    System.out.println("Обработка нажатия клавиши передана компоненту: " + super.getClass().getName());
                    keyCode = e.getExtendedKeyCode();
                    System.out.println(keyCode + " '" + e.getKeyChar() + "' \"" + KeyEvent.getKeyText(keyCode) + "\"");
                }
                super.keyPressed(e);
            }
        }
    };
    
    /**
     * Обработчик нажатия кнопок формы мышью - здесь происходит основная работа Калькулятора
     *
     * @implNote    Если это кнопка с цифрой, то добавляем её к тексту поля ввода, иначе показываем знак операции, вместо
     *  текущего текста. В любом случае сначала запоминаем текущие показания "дисплея".
     * */
    private ActionListener buttonClickHandler = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object firedComp = e.getSource();
            // Кнопка "Равно (=)" - вычислить результат и показать на дисплее
            if (firedComp == btCalculate)
            {
                if (CalculatorState.getCurOperation() == Operation.None)
                {
                    return;
                }
                double operandA = CalculatorState.getOperandA(), operandB = CalculatorState.getOperandB();
                Operation op = CalculatorState.getCurOperation();
                // реализуем механизм повтора последней операции при повторном нажатии (=)
                if (op == Operation.Equality)
                {
                    operandA = CalculatorState.getResult();
                    CalculatorState.setCurOperation(CalculatorState.getLastOperation());
                    op = CalculatorState.getCurOperation();
                }
                else
                {
                    try
                    {
                        operandB = _formatter.parse(txtDisplay.getText()).doubleValue();
                    }
                    catch (Exception ex)
                    {
                        // если (=) нажали сразу после ввода знака операции, то её нужно применить к первому операнду
                        operandB = operandA;
                    }
                }
                CalculatorState.setOperandA(operandA);
                CalculatorState.setOperandB(operandB);
                CalculatorState.setResult(calculate(operandA, operandB, op));
                CalculatorState.setLastOperation(op);
                CalculatorState.setCurOperation(Operation.Equality);
                setDisplayText(_formatter.format(CalculatorState.getResult()));
            }
            else if (firedComp == btBackspace)
            {
                try
                {
                    String curText = txtDisplay.getText();
                    Double.parseDouble(curText);
                    if (curText.length() == 1)
                    {
                        setDisplayText("0");
                    }
                    else
                    {
                        setDisplayText(curText.substring(0, curText.length() - 1));
                    }
                }
                catch (Exception ex)
                {
                    // на дисплее не число - значит это операция - отменяем её
                    setDisplayText(_formatter.format(CalculatorState.getOperandA()));
                    CalculatorState.setCurOperation(Operation.None);
                }
                if (IS_DEBUG)
                {
                    showThreadInfo(btBackspace.getText());
                }
            }
            else if (firedComp == btClearAll)
            {
                CalculatorState.clearState();
                setDisplayText("0");
            }
            else if (firedComp == btClearDisplay)
            {
                // При удалении с экрана результата заменяем результат Первым операндом
                if (CalculatorState.getCurOperation() == Operation.Equality)
                {
                    CalculatorState.setResult(CalculatorState.getOperandA());
                }
                setDisplayText("0");
            }
            else if (firedComp == btDot && txtDisplay.getText().contains(btDot.getActionCommand()))
            {
                return;
            }
            else if (firedComp == btPlus || firedComp == btMinus || firedComp == btStar || firedComp == btSlash || firedComp == btPower)
            {
                CalculatorState.setOperandB(Double.NaN);
                CalculatorState.setCurOperation(firedComp == btStar ? Operation.Multiply : (firedComp == btSlash ? Operation.Divide :
                        (firedComp == btMinus ? Operation.Subtract : (firedComp == btPlus ? Operation.Add : Operation.Power))));
                try
                {
                    CalculatorState.setOperandA(Double.parseDouble(txtDisplay.getText().trim()));
                }
                catch (Exception ex)
                {
                    // обработка не требуется - видимо польз-ль повторно нажал клавишу с операцией
                }
                setDisplayText(((JButton)firedComp).getActionCommand());
            }
            // нажата кнопка с цифрой или точка
            else
            {
                CalculatorState.setOperandB(Double.NaN);
                String key = ((JButton)firedComp).getActionCommand();
                String oldText = CalculatorState.getCurOperation() == Operation.Equality ? "" : txtDisplay.getText().trim();
                String res;
                // если на экране число, то нажатую цифру нужно дописать к нему (если это не результат вычислений, тогда экран нужно заменять)
                try
                {
                    Double num = Double.parseDouble(oldText);
                    if (num == 0 && key.equalsIgnoreCase(_dot))
                    {
                        res = "0" + key;
                    }
                    else
                    {
                        res = ((num != 0) || oldText.equalsIgnoreCase("0" + _dot)) ? oldText + key : key;
                    }
                }
                catch (Exception ex)
                {
                    res = key.equalsIgnoreCase(_dot) ? "0" + key : key;
                }
                finally
                {
                    if (CalculatorState.getCurOperation() == Operation.Equality)
                    {
                        CalculatorState.setCurOperation(Operation.None);
                    }
                }
                setDisplayText(res);
            }
        }
    };
    
    //endregion 'Обработчики'
    
    
    
    
    //region 'Методы'
    
    /**
     * Метод навешивания обработчиков для компонентов
     * */
    private void setActionListeners()
    {
        if (IS_DEBUG)
        {
            showThreadInfo();
        }
        txtDisplay.addKeyListener(keyPressedHandler);
        
        // навешиваем обработчики на все кнопки
        Arrays.stream(mainPanel.getComponents()).filter(x -> x instanceof JButton).forEach(b ->
                ((JButton) b).addActionListener(this.buttonClickHandler)
        );
        /*
          Обработчик поддержки перетаскивания окна за указанный компонент (строка меню)
         * */
        _miniBar.addMouseListener(mouseDownHandler);
        pInfo.addMouseListener(mouseDownHandler);
        /*
          Обработчик поддержки перетаскивания окна за указанный компонент (само перетаскивание)
          */
        _miniBar.addMouseMotionListener(mouseDragHandler);
        pInfo.addMouseMotionListener(mouseDragHandler);
        //
        // Обработчик гиперссылок диалога "О программе"
        //
        _txtAbout.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                {
                    try
                    {
                        Desktop.getDesktop().browse(e.getURL().toURI()); // roll your own link launcher or use Desktop if J6+
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });
        //
        // Обработчик обновления панели с историей вычислений
        //
        CalculatorState.addStateChangeListener(e -> {
            Double num = CalculatorState.getOperandA();
            String a = Double.isNaN(num) ? "" : String.format(Locale.ROOT,"%.3g", num); //_formatter.format(num).trim();
            num = CalculatorState.getOperandB();
            String b = Double.isNaN(num) ? "" : String.format(Locale.ROOT,"%.3g", num); //_formatter.format(num).trim();
            num = CalculatorState.getResult();
            String r = String.format(Locale.ROOT,"%.3g", num); //_formatter.format(CalculatorState.getResult()).trim();
            Operation op = CalculatorState.getCurOperation();
            Operation lastOp = CalculatorState.getLastOperation();
            
            if (e.isOperationChange)
            {
                if (op == Operation.Equality && lastOp != Operation.None)
                {
                    lbStatus.setText(a + " " + CalculatorState.getLastOperation().title + " " + b + " " + op.title);
                }
                else if (op == Operation.None)
                {
                    lbStatus.setText(Character.toString(op.title));
                }
                else
                {
                    lbStatus.setText(r + " " + op.title);
                }
            }
            else if (op != Operation.Equality && op != Operation.None)
            {
                lbStatus.setText(a + " " + op.title + " " + b);
            }
        });
    }
    
    /**
     * Установка текста отображаемого на дисплее должна происходить только через этот метод (чтобы подогнать отображаемое
     *  число под размер дисплея)
     *
     * @param isScientificFormatAllowed    True - Разрешить преобразование числа в научный формат, если оно не помещается.
     * */
    private void setDisplayText(String txt, boolean isScientificFormatAllowed)
    {
        Font fnt = getOptimizedFont(txt);
        if (fnt != null)
        {
            if (fnt != txtDisplay.getFont())
            {
                txtDisplay.setFont(fnt);
                System.gc();
            }
            txtDisplay.setText(txt);
        }
        else if (isScientificFormatAllowed)
        {
            try
            {
                //txtDisplay.setText(String.format(Locale.ROOT, "%g", _formatter.parse(txt)));
                setDisplayText(String.format(Locale.ROOT, "%g", _formatter.parse(txt)), false);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                txtDisplay.setText(txt);
            }
        }
        else
        {
            txtDisplay.setText(txt);
        }
    }
    
    private void setDisplayText(String txt)
    {
        setDisplayText(txt, true);
    }
    
    /**
     * Возвращает шрифт такого размера, что позволит полностью уместить число на дисплее
     *
     * @return  Подходящий шрифт или Null, если расчётная высота шрифта меньше {@link CalcT6#MIN_TEXT_HEIGHT}
     * */
    public Font getOptimizedFont(String text)
    {
        Font testFont = _defaultDisplayFont;
        int strWidth = 0;
        FontMetrics fmetr = _defaultDisplayFontMetrics;
        do
        {
            strWidth = SwingUtilities.computeStringWidth(fmetr, text);
            if (strWidth < (txtDisplay.getWidth() - txtDisplay.getInsets().left - txtDisplay.getInsets().right))
            {
                break;
            }
            testFont = testFont.deriveFont(testFont.getSize() - 1f);
            fmetr = txtDisplay.getFontMetrics(testFont);
        }
        while (fmetr.getHeight() > MIN_TEXT_HEIGHT);
        if (fmetr.getHeight() <= MIN_TEXT_HEIGHT)
        {
            testFont = null;
        }
        return testFont;
    }

    
    /**
     * Метод расчёта для калькулятора
     *
     * */
    private double calculate(double operandA, double operandB, Operation mathOperation)
    {
        double result = 0;
        try
        {
            switch (mathOperation)
            {
                case Add:
                {
                    result = operandA + operandB;
                    break;
                }
                case Divide:
                {
                    result = operandA / operandB;
                    break;
                }
                case Multiply:
                {
                    result = operandA * operandB;
                    break;
                }
                case Subtract:
                {
                    result = operandA - operandB;
                    break;
                }
                case Power:     // при возведении в степень на экране будет значение библиотечной функции
                {
                    result = Math.pow(operandA, operandB);
                    double myResult = operandA;
                    for (int i = 0; i < operandB - 1; i++)
                    {
                        myResult *= operandA;
                    }
                    String mes = MessageFormat.format("<html><p>Результат вычисления через функцию <i>Math.pow()</i>: <b>{0}</b></p>" +
                                    "<p>Результат вычисления без библиотечной функции: <b>{0}</b></p></html>", result, myResult);
                    JOptionPane.showMessageDialog(btCalculate, mes,"Результат", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
                case None:
                {
                    break;
                }
                default:
                {
                    JOptionPane.showMessageDialog(btCalculate, "Неизвестная операция " + CalculatorState.getCurOperation(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        catch (NumberFormatException nex)
        {
            // действие не требуется
        }
        catch (Exception ex)
        {
            System.err.printf("Ошибка вычислений! Операнд 1: %s, Операнд 2: %s, Операция: %s", operandA, operandB,
                    CalculatorState.getCurOperation());
            System.err.println();
            System.err.println(ex.toString());
        }
        return result;
    }
    
    /**
     * Вспомогательный метод вывода в консоль информации о текущем потоке выполнения
     * */
    private static void showThreadInfo()
    {
        showThreadInfo("");
    }
    /**
     * Вспомогательный метод вывода в консоль информации о текущем потоке выполнения с произвольным сообщением
     * */
    private static void showThreadInfo(String text)
    {
        Thread curThread = Thread.currentThread();
        if (!text.isEmpty())
        {
            System.out.printf("Сообщение: \"%s\" - ", text);
        }
        System.out.printf("Method: \"%s\", Thread: \"%s\", (id: %d)%n" , curThread.getStackTrace()[3].getMethodName(),
                curThread.getName(), curThread.getId());
    }
    
    //endregion 'Методы'
    
    
    
}
