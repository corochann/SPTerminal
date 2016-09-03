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

/**
 * Multiline Command confirm dialog
 * It is invoked from {@link TerminalPanel} when user paste multi line command.
 */
public class MultilineCommandConfirmDialog extends JDialog implements ActionListener {

    /*--- CONSTANTS ---*/
    private static final int OPTION_OK = 0; // Same with JOptionPane.OK_OPTION
    private static final int OPTION_CANCEL = 2; // Same with JOptionPane.CANCEL_OPTION
    private static final int OPTION_REGISTER = 3;  // register as new macro

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_REGISTER = "register";
    private static final String ACTION_CANCEL = "cancel";
    private final JLabel confirmLabel;
    private final JTextArea multiLineCommandTextArea;

    private String commandsText = "";
    private int option = OPTION_CANCEL;

    public MultilineCommandConfirmDialog(Frame owner, String pasteText) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.commandsText = pasteText;

        this.setTitle("Multi line command execution");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: confirmLabel */
        JPanel confirmPanel = new JPanel();
        //confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));

        confirmLabel = new JLabel("Transmit below text or register as new Teraterm macro?");
        confirmPanel.add(confirmLabel);

        /* 2nd row: multiline command text */
        JPanel multilineCommandPanel = new JPanel();
        //confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.X_AXIS));

        multiLineCommandTextArea = new JTextArea(pasteText);
        multiLineCommandTextArea.setEditable(false);

        multilineCommandPanel.add(multiLineCommandTextArea);

        /* 3rd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleSelectorConfig.getStyleConfig());
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        CustomJButton registerButton = new CustomJButton("Register", styleSelectorConfig.getStyleConfig());
        registerButton.setActionCommand(ACTION_REGISTER);
        registerButton.addActionListener(this);
        CustomJButton cancelButton = new CustomJButton("Cancel", styleSelectorConfig.getStyleConfig());
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(registerButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(confirmPanel);
        mainPanel.add(multilineCommandPanel);
        mainPanel.add(formButtonPanel);

        this.getContentPane().add(mainPanel);
        this.pack();
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
                String ttlText = TTLParser.convertToSendlnCommand(this.commandsText);
                /* Launch TTLMacroConfigDialog */
                SPTerminal frame = SPTerminal.getFrame();
                TTLMacroConfigDialog ttlMacroConfigDialog = new TTLMacroConfigDialog(frame, ttlText);
                ttlMacroConfigDialog.setLocationRelativeTo(frame);
                ttlMacroConfigDialog.setVisible(true);

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
