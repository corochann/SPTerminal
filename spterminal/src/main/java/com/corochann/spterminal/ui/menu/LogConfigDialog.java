package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.LogConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.ui.component.CustomJButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Log config dialog
 * It is invoked from {@link SPTMenuBar}
 */
public class LogConfigDialog extends JDialog implements ActionListener {

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_CANCEL = "cancel";
    private final JCheckBox autoLogCheckBox;
    private final JTextField logFileNameTextField;

    public LogConfigDialog(Frame owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("Log setup");
        LogConfig logConfig = SPTerminalPreference.getInstance().getLogConfig();
        StyleConfig styleConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig().getStyleConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st row: auto-logging check box */
        JPanel autoLogCheckBoxPanel = new JPanel();
        autoLogCheckBoxPanel.setLayout(new BoxLayout(autoLogCheckBoxPanel, BoxLayout.X_AXIS));


        JLabel autoLogLabel = new JLabel("Take log automatically");
        autoLogCheckBox = new JCheckBox();
        autoLogCheckBox.setSelected(logConfig.isAutoLogging());

        autoLogCheckBoxPanel.add(autoLogLabel);
        autoLogCheckBoxPanel.add(autoLogCheckBox);

        /* 2nd row: log file name */
        JPanel logFileNamePanel = new JPanel();
        logFileNamePanel.setLayout(new BoxLayout(logFileNamePanel, BoxLayout.X_AXIS));

        JLabel logFileNameLabel = new JLabel("Log file name");
        logFileNameTextField = new JTextField();
        logFileNameTextField.setText(logConfig.getAutoLogFileName());

        logFileNamePanel.add(logFileNameLabel);
        logFileNamePanel.add(logFileNameTextField);

        /* 3rd row: description */
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));

        JLabel descriptionLabel = new JLabel("<html>'&h' is converted to host name.<br/>" +
                "JAVA's SimpleDateFormat is used for log file name</html>");
        descriptionPanel.add(descriptionLabel);

        /* 4th row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleConfig);
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        CustomJButton cancelButton = new CustomJButton("Cancel", styleConfig);
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(autoLogCheckBoxPanel);
        mainPanel.add(logFileNamePanel);
        mainPanel.add(descriptionPanel);
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
                boolean autoLogging = autoLogCheckBox.isSelected();
                String autoLogFileName = logFileNameTextField.getText();
                saveLogConfig(autoLogging, autoLogFileName);
                this.dispose();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                this.dispose();
                break;
        }
    }

    private void saveLogConfig(boolean autoLogging, String autoLogFileName) {
        LogConfig logConfig = SPTerminalPreference.getInstance().getLogConfig();
        logConfig.setAutoLogging(autoLogging);
        logConfig.setAutoLogFileName(autoLogFileName);
        logConfig.save();
    }
}
