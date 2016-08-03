package ui;

import serial.SerialTest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * Main class for SerialTerminalTest
 */
public class SPTerminal extends JFrame {

    // lafClassName to use
    public static final String WINDOWS_STYLE = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    // Application manifest
    public static final String APP_TITLE = "SerialTerminalTest";


    // ----- Relations -------

    // ----- Attributes -------
    private static SPTerminal frame;

      // Center
    private JTabbedPane mCenterTabbedPane;
    public final TerminalPanel mTerminalPanel;

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                // We want to make this UI as first priority, we don't want other task to interrupt.
                // So invokeAndWait method is used.
                @Override
                public void run(){
                    createGUIDoFirst();
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return ;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ;
        }
        // We need to use invokeAndWait method for createGUIDoFirst,
        // otherwise createGUIDoLater will interrupt to createGUIDoFirst.

        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                frame.createGUIDoLater();
            }
        });

        new SerialTest().initialize();
    }


    /**
     * Must be executed in EDT (Event Dispatch Thread)
     * GUI processing which must be done urgently is placed here.
     */
    private static void createGUIDoFirst() {
        //final Debugger frame = new Debugger();
        frame = new SPTerminal();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Finalization callback at EXIT timing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent){
                System.out.println("windowClosing...");
                //System.exit(0);
            }
        });

        //frame.setBounds(50, 50, 1300, 800);
        frame.pack(); // Instead of specify size absolute value, it makes frame "packed".
        frame.setLocationRelativeTo(null); // Launch Frame at the center of Window.
        frame.setTitle(APP_TITLE);
        //frame.setIconImage(logo.getImage());
        frame.setVisible(true);
    }


    /**
     * Must be executed in EDT (Event Dispatch Thread)
     * This is executed after {@link #createGUIDoFirst} finishes,
     * GUI processing which can be done not urgently is placed here.
     */
    private void createGUIDoLater() {

    }


    private SPTerminal() {
        try{
            UIManager.setLookAndFeel(WINDOWS_STYLE);
            //SwingUtilities.updateComponentTreeUI(this);
        }catch(Exception ex){
            System.out.println("Error L&F Setting");
        }

         /* centerTabbedPane: CENTER */

        mCenterTabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Only make 1. RecorderPanel. Other remaining Panel will be created at CreateGUILater()
        // 1 Recorder UI
        mTerminalPanel = new TerminalPanel();
        mCenterTabbedPane.addTab("terminal", mTerminalPanel);
        mCenterTabbedPane.setMnemonicAt(0, KeyEvent.VK_R);

        getContentPane().add(mCenterTabbedPane, BorderLayout.CENTER);
    }

    public static SPTerminal getFrame() {
        return frame;
    }
}
