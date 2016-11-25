package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.LayoutConfig;
import com.corochann.spterminal.plugin.MenuItemPlugin;
import com.corochann.spterminal.serial.SerialPortTX;
import com.corochann.spterminal.teraterm.TTLMacroExecutor;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.util.MyUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Iterator;

/**
 * Menu bar of {@link SPTerminal}.
 */
public class SPTMenuBar extends JMenuBar implements ActionListener {

    /* Action definitions */
    public static final String ACTION_EDIT_CLEARSCREEN = "clearscreen";
    public static final String ACTION_EDIT_FIND = "find";
    public static final String ACTION_SETUP_SERIALPORT = "setupserialport";
    public static final String ACTION_SETUP_LOG = "setuplog";
    public static final String ACTION_SETUP_STYLE = "setupstyle";
    public static final String ACTION_SETUP_LAYOUT = "layout";
    public static final String ACTION_TTLMACRO_REGISTER = "ttlmacroregister";
    public static final String ACTION_TTLMACRO_EXE = "ttlmacroexe";
    public static final String ACTION_ABOUT = "about";

    public SPTMenuBar() {
        super();

        /* MENU: File */

        /* MENU: Edit */
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
          /* clear screen */
        JMenuItem clearScreen = new JMenuItem("Clear screen");
        clearScreen.setMnemonic(KeyEvent.VK_C);
        clearScreen.addActionListener(this);
        clearScreen.setActionCommand(ACTION_EDIT_CLEARSCREEN);

          /* find */
        JMenuItem find = new JMenuItem("Find");
        find.setMnemonic(KeyEvent.VK_F);
        find.addActionListener(this);
        find.setActionCommand(ACTION_EDIT_FIND);

        editMenu.add(clearScreen);
        editMenu.add(find);

        /* MENU: Setup */
        JMenu setupMenu = new JMenu("Setup");
        setupMenu.setMnemonic(KeyEvent.VK_S);

          /* style */
        JMenuItem styleSetup = new JMenuItem("Style");
        styleSetup.setMnemonic(KeyEvent.VK_Y);
        styleSetup.addActionListener(this);
        styleSetup.setActionCommand(ACTION_SETUP_STYLE);

          /* layout */
        JMenuItem layoutSetup = new JMenuItem("Layout");
        layoutSetup.setMnemonic(KeyEvent.VK_L);
        layoutSetup.addActionListener(this);
        layoutSetup.setActionCommand(ACTION_SETUP_LAYOUT);

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
        setupMenu.add(layoutSetup);
        setupMenu.add(logSetup);
        setupMenu.add(serialPortSetup);

        /* MENU: Tools */
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_O);
          /* ttl macro */
        JMenuItem ttlMacroSetup = new JMenuItem("Teraterm macro setting");
        //ttlMacroSetup.setMnemonic(KeyEvent.VK_R);
        ttlMacroSetup.addActionListener(this);
        ttlMacroSetup.setActionCommand(ACTION_TTLMACRO_REGISTER);

        JMenuItem ttlMacroExe = new JMenuItem("Teraterm macro execute");
        ttlMacroExe.setMnemonic(KeyEvent.VK_M);
        ttlMacroExe.addActionListener(this);
        ttlMacroExe.setActionCommand(ACTION_TTLMACRO_EXE);

        toolsMenu.add(ttlMacroSetup);
        toolsMenu.add(ttlMacroExe);

        /* MENU: Plugins */
        Iterator<MenuItemPlugin> it = MyUtils.loadPlugins(MenuItemPlugin.class);
        JMenu pluginsMenu = new JMenu("Plugins");
        pluginsMenu.setMnemonic(KeyEvent.VK_P);
        while (it.hasNext()) {
            MenuItemPlugin menuItemPlugin = it.next();
            System.out.println("Found menuItemPlugin: " + menuItemPlugin.getText());
            JMenuItem pluginMenuItem = menuItemPlugin.createJMenuItemInstance();
            pluginsMenu.add(pluginMenuItem);
        }

        /* MENU: Help */
        JMenu helpMenu = new JMenu("Help");
        setupMenu.setMnemonic(KeyEvent.VK_H);
          /* about */
        JMenuItem about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_BACK_SLASH);
        about.addActionListener(this);
        about.setActionCommand(ACTION_ABOUT);

        helpMenu.add(about);

        this.add(editMenu);
        this.add(setupMenu);
        this.add(toolsMenu);
        if (pluginsMenu.getItemCount() > 0) this.add(pluginsMenu);
        this.add(helpMenu);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        SPTerminal frame = SPTerminal.getFrame();
        switch (action) {
            case ACTION_EDIT_CLEARSCREEN:
                frame.mTerminalPanel.clearRXScreen();
                break;
            case ACTION_EDIT_FIND:
                frame.mTerminalPanel.startFind();
                break;
            case ACTION_SETUP_STYLE:
                StyleConfigDialog styleConfigDialog = new StyleConfigDialog(frame);
                styleConfigDialog.showDialog();
                break;
            case ACTION_SETUP_LAYOUT:
                LayoutConfigDialog layoutConfigDialog = new LayoutConfigDialog(frame);
                layoutConfigDialog.showDialog();
                if (layoutConfigDialog.getOption() == LayoutConfigDialog.OPTION_OK) {
                    frame.updateLayout();
                }
                break;
            case ACTION_SETUP_LOG:
                LogConfigDialog logConfigDialog = new LogConfigDialog(frame);
                logConfigDialog.showDialog();
                break;
            case ACTION_SETUP_SERIALPORT:
                SerialPortConfigDialog spConfigDialog = new SerialPortConfigDialog();
                spConfigDialog.showDialog();
                break;
            case ACTION_TTLMACRO_REGISTER:
                TTLMacroConfigDialog ttlMacroConfigDialog = new TTLMacroConfigDialog(frame);
                ttlMacroConfigDialog.showDialog();
                break;
            case ACTION_TTLMACRO_EXE:
                executeTTLMacro();
                break;
            case ACTION_ABOUT:
                VersionDialog versionDialog = new VersionDialog(frame);
                versionDialog.showDialog();
                break;
            default:
                System.out.println("Action " + action + " not handled!");
                break;
        }
    }

    private void executeTTLMacro() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter ttlFileFilter = new FileNameExtensionFilter("TTL Macro file", "ttl");
        fileChooser.addChoosableFileFilter(ttlFileFilter);
        fileChooser.setFileFilter(ttlFileFilter);
        int selected = fileChooser.showOpenDialog(SPTerminal.getFrame());

        if (selected == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (checkFile(file)) {
                String fileName = file.getName();
                String filePath = file.getAbsolutePath();
                System.out.println("TTL Macro exe " + fileName + " selected.");
                SerialPortTX portTX = SPTerminal.getSerialPortManager().getPortTX();
                if (portTX != null) {
                    // Run another thread for TTL Macro.
                    TTLMacroExecutor ttlMacroExecutor = new TTLMacroExecutor(filePath, portTX);
                    ttlMacroExecutor.start();
                } else {
                    JOptionPane.showMessageDialog(this, "[Error] port is not connected yet");
                }
            } else {
                System.out.println("file cannot be executed");
            }
        } else {
            System.out.println("TTL Macro exe cancel");
        }
    }

    private boolean checkFile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canRead()) {
                return true;
            }
        }
        return false;
    }

}
