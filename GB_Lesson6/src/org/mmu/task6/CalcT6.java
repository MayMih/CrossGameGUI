package org.mmu.task6;

import javax.swing.*;
import java.awt.*;

/**
 * 1. Разработать оконное приложение «Калькулятор»;
 * */
public class CalcT6
{
    private JPanel mainPanel;
    private JTextField txtDisplay;
    
    public static void main(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
        new CalcT6().CreateForm();
    }
    
    /**
     * Метод создания главного окна приложения
     *
     * @apiNote     В каталоге, указанном в качестве рабочего должна лежать иконка приложения "basic16.png"
     *
     * @implNote    Поля "главного" класса {@link CalcT6} соот-щие графическим компонентам (вроде {@link CalcT6#mainPanel})
     *  генерируются автоматически средой IntelliJ Idea на основе xml-файла <b>CalcT6.form</b>, т.о. вручную остаётся только
     *  создать главное окно программы.
     * */
    private void CreateForm()
    {
        JFrame jf = new JFrame("Калькулятор");
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setContentPane(this.mainPanel);
        //jf.setSize(320, 480);
        jf.setPreferredSize(new Dimension(320, 240));
        ImageIcon img = new ImageIcon("./basic16.png");
        jf.setIconImage(img.getImage());
        jf.pack();
        jf.setVisible(true);
    }
    
    private void createUIComponents()
    {
        // TODO: place custom component creation code here
    }
}
