package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.component.CustomJButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Style config dialog
 * It is invoked from {@link SPTMenuBar}
 *
 * Note: Style config dialog specifies {@link StyleSelectorConfig},
 * and it specifies which {@link StyleConfig} to be used.
 */
public class StyleConfigDialog extends JDialog implements ActionListener {

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_CANCEL = "cancel";
    private final JComboBox styleNameComboBox;

    public StyleConfigDialog(Frame owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("Style setup");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: style name combo box */
        JPanel styleNamePanel = new JPanel();
        styleNamePanel.setLayout(new BoxLayout(styleNamePanel, BoxLayout.X_AXIS));

        JLabel styleNameLabel = new JLabel("Style name");
        styleNameComboBox = new JComboBox(StyleConfig.STYLE_LIST);
        styleNameComboBox.setSelectedItem(styleSelectorConfig.getStyleName());

        styleNamePanel.add(styleNameLabel);
        styleNamePanel.add(styleNameComboBox);

        /* 2nd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleSelectorConfig.getStyleConfig());
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        CustomJButton cancelButton = new CustomJButton("Cancel", styleSelectorConfig.getStyleConfig());
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(styleNamePanel);
        mainPanel.add(formButtonPanel);

        this.getContentPane().add(mainPanel);
        this.pack();
    }

    public void showDialog() {
        setLocationRelativeTo(this.getOwner());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case ACTION_OK:
                System.out.println("Ok pressed");
                String styleName = ((String) styleNameComboBox.getSelectedItem());
                saveStyleSelectorConfig(styleName);
                this.dispose();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                this.dispose();
                break;
        }
    }

    private void saveStyleSelectorConfig(String styleName) {
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();
        styleSelectorConfig.setStyleName(styleName);
        styleSelectorConfig.save();
    }
}
