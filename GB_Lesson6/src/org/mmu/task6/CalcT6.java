package org.mmu.task6;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

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
    
    static
    {
        IS_DEBUG = false;
    }
    
    /**
     * Набор возможных операций над числами
     * */
    enum Operation
    {
        None(' '),
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
    
    //private static final char _decimalSeparator = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
    private static final DecimalFormat _formatter = new DecimalFormat();
    private final Font _defaultDisplayFont;
    private final String _dot;
    private final CalculatorState _calcState = new CalculatorState();
    
    //endregion 'Поля и константы'
    
    
    
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
        //_formatter.setRoundingMode(Ro);
        _formatter.setMaximumFractionDigits(100);
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
                        if ("Nimbus".equals(info.getName()))
                        {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                    //UIManager.setLookAndFeel(new FlatLightLaf());
                }
                catch (Exception e)
                {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
                // Создаём нашу фому калькулятора
                if (IS_DEBUG)
                {
                    showThreadInfo();
                }
                new CalcT6().setActionListeners();
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
        final String signChangeHotkeyTooltip = "<html>Для смены знака нажмите <b><kbd>Ctrl</kbd> + (<kbd>-</kbd>)</b></html>";
        if (IS_DEBUG)
        {
            showThreadInfo();
        }
        _jf = new JFrame("Калькулятор");
        //_jf.setUndecorated(true);
        _jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _jf.setContentPane(this.mainPanel);
        //_jf.setSize(320, 480);
        //_jf.setPreferredSize(new Dimension(320, 240));
        _jf.setResizable(false);
        try
        {
            ImageIcon img = new ImageIcon(getClass().getResource("/basic16.png"));
            _jf.setIconImage(img.getImage());
        }
        catch (Exception ex)
        {
            System.err.println("Не найден ресурс: " + getClass().getResource("/basic16.png"));
        }
        
        // создаём Меню
        
        _miniBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Ещё...");
        mainMenu.setToolTipText("Реализует задание 1.3");
        JCheckBoxMenuItem miShowCalcState = new JCheckBoxMenuItem(infoPanelSwitcher);
        miShowCalcState.setState(pInfo.isVisible());
        mainMenu.add(miShowCalcState);
        JMenuItem miOpenConverter = mainMenu.add("Конвертер величин (Мили <-> Километры)");
        miOpenConverter.setToolTipText("Калькулятор работает с двумя параметрами, вводимыми пользователем в окна ввода");
        _miniBar.add(mainMenu);
        _miniBar.setToolTipText("Пс-с-с... Меня можно таскать за это место!");
        _jf.setJMenuBar(_miniBar);
        if (IS_DEBUG)
        {
            showThreadInfo("Перед показом окна");
        }
        //TODO: Опасное действие - желательно не выдумывать свои форматы (могут совпасть с чем-то), а использовать из ОС.
        //  Но тогда возникает проблема с обратным преобразование, т.к. Double.parseDouble() - выбрасывает исключение на запятой!
        //  Нужно пользоваться дополнительным классом-обёрткой NumberFormat
        _dot = btDot.getActionCommand();
        DecimalFormatSymbols dfs = _formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(btDot.getActionCommand().charAt(0));
        _formatter.setDecimalFormatSymbols(dfs);
        txtDisplay.setToolTipText(signChangeHotkeyTooltip);
        btMinus.setToolTipText(signChangeHotkeyTooltip);
        _defaultDisplayFont = txtDisplay.getFont();
        
        _jf.setLocationByPlatform(true);
        _jf.pack();
        _jf.setVisible(true);
        
        if (IS_DEBUG)
        {
            showThreadInfo("После показа окна");
        }
    }
    
    
    
    //region 'Обработчики'
    
    private Action infoPanelSwitcher = new AbstractAction("Показывать панель состояния")
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
                        txtDisplay.setText(_formatter.format(number));
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
    private ActionListener clickHandler = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object firedComp = e.getSource();
            // Кнопка "Равно (=)" - вычислить результат и показать на дисплее
            if (firedComp == btCalculate)
            {
                // реализуем механизм повтора последней операции при повторном нажатии (=)
                if (CalculatorState.getCurOperation() == Operation.Equality)
                {
                    CalculatorState.setCurOperation(CalculatorState.getLastOperation());
                    CalculatorState.setResult(calculate(true));
                }
                else
                {
                    CalculatorState.setResult(calculate());
                }
                CalculatorState.setLastOperation(CalculatorState.getCurOperation());
                CalculatorState.setCurOperation(Operation.Equality);
                txtDisplay.setText(_formatter.format(CalculatorState.getResult()));
            }
            else if (firedComp == btBackspace)
            {
                try
                {
                    String curText = txtDisplay.getText();
                    Double.parseDouble(curText);
                    if (curText.length() == 1)
                    {
                        txtDisplay.setText("0");
                    }
                    else
                    {
                        txtDisplay.setText(curText.substring(0, curText.length() - 1));
                    }
                }
                catch (Exception ex)
                {
                    // на дисплее не число - значит это операция - отменяем её
                    txtDisplay.setText(_formatter.format(CalculatorState.getOperandA()));
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
                txtDisplay.setText("0");
            }
            else if (firedComp == btClearDisplay)
            {
                // При удалении с экрана результата заменяем результат Первым операндом
                if (CalculatorState.getCurOperation() == Operation.Equality)
                {
                    CalculatorState.setResult(CalculatorState.getOperandA());
                }
                txtDisplay.setText("0");
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
                txtDisplay.setText(((JButton)firedComp).getActionCommand());
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
                txtDisplay.setText(res);
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
                ((JButton) b).addActionListener(this.clickHandler)
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
        // Обработчик обновления панели с историей вычислений
        //
        CalculatorState.addStateChangeListener(e -> {
            Double num = CalculatorState.getOperandA();
            String a = Double.isNaN(num) ? "" : _formatter.format(num).trim();
            num = CalculatorState.getOperandB();
            String b = Double.isNaN(num) ? "" : _formatter.format(num).trim();
            String r = _formatter.format(CalculatorState.getResult()).trim();
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
                    lbStatus.setText(" ");      // ставим пробел, т.к. при пустой строке панель пропадает с экрана?!
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
        
        //
        // Обработчик изменения текста в компоненте Дисплея - подгоняет размер шрифта под отображаемый текст
        //
        txtDisplay.getDocument().addDocumentListener(new DocumentListener()
        {
            private void handleTextUpdate()
            {
                Font testFont = _defaultDisplayFont;
                int strWidth = 0;
                String txt = txtDisplay.getText();
                FontMetrics fmetr = txtDisplay.getFontMetrics(testFont);
                do
                {
                    strWidth = SwingUtilities.computeStringWidth(fmetr, txt);
                    if (strWidth < (txtDisplay.getWidth() - txtDisplay.getInsets().left - txtDisplay.getInsets().right))
                    {
                        break;
                    }
                    testFont = testFont.deriveFont(testFont.getSize() - 1f);
                    fmetr = txtDisplay.getFontMetrics(testFont);
                }
                while (fmetr.getHeight() > 15);
                
                if (testFont != txtDisplay.getFont())
                {
                    txtDisplay.setFont(testFont);
                    System.gc();
                }
            }
        
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                handleTextUpdate();
            }
        
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                handleTextUpdate();
            }
        
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                //это к изменениям текста не относится - тут речь только об изменении каких-то стилей (см.:
                //  <a href=https://docs.oracle.com/javase/tutorial/uiswing/events/documentlistener.html></a>)
            }
        });
    }
    
    private double calculate()
    {
        return calculate(false);
    }
    /**
     * Метод расчёта для калькулятора
     *
     * @isRepeatLast    True - повторить предыдущую операцию с сохранённым Операндом_2
     *
     * */
    private double calculate(boolean isRepeatLast)
    {
        double result = 0;
        double operandA = isRepeatLast ? CalculatorState.getResult() : CalculatorState.getOperandA();
        double operandB = isRepeatLast ? CalculatorState.getOperandB() : Double.parseDouble(txtDisplay.getText().trim());
        try
        {
            switch (CalculatorState.getCurOperation())
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
            //
            CalculatorState.setOperandA(operandA);
            //
            CalculatorState.setOperandB(operandB);
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
