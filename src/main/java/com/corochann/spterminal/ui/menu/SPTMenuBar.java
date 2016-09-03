package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.ui.SPTerminal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Menu bar of {@link SPTerminal}.
 */
public class SPTMenuBar extends JMenuBar implements ActionListener {

    /* Action definitions */
    public static final String ACTION_SETUP_SERIALPORT = "setupserialport";
    public static final String ACTION_SETUP_LOG = "setuplog";
    public static final String ACTION_SETUP_STYLE = "setupstyle";
    public static final String ACTION_TTLMACRO = "ttlmacro";
    public static final String ACTION_ABOUT = "about";

    public SPTMenuBar() {
        super();

        /* MENU: File */
        /* MENU: Setup */
        JMenu setupMenu = new JMenu("Setup");
        setupMenu.setMnemonic(KeyEvent.VK_S);

          /* style */
        JMenuItem styleSetup = new JMenuItem("Style");
        styleSetup.setMnemonic(KeyEvent.VK_T);
        styleSetup.addActionListener(this);
        styleSetup.setActionCommand(ACTION_SETUP_STYLE);

          /* log */
        JMenuItem logSetup = new JMenuItem("Log");
        logSetup.setMnemonic(KeyEvent.VK_L);
        logSetup.addActionListener(this);
        logSetup.setActionCommand(ACTION_SETUP_LOG);

          /* serial port */
        JMenuItem serialPortSetup = new JMenuItem("Serial port");
        serialPortSetup.setMnemonic(KeyEvent.VK_P);
        serialPortSetup.addActionListener(this);
        serialPortSetup.setActionCommand(ACTION_SETUP_SERIALPORT);

        setupMenu.add(styleSetup);
        setupMenu.add(logSetup);
        setupMenu.add(serialPortSetup);

        /* MENU: Tools */
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
          /* ttl macro */
        JMenuItem ttlMacroSetup = new JMenuItem("Teraterm macro");
        ttlMacroSetup.setMnemonic(KeyEvent.VK_T);
        ttlMacroSetup.addActionListener(this);
        ttlMacroSetup.setActionCommand(ACTION_TTLMACRO);

        toolsMenu.add(ttlMacroSetup);
        /* MENU: Help */
        JMenu helpMenu = new JMenu("Help");
        setupMenu.setMnemonic(KeyEvent.VK_H);
          /* about */
        JMenuItem about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_BACK_SLASH);
        about.addActionListener(this);
        about.setActionCommand(ACTION_ABOUT);

        helpMenu.add(about);

        this.add(setupMenu);
        this.add(toolsMenu);
        this.add(helpMenu);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        SPTerminal frame = SPTerminal.getFrame();
        switch (action) {
            case ACTION_SETUP_STYLE:
                StyleConfigDialog styleConfigDialog = new StyleConfigDialog(frame);
                styleConfigDialog.setLocationRelativeTo(frame);
                styleConfigDialog.setVisible(true);
                break;
            case ACTION_SETUP_LOG:
                LogConfigDialog logConfigDialog = new LogConfigDialog(frame);
                logConfigDialog.setLocationRelativeTo(frame);
                logConfigDialog.setVisible(true);
                break;
            case ACTION_SETUP_SERIALPORT:
                SerialPortConfigDialog spConfigDialog = new SerialPortConfigDialog();
                //spConfigDialog.setLocationRelativeTo(null);  // show dialong at Center of PC's window
                spConfigDialog.setLocationRelativeTo(frame);  // show dialong at Center of |frame|
                spConfigDialog.setVisible(true);
                break;
            case ACTION_TTLMACRO:
                TTLMacroConfigDialog ttlMacroConfigDialog = new TTLMacroConfigDialog(frame);
                ttlMacroConfigDialog.setLocationRelativeTo(frame);
                ttlMacroConfigDialog.setVisible(true);
                break;
            case ACTION_ABOUT:
                VersionDialog versionDialog = new VersionDialog(frame);
                versionDialog.setLocationRelativeTo(frame);
                versionDialog.setVisible(true);
                break;
            default:
                System.out.println("Action " + action + " not handled!");
                break;
        }
    }
}
