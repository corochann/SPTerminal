package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.LayoutConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.component.CustomJFormattedTextField;
import jdk.nashorn.internal.runtime.ParserException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Style config dialog
 * It is invoked from {@link SPTMenuBar}
 *
 * Note: Style config dialog specifies {@link StyleSelectorConfig},
 * and it specifies which {@link StyleConfig} to be used.
 */
public class LayoutConfigDialog extends JDialog implements ActionListener {

    /*--- CONSTANTS ---*/
    public static final int OPTION_OK = 0; // Same with JOptionPane.OK_OPTION
    public static final int OPTION_CANCEL = 2; // Same with JOptionPane.CANCEL_OPTION

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_CANCEL = "cancel";

    /*--- Attribute ---*/
    private final JCheckBox autoUpdateCheckBox;
    private int option = OPTION_CANCEL;
    private final CustomJFormattedTextField frameWidthTextFeild;
    private final CustomJFormattedTextField frameHeightTextFeild;
    private final CustomJFormattedTextField verticalDividerLocationTextFeild;
    private final CustomJFormattedTextField horizontalDividerLocationTextFeild;

    public LayoutConfigDialog(Frame owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("Layout setup");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();
        LayoutConfig layoutConfig = SPTerminalPreference.getInstance().getLayoutConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: auto update check box */
        JPanel autoUpdatePanel = new JPanel();
        autoUpdatePanel.setLayout(new BoxLayout(autoUpdatePanel, BoxLayout.X_AXIS));

        autoUpdateCheckBox = new JCheckBox("Auto update");
        autoUpdateCheckBox.setToolTipText(
                "If checked, the values when window closes are automatically saved and used for next launch."
        );
        autoUpdateCheckBox.setSelected(layoutConfig.isAutoUpdate());

        autoUpdatePanel.add(autoUpdateCheckBox);

        /*--- 2nd-5th row - --*/
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 2, 100, 30));  // row = 5, col = 2, hgap (px), vgap(px)

        NumberFormat nfi = NumberFormat.getIntegerInstance();
        nfi.setMaximumIntegerDigits(10000);
        nfi.setMinimumIntegerDigits(0);

        frameWidthTextFeild = new CustomJFormattedTextField(nfi);
        frameWidthTextFeild.setValue(layoutConfig.getFrameWidth());
        frameHeightTextFeild = new CustomJFormattedTextField(nfi);
        frameHeightTextFeild.setValue(layoutConfig.getFrameHeight());
        verticalDividerLocationTextFeild = new CustomJFormattedTextField(nfi);
        verticalDividerLocationTextFeild.setValue(layoutConfig.getTerminalSplitPaneLocation());
        horizontalDividerLocationTextFeild = new CustomJFormattedTextField(nfi);
        horizontalDividerLocationTextFeild.setValue(layoutConfig.getRightVerticalSplitPaneLocation());

        /* 2nd row: frameWidth */
        gridPanel.add(new JLabel("Frame Width: "));
        gridPanel.add(frameWidthTextFeild);
        gridPanel.add(new JLabel("Frame Height: "));
        gridPanel.add(frameHeightTextFeild);
        gridPanel.add(new JLabel("Vertical divider location: "));
        gridPanel.add(verticalDividerLocationTextFeild);
        gridPanel.add(new JLabel("Horizontal divider location: "));
        gridPanel.add(horizontalDividerLocationTextFeild);

        /* 6th row: form button */
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

        mainPanel.add(autoUpdatePanel);
        mainPanel.add(gridPanel);
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
                boolean autoUpdateFlag = autoUpdateCheckBox.isSelected();
                try {
                    frameWidthTextFeild.commitEdit();
                    frameHeightTextFeild.commitEdit();
                    verticalDividerLocationTextFeild.commitEdit();
                    horizontalDividerLocationTextFeild.commitEdit();
                    int frameWidth = ((Long)(frameWidthTextFeild.getValue())).intValue();
                    int frameHeight = ((Long)frameHeightTextFeild.getValue()).intValue();
                    int terminalSplitPaneLocation = ((Long)verticalDividerLocationTextFeild.getValue()).intValue();
                    int rightVerticalSplitPaneLocation = ((Long)horizontalDividerLocationTextFeild.getValue()).intValue();
                    saveLayoutConfig(autoUpdateFlag, frameWidth, frameHeight,
                            terminalSplitPaneLocation, rightVerticalSplitPaneLocation);
                    option = OPTION_OK;
                    this.dispose();
                } catch (ParseException pe) {
                    JOptionPane.showMessageDialog(this, "Invalid value!");
                    pe.printStackTrace();
                }
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                option = OPTION_CANCEL;
                this.dispose();
                break;
        }
    }

    private void saveLayoutConfig(boolean autoUpdateFlag, int frameWidth, int frameHeight,
                                  int terminalSplitPaneLocation, int rightVerticalSplitPaneLocation) {
        LayoutConfig layoutConfig = SPTerminalPreference.getInstance().getLayoutConfig();
        layoutConfig.setAutoUpdate(autoUpdateFlag);
        layoutConfig.setFrameWidth(frameWidth);
        layoutConfig.setFrameHeight(frameHeight);
        layoutConfig.setTerminalSplitPaneLocation(terminalSplitPaneLocation);
        layoutConfig.setRightVerticalSplitPaneLocation(rightVerticalSplitPaneLocation);
        layoutConfig.save();
    }

    public int getOption() {
        return option;
    }
}
