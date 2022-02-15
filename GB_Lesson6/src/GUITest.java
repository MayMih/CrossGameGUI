import javax.swing.*;
import java.awt.*;


public class GUITest implements Runnable
{
    public static void createGUI()
    {
        try
        {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            //SwingUtilities.updateComponentTreeUI(jf);
        }
        catch (Exception e)
        {
            System.out.println("Ошибка при загрузке Metal-Look-And-Feel");
        }
        JFrame jf = new JFrame("Test");
        jf.revalidate();
        Insets ins = jf.getInsets();    // аналог C#  Padding
        //jf.getRootPane().setWindowDecorationStyle();
        //JLayeredPane jlp = new JLayeredPane();
        //jf.add(jlp);
        //jf.setResizable(false);
//        RepaintManager.currentManager(jf).setDoubleBufferingEnabled(true);
//        RepaintManager.currentManager(jf).setDoubleBufferMaximumSize(new Dimension(1920, 1080));
        
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setPreferredSize(new Dimension(800, 480));
        jf.pack();  // похоже аналог C# window.Autosize = true; - правда тут в спарвке говорится ещё о каких-то побочных явлениях, вроде установки
        //  displayable в true (?) и валидации, правда тут validate() похоже значит совсем другое - речь не о валидации данных,
        // а о неком аналоге Control.PerfromLayout()
        jf.setVisible(true);
    }
    
    @Override
    public void run()
    {
        createGUI();
    }
    
    public static void main(String[] args)
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(new GUITest());
    }
}