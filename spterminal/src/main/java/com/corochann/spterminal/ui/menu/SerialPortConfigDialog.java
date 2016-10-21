package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.SerialPortConfig;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.ui.component.CustomJButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Serial port config dialog.
 * It is invoked from {@link SPTMenuBar}.
 */
public class SerialPortConfigDialog extends JDialog implements ActionListener {

    /* Action definitions */
    public static final String ACTION_OK = "ok";
    public static final String ACTION_CANCEL = "cancel";

    private final JComboBox baudrateComboBox;
    private final JComboBox databitComboBox;
    private final JComboBox parityComboBox;
    private final JComboBox stopbitComboBox;
    private final JCheckBox rtsctsinCheckBox;
    private final JCheckBox rtsctsoutCheckBox;
    private final JCheckBox xonxoffinCheckBox;
    private final JCheckBox xonxoffoutCheckBox;

    SerialPortConfigDialog() {
        super(SPTerminal.getFrame(), ModalityType.APPLICATION_MODAL);
        this.setTitle("Serial port setup");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(8, 2, 100, 30));  // row = 5, col = 2, hgap (px), vgap(px)

        /* create each component */
        baudrateComboBox = new JComboBox(SerialPortConfig.BAUDRATE_LIST);
        databitComboBox = new JComboBox(SerialPortConfig.DATABIT_LIST);
        parityComboBox = new JComboBox(SerialPortConfig.PARITY_LIST);
        stopbitComboBox = new JComboBox(SerialPortConfig.STOPBIT_LIST);
        rtsctsinCheckBox = new JCheckBox();
        rtsctsoutCheckBox = new JCheckBox();
        xonxoffinCheckBox = new JCheckBox();
        xonxoffoutCheckBox = new JCheckBox();

        /* value setting */
        SerialPortConfig serialPortConfig = SPTerminalPreference.getInstance().getSerialPortConfig();
        StyleConfig styleConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig().getStyleConfig();

        baudrateComboBox.setSelectedItem(serialPortConfig.getBaudrate());
        databitComboBox.setSelectedItem(serialPortConfig.getDatabit());
        parityComboBox.setSelectedItem(serialPortConfig.getParity());
        stopbitComboBox.setSelectedItem(serialPortConfig.getStopbit());
        rtsctsinCheckBox.setSelected(serialPortConfig.isRtsctsIn());
        rtsctsoutCheckBox.setSelected(serialPortConfig.isRtsctsOut());
        xonxoffinCheckBox.setSelected(serialPortConfig.isXonxoffIn());
        xonxoffoutCheckBox.setSelected(serialPortConfig.isXonxoffOut());

        /* Add to panel */
        gridPanel.add(new JLabel("Baud rate"));
        gridPanel.add(baudrateComboBox);
        gridPanel.add(new JLabel("Data bit"));
        gridPanel.add(databitComboBox);
        gridPanel.add(new JLabel("Parity"));
        gridPanel.add(parityComboBox);
        gridPanel.add(new JLabel("Stop bit"));
        gridPanel.add(stopbitComboBox);

        gridPanel.add(new JLabel("Flow Control RTS/CTS in"));
        gridPanel.add(rtsctsinCheckBox);
        gridPanel.add(new JLabel("Flow Control RTS/CTS out"));
        gridPanel.add(rtsctsoutCheckBox);
        gridPanel.add(new JLabel("Flow Control XON/XOFF in"));
        gridPanel.add(xonxoffinCheckBox);
        gridPanel.add(new JLabel("Flow Control XON/XOFF out"));
        gridPanel.add(xonxoffoutCheckBox);

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

        mainPanel.add(gridPanel);
        mainPanel.add(formButtonPanel);
        this.getContentPane().add(mainPanel);
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ACTION_OK)) {
            System.out.println("Ok pressed");
            /* Update SerialPortConfig preference */
            String baudrateValue = (String) baudrateComboBox.getSelectedItem();
            String databitValue = (String) databitComboBox.getSelectedItem();
            String parityValue = (String) parityComboBox.getSelectedItem();
            String stopbitValue = (String) stopbitComboBox.getSelectedItem();

            boolean rtsctsinValue = rtsctsinCheckBox.isSelected();
            boolean rtsctsoutValue = rtsctsoutCheckBox.isSelected();
            boolean xonxoffinValue = xonxoffinCheckBox.isSelected();
            boolean xonxoffoutValue = xonxoffoutCheckBox.isSelected();

            saveSerialPortConfigs(baudrateValue, databitValue, parityValue, stopbitValue,
                    rtsctsinValue, rtsctsoutValue, xonxoffinValue, xonxoffoutValue);
            this.dispose();
        } else if (action.equals(ACTION_CANCEL)) {
            System.out.println("Cancel pressed");
            this.dispose();
        }
    }

    private void saveSerialPortConfigs(String baudrateValue, String databitValue, String parityValue, String stopbitValue,
                                       boolean rtsctsinValue, boolean rtsctsoutValue, boolean xonxoffinValue, boolean xonxoffoutValue) {
        SerialPortConfig serialPortConfig = SPTerminalPreference.getInstance().getSerialPortConfig();
        serialPortConfig.setBaudrate(baudrateValue);
        serialPortConfig.setDatabit(databitValue);
        serialPortConfig.setParity(parityValue);
        serialPortConfig.setStopbit(stopbitValue);
        serialPortConfig.setRtsctsIn(rtsctsinValue);
        serialPortConfig.setRtsctsOut(rtsctsoutValue);
        serialPortConfig.setXonxoffIn(xonxoffinValue);
        serialPortConfig.setXonxoffOut(xonxoffoutValue);
        serialPortConfig.save();
    }
}
