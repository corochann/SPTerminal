package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.component.CustomJButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Version config dialog
 * It is invoked from {@link SPTMenuBar}
 */
public class VersionDialog extends JDialog implements ActionListener {

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_CANCEL = "cancel";
    private final JLabel versionLabel;

    public VersionDialog(Frame owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("SPTerminal SW Version");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: version label */
        JPanel styleNamePanel = new JPanel();
        styleNamePanel.setLayout(new BoxLayout(styleNamePanel, BoxLayout.X_AXIS));

        String versionText = "Version: " + ProjectConfig.VERSION;
        versionLabel = new JLabel(versionText);

        styleNamePanel.add(versionLabel);

        /* 2nd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleSelectorConfig.getStyleConfig());
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        formButtonPanel.add(okButton);

        mainPanel.add(styleNamePanel);
        mainPanel.add(formButtonPanel);

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
                this.dispose();
                break;
        }
    }
}
