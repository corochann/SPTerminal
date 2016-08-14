package ui;

import config.ProjectConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Menu bar of {@link SPTerminal}.
 */
public class MenuBar extends JMenuBar implements ActionListener {

    /* Action definitions */
    public static final String ACTION_SETUP_SERIALPORT = "setupserialport";
    public static final String ACTION_ABOUT = "about";

    MenuBar() {
        super();

        /* MENU: File */
        /* MENU: Setup */
        JMenu setupMenu = new JMenu("Setup");
        setupMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem serialPortSetup = new JMenuItem("Serial port");
        serialPortSetup.addActionListener(this);
        serialPortSetup.setActionCommand(ACTION_SETUP_SERIALPORT);

        setupMenu.add(serialPortSetup);
        /* MENU: Help */
        JMenu helpMenu = new JMenu("Help");
        setupMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(this);
        about.setActionCommand(ACTION_ABOUT);

        helpMenu.add(about);

        this.add(setupMenu);
        this.add(helpMenu);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(ACTION_SETUP_SERIALPORT)) {
            JDialog spConfigDialog = new SerialPortConfigDialog().getDialog();
            //spConfigDialog.setLocationRelativeTo(null);  // show dialong at Center of PC's window
            spConfigDialog.setLocationRelativeTo(SPTerminal.getFrame());  // show dialong at Center of |frame|
            spConfigDialog.setVisible(true);
        } else if (action.equals(ACTION_ABOUT)) {
            String aboutText =
                    "Version: " + ProjectConfig.VERSION + "\n";
            //+ "Last updated: bbb";
            JOptionPane.showMessageDialog(this, aboutText);
        }
    }

}
