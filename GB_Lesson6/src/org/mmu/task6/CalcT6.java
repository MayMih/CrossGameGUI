package org.mmu.task6;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 1. Разработать оконное приложение «Калькулятор»;
 *
 * @apiNote     Забыл научить работать калькулятор с отрицательными числами, точнее их нельзя ввести, т.к. не хватило
 *  места для операции смены знака!
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
    
    //endregion 'Поля и константы'
   
    
    
    /**
     * Создаём окно в отдельном, т.н. "потоке обработки событий" (вроде так принято в Swing)
     *
     * */
    public static void main(String[] args)
    {
        System.out.println(Thread.currentThread().getName() + " : " + Thread.currentThread().getId());
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
                System.out.println(Thread.currentThread().getName() + " : " + Thread.currentThread().getId());
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
     * генерируются автоматически средой IntelliJ Idea на основе xml-файла <b>CalcT6.form</b>, т.о. вручную остаётся только
     * создать главное окно программы.
     *
     */
    public CalcT6()
    {
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
        _jf.setVisible(true);
    }
    
    
    /**
     * Обработчик нажатия кнопок
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
                double number;
                try
                {
                    String curText = txtDisplay.getText();
                    number = Double.parseDouble(curText);
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
                    txtDisplay.setText(Double.toString(_operandA));
                    _curOperation = Operation.None;
                }
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
            else if (firedComp == btDot && txtDisplay.getText().contains("."))
            {
                return;
            }
            else if (firedComp == btPlus || firedComp == btMinus || firedComp == btStar || firedComp == btSlash || firedComp == btPower)
            {
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
            }
            // нажата кнопка с цифрой или точка
            else
            {
                String oldText = txtDisplay.getText().trim();
                txtDisplay.setText((oldText.equals("0") ? "" : oldText) + ((JButton)firedComp).getActionCommand());
            }
        }
    };
    
    /**
     * Метод навешивания обработчиков для компонентов
     * */
    private void setActionListeners()
    {
        Arrays.stream(mainPanel.getComponents()).filter(x -> x instanceof JButton).forEach(b -> ((JButton) b).addActionListener(
            this.clickHandler
        ));
        
        /**
         * Обработчик поддержки перетаскивания окна за указанный компонент (строка меню)
         *
         * @implNote    почему-то получилось, только когда я разнёс по разным Прослушивателям обработку событий
         *  первоначального зажатия кнопки мыши и Перемещения мыши в зажатом состоянии.
         * */
        _miniBar.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                _mouseDownCursorPos = e.getLocationOnScreen();
            }
        });
    
        /**
         * Обработчик поддержки перетаскивания окна за указанный компонент
         * */
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
        
        /**
         * Обработчик нажатия клавиш на форме
         */
        txtDisplay.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int keyCode = e.getKeyCode();
                if ((_validKeyCodes.contains(keyCode) || (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) ||
                        (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9)))
                {
                    System.out.println(keyCode + " '" + e.getKeyChar() + "' \"" + KeyEvent.getKeyText(keyCode) + "\"");
                    // подменяем коды на те, что прописаны в качестве полей {@link JButton#getActionCommand()}
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
                    if (keyCode == KeyEvent.VK_ENTER)
                    {
                        //TODO: Возможно здесь стоит не подменять символ, а сразу вызывать обработчик нужной кнопки - это
                        // будет быстрее, но менее универсально
                        e.setKeyChar('=');
                    }
                    else if (keyCode == KeyEvent.VK_COMMA || keyCode == KeyEvent.VK_DECIMAL || keyCode == KeyEvent.VK_PERIOD)
                    {
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
                    Optional btn = Arrays.stream(mainPanel.getComponents()).filter(x -> (x instanceof JButton) &&
                            ((JButton)x).getActionCommand().trim().charAt(0) == e.getKeyChar()).findFirst();
                    if (e.getKeyCode() == KeyEvent.VK_MINUS && (e.isControlDown() || e.isShiftDown()))
                    {
                        try
                        {
                            double number = Math.max(0, -Double.parseDouble(txtDisplay.getText()));
                            txtDisplay.setText(Double.toString(number));
                        }
                        catch (Exception ex)
                        {
                            // обработка не требуется - польз-ль попытался изменить знак НЕчисла
                        }
                    }
                    else if (btn.isPresent())
                    {
                        btn.ifPresent(bt -> ((JButton) bt).doClick());
                    }
                }
                else
                {
                    super.keyPressed(e);
                }
            }
        });
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
    
    
}
