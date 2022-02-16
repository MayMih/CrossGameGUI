package org.mmu.task6;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * 1. Разработать оконное приложение «Калькулятор»;
 */
public class CalcT6
{
    private JPanel mainPanel2;
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
    
    private JMenuBar miniBar;
    private Point mouseDownCursorPos = new Point();
    private JFrame jf;
    
   
    /**
     * Создаём окно в отдельном, т.н. "потоке обработки событий" (вроде как так принято в Swing)
     *
     * @implNote    Плюсом такого подхода по идее должно быть отсутствие возможности "повесить" интерфейс за счёт очень
     *  долгой обработки какого-то события, а минусом - необходимость вызова конструкций синхронизации при обращении к
     *  элементам интерфейса из обработчика, а также побочный эффект в виде перехода формы в неопределённое состояние,
     *  если заранее не отключать "лишние" (все?) контролы пока не выполнится текущий обработчик.
     * */
    public static void main(String[] args)
    {
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
                new CalcT6().setActionListeners();
            }
        });
    }
    
    /**
     * Конструктор главного окна приложения
     *
     * @apiNote В каталоге, указанном в качестве рабочего должна лежать иконка приложения "basic16.png"
     *
     * @implNote Поля "главного" класса {@link CalcT6} соот-щие графическим компонентам (вроде {@link CalcT6#mainPanel2})
     * генерируются автоматически средой IntelliJ Idea на основе xml-файла <b>CalcT6.form</b>, т.о. вручную остаётся только
     * создать главное окно программы.
     *
     */
    public CalcT6()
    {
        jf = new JFrame("Калькулятор");
        //jf.setUndecorated(true);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setContentPane(this.mainPanel2);
        //jf.setSize(320, 480);
        //jf.setPreferredSize(new Dimension(320, 240));
        jf.setResizable(false);
        ImageIcon img = new ImageIcon("./basic16.png");
        jf.setIconImage(img.getImage());
        //txtDisplay.setMinimumSize(txtDisplay.getPreferredSize());
        miniBar = new JMenuBar();
        JMenu mainMenu = new JMenu("Инструменты");
        mainMenu.setToolTipText("Реализует задание 1.3");
        JMenuItem miOpenConverter = mainMenu.add("Конвертер величин");
        miOpenConverter.setToolTipText("Калькулятор работает с двумя параметрами, вводимыми пользователем в окна ввода");
        miniBar.add(mainMenu);
        jf.setJMenuBar(miniBar);
        jf.pack();
        jf.setLocationByPlatform(true);
        jf.setVisible(true);
    }
    

    /**
     * Метод навешивания обработчиков для компонентов
     * */
    private void setActionListeners()
    {
        /**
         * Обработчик поддержки перетаскивания окна за указанный компонент (строка меню)
         *
         * @implNote    почему-то получилось, только когда я разнёс по разным Прослушивателям обработку событий
         *  первоначального зажатия кнопки мыши и Перемещения мыши в зажатом состоянии.
         * */
        miniBar.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                mouseDownCursorPos = e.getLocationOnScreen();
            }
        });
    
        /**
         * Обработчик поддержки перетаскивания окна за указанный компонент
         * */
        miniBar.addMouseMotionListener(new MouseMotionListener()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                jf.setLocation(jf.getX() + (e.getXOnScreen() - mouseDownCursorPos.x), jf.getY() + + (e.getYOnScreen() - mouseDownCursorPos.y));
                mouseDownCursorPos = e.getLocationOnScreen();
            }
            
            @Override
            public void mouseMoved(MouseEvent e)
            {
            }
        });
        
//        txtDisplay.addMouseListener(miniBar.getMouseListeners()[0]);
//        txtDisplay.addMouseMotionListener(miniBar.getMouseMotionListeners()[0]);
    }
    
    
}
