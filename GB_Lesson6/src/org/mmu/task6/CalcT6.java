package org.mmu.task6;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
     * Набор возможных операций над числами
     * */
    enum Operation
    {
        None,
        /**
         * Умножить
         * */
        Add,
        /**
         * Вычесть
         * */
        Subtract,
        /**
         * Разделить
         * */
        Divide,
        /**
         * Умножить
         * */
        Multiply,
        /**
         * Возвести в степень
         * */
        Power
    }
    
    
    
    //region 'Поля и константы'
    
    private JPanel mainPanel;
    private JTextField txtDisplay;
    private JButton btClearCalcStack;
    private JButton btClearDisplay;
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
    
    private JMenuBar _miniBar;
    private Point _mouseDownCursorPos = new Point();
    private JFrame _jf;
    private double _operandA = 0;
    private Operation _curOperation = Operation.None;
    
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
    private final Font _defaultDisplayFont;
    
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
        showThreadInfo();
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // Устанавливаем более современный кросплатформенный "скин" Nimbus для интерфейса
                //  (см.: <a href=https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/nimbus.html></a>)
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                    JFrame.setDefaultLookAndFeelDecorated(true);
                }
                // Создаём нашу фому калькулятора
                showThreadInfo();
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
        showThreadInfo();
        _jf = new JFrame("Калькулятор");
        //_jf.setUndecorated(true);
        _jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        _jf.setContentPane(this.mainPanel);
        //_jf.setSize(320, 480);
        //_jf.setPreferredSize(new Dimension(320, 240));
        _jf.setResizable(false);
        ImageIcon img = new ImageIcon("./basic16.png");
        _jf.setIconImage(img.getImage());
        //txtDisplay.setMinimumSize(txtDisplay.getPreferredSize());
        _miniBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Инструменты");
        mainMenu.setToolTipText("Реализует задание 1.3");
        JMenuItem miOpenConverter = mainMenu.add("Конвертер величин");
        miOpenConverter.setToolTipText("Калькулятор работает с двумя параметрами, вводимыми пользователем в окна ввода");
        _miniBar.add(mainMenu);
        _miniBar.setToolTipText("Пс-с-с... Меня можно таскать за это место!");
        _jf.setJMenuBar(_miniBar);
        _jf.pack();
        _jf.setLocationByPlatform(true);
        showThreadInfo("Перед показом окна");
        txtDisplay.setToolTipText(signChangeHotkeyTooltip);
        btMinus.setToolTipText(signChangeHotkeyTooltip);
        //btDot.setActionCommand(Character.toString(_decimalSeparator));
        _defaultDisplayFont = txtDisplay.getFont();
        _jf.setVisible(true);
        showThreadInfo("После показа окна");
    }
    
    
    
    //region 'Обработчики'
    
    /**
     * Обработчик нажатия клавиш на форме
     */
    KeyListener keyPressedHandler = new KeyAdapter()
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            int keyCode = e.getKeyCode();
            if ((_validKeyCodes.contains(keyCode) || (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) ||
                    (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9)))
            {
                System.out.println("Обработка нажатия клавиши перехвачена:");
                System.out.println(keyCode + " '" + e.getKeyChar() + "' \"" + KeyEvent.getKeyText(keyCode) + "\"");
                
                if (keyCode == KeyEvent.VK_DELETE)
                {
                    if (e.isShiftDown() || e.isControlDown())
                    {
                        btClearCalcStack.doClick();
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
                        txtDisplay.setText(number == 0 ? "0" : Double.toString(number));
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
                if (keyCode != KeyEvent.VK_SHIFT && keyCode != KeyEvent.VK_CONTROL && keyCode != KeyEvent.VK_ALT)
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
     * Обработчик нажатия кнопок формы мышью
     *
     * @implNote    Если это кнопка с цифрой, то добавляем её к тексту поля ввода, иначе показываем знак операции, вместо
     *  текущего текста. В любом случае сначала запоминаем текущие показания "дисплея".
     * */
    ActionListener clickHandler = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object firedComp = e.getSource();
            // Кнопка "Равно (=)" - вычислить результат и показать на дисплее
            if (firedComp == btCalculate && (Double.isNaN(_operandA)))
            {
                double result = calculate();
                // подавляем огрехи форматирования
                if (!Double.isNaN(result))
                {
                    _operandA = result;
                    _curOperation = Operation.None;
                    txtDisplay.setText(result != 0 ? Double.toString(result) : "0");
                }
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
                    //txtDisplay.setText(Double.isNaN(_operandA) ? "0" : NumberFormat.getNumberInstance().format(_operandA));
                    txtDisplay.setText(Double.isNaN(_operandA) ? "0" : Double.toString(_operandA));
                    _curOperation = Operation.None;
                }
                //showThreadInfo(btBackspace.getText());
            }
            else if (firedComp == btClearCalcStack)
            {
                _operandA = Double.NaN;
                _curOperation = Operation.None;
                txtDisplay.setText("0");
            }
            else if (firedComp == btClearDisplay)
            {
                txtDisplay.setText("0");
            }
            else if (firedComp == btDot && txtDisplay.getText().contains(btDot.getActionCommand()))
            {
                return;
            }
            else if (firedComp == btPlus || firedComp == btMinus || firedComp == btStar || firedComp == btSlash || firedComp == btPower)
            {
                _curOperation = firedComp == btStar ? Operation.Multiply : (firedComp == btSlash ? Operation.Divide :
                        (firedComp == btMinus ? Operation.Subtract : (firedComp == btPlus ? Operation.Add : Operation.Power)));
                try
                {
                    _operandA = Double.parseDouble(txtDisplay.getText().trim());
                    txtDisplay.setText(((JButton)firedComp).getActionCommand());
                }
                catch (Exception ex)
                {
                    // обработка не требуется - видимо польз-ль повторно нажал клавишу с операцией
                }
            }
            // нажата кнопка с цифрой или точка
            else
            {
                String oldText = txtDisplay.getText().trim();
                String cmd = ((JButton)firedComp).getActionCommand();
                //txtDisplay.setText((oldText.equals("0") && !cmd.equalsIgnoreCase(Character.toString(_decimalSeparator)) ? "" : oldText) + cmd);
                String res = (oldText.equals("0") && !cmd.equalsIgnoreCase(btDot.getActionCommand()) ? "" : oldText) + cmd;
                txtDisplay.setText(res);
            }
        }
    };
    
    //endregion 'Обработчики'
    
    
    
    
    //region 'Методы'
    
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
        System.out.printf("Method: \"%s\", Thread: \"%s\", (id: %d)%n" , curThread.getStackTrace()[2].getMethodName(),
                curThread.getName(), curThread.getId());
    }
    
    /**
     * Метод навешивания обработчиков для компонентов
     * */
    private void setActionListeners()
    {
        showThreadInfo();
        Arrays.stream(mainPanel.getComponents()).filter(x -> x instanceof JButton).forEach(b -> ((JButton) b).addActionListener(
                this.clickHandler
        ));
        /*
          Обработчик поддержки перетаскивания окна за указанный компонент (строка меню)
         
          @implNote    почему-то получилось, только когда я разнёс по разным Прослушивателям обработку событий
         *  первоначального зажатия кнопки мыши и Перемещения мыши в зажатом состоянии.
         * */
        _miniBar.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                // определяем начальную позицию курсора, с которой началось перетаскивание
                _mouseDownCursorPos = e.getLocationOnScreen();
            }
        });
    
        /*
          Обработчик поддержки перетаскивания окна за указанный компонент (само перетаскивание)
          */
        _miniBar.addMouseMotionListener(new MouseMotionListener()
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
                FontMetrics fmeter = txtDisplay.getFontMetrics(testFont);
                do
                {
                    strWidth = SwingUtilities.computeStringWidth(fmeter, txt);
                    if (strWidth < (txtDisplay.getWidth() - txtDisplay.getInsets().left - txtDisplay.getInsets().right))
                    {
                        break;
                    }
                    testFont = testFont.deriveFont(testFont.getSize() - 1f);
                    fmeter = txtDisplay.getFontMetrics(testFont);
                }
                while (fmeter.getHeight() > 20);
                
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
        
        txtDisplay.addKeyListener(keyPressedHandler);
    }
    
    /**
     * Метод расчёта для калькулятора
     * */
    private double calculate()
    {
        double result = Double.NaN;
        double displayedNumber = Double.NaN;
        try
        {
            displayedNumber = Double.parseDouble(txtDisplay.getText().trim());
            switch (_curOperation)
            {
                case Add:
                {
                    result = _operandA + displayedNumber;
                    break;
                }
                case Divide:
                {
                    result = _operandA / displayedNumber;
                    break;
                }
                case Multiply:
                {
                    result = _operandA * displayedNumber;
                    break;
                }
                case Subtract:
                {
                    result = _operandA - displayedNumber;
                    break;
                }
                case Power:
                {
                    result = Math.pow(_operandA, displayedNumber);
                    double myResult = _operandA;
                    for (int i = 0; i < displayedNumber; i++)
                    {
                        myResult *= _operandA;
                    }
                    JOptionPane.showMessageDialog(btCalculate, "Результат вычисления без библиотечной функции: " + myResult);
                    break;
                }
                default:
                {
                    result = displayedNumber;
                }
            }
        }
        catch (NumberFormatException nex)
        {
            // действие не требуется
        }
        catch (Exception ex)
        {
            System.err.printf("Ошибка вычислений! Операнд 1: %s, Операнд 2: %s, Операция: %s", _operandA, displayedNumber, _curOperation);
            System.err.println();
            System.err.println(ex.toString());
        }
        return result;
    }
    
    //endregion 'Методы'
    
    
    
}
