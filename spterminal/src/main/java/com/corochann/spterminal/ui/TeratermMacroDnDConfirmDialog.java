package com.corochann.spterminal.ui;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.teraterm.TTLParser;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.menu.TTLMacroConfigDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Teraterm macro drag & drop confirm dialog
 * It is invoked from {@link TerminalPanel} when user drag & drop (DnD) teraterm macro file.
 */
public class TeratermMacroDnDConfirmDialog extends JDialog implements ActionListener {

    /*--- CONSTANTS ---*/
    public static final int OPTION_OK = 0; // Same with JOptionPane.OK_OPTION
    public static final int OPTION_CANCEL = 2; // Same with JOptionPane.CANCEL_OPTION
    public static final int OPTION_REGISTER = 3;  // register as new macro

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_REGISTER = "register";
    private static final String ACTION_CANCEL = "cancel";
    private final JLabel confirmLabel;

    private int option = OPTION_CANCEL;
    private final CustomJButton okButton;
    private final CustomJButton registerButton;
    private final CustomJButton cancelButton;

    public TeratermMacroDnDConfirmDialog(Frame owner, String ttlFileName) {
        super(owner, ModalityType.APPLICATION_MODAL);

        this.setTitle("Teraterm macro execute");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: confirmLabel */
        JPanel confirmPanel = new JPanel();
        //confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));

        confirmLabel = new JLabel("Execute or register " + ttlFileName + "?");
        confirmPanel.add(confirmLabel);

        /* 2nd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        okButton = new CustomJButton("Ok", styleSelectorConfig.getStyleConfig());
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        okButton.setToolTipText("Execute command. Enter key works for shortcut.");

        registerButton = new CustomJButton("Register", styleSelectorConfig.getStyleConfig());
        registerButton.setActionCommand(ACTION_REGISTER);
        registerButton.addActionListener(this);
        registerButton.setToolTipText("Register TTL Macro. r key works for shortcut.");

        cancelButton = new CustomJButton("Cancel", styleSelectorConfig.getStyleConfig());
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        cancelButton.setToolTipText("Cancel to execute command and exit. ESC key works for shortcut.");
        formButtonPanel.add(okButton);
        formButtonPanel.add(registerButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(confirmPanel);
        mainPanel.add(formButtonPanel);

        /* setFocusable & requestFocusInWindow is necessary for addKeyListener to work */
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("[DEBUG] MultilineCommandConfirmDialog keyPressed: " + e.getKeyCode());
                //super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        okButton.doClick();
                        break;
                    case KeyEvent.VK_R:
                        registerButton.doClick();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        cancelButton.doClick();
                        break;
                    default:
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        this.getContentPane().add(mainPanel);
        this.pack();
    }

    public void showDialog() {
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case ACTION_OK:
                System.out.println("Ok pressed");
                option = OPTION_OK;
                this.dispose();
                break;
            case ACTION_REGISTER:
                /* register new macro */
                option = OPTION_REGISTER;
                this.dispose();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                option = OPTION_CANCEL;
                this.dispose();
                break;
        }
    }

    public int getOption() {
        return option;
    }
}
