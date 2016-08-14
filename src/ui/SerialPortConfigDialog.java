package ui;

import config.SPTerminalPreference;
import config.SerialPortConfig;
import serial.SerialPortProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static serial.SerialPortProxy.*;

/**
 * Serial port config dialog.
 * It is invoked from {@link MenuBar}.
 */
public class SerialPortConfigDialog implements ActionListener {

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

    JDialog spConfigDialog = null;
    private SPTerminalPreference preference;


    SerialPortConfigDialog() {
        spConfigDialog = new JDialog(SPTerminal.getFrame(), Dialog.ModalityType.APPLICATION_MODAL);
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
        preference = SPTerminalPreference.getInstance();

        String baudrate = preference.getStringValue(SerialPortConfig.KEY_BAUDRATE, SerialPortProxy.DEFAULT_BAUDRATE);
        String databit = preference.getStringValue(SerialPortConfig.KEY_DATABIT, SerialPortProxy.DEFAULT_DATABIT);
        String parity = preference.getStringValue(SerialPortConfig.KEY_PARITY, SerialPortProxy.DEFAULT_PARITY);
        String stopbit = preference.getStringValue(SerialPortConfig.KEY_STOPBIT, SerialPortProxy.DEFAULT_STOPBIT);
        int rtsctsInConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_IN, DEFAULT_RTSCTS_IN);
        int rtsctsOutConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_OUT, DEFAULT_RTSCTS_OUT);
        int xonxoffInConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_IN, DEFAULT_XONXOFF_IN);
        int xonxoffOutConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_OUT, DEFAULT_XONXOFF_OUT);

        baudrateComboBox.setSelectedItem(baudrate);
        databitComboBox.setSelectedItem(databit);
        parityComboBox.setSelectedItem(parity);
        stopbitComboBox.setSelectedItem(stopbit);
        rtsctsinCheckBox.setSelected(rtsctsInConfig != 0);
        rtsctsoutCheckBox.setSelected(rtsctsOutConfig != 0);
        xonxoffinCheckBox.setSelected(xonxoffInConfig != 0);
        xonxoffoutCheckBox.setSelected(xonxoffOutConfig != 0);

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
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(gridPanel);
        mainPanel.add(formButtonPanel);
        spConfigDialog.getContentPane().add(mainPanel);
        spConfigDialog.pack();
    }

    public JDialog getDialog() {
        return spConfigDialog;
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

            String rtsctsinValue = rtsctsinCheckBox.isSelected() ? "1" : "0";
            String rtsctsoutValue = rtsctsoutCheckBox.isSelected() ? "1" : "0";
            String xonxoffinValue = xonxoffinCheckBox.isSelected() ? "1" : "0";
            String xonxoffoutValue = xonxoffoutCheckBox.isSelected() ? "1" : "0";

            saveSerialPortConfigs(baudrateValue, databitValue, parityValue, stopbitValue,
                    rtsctsinValue, rtsctsoutValue, xonxoffinValue, xonxoffoutValue);
            spConfigDialog.dispose();
        } else if (action.equals(ACTION_CANCEL)) {
            System.out.println("Cancel pressed");
            spConfigDialog.dispose();
        }
    }

    private void saveSerialPortConfigs(String baudrateValue, String databitValue, String parityValue, String stopbitValue, String rtsctsinValue, String rtsctsoutValue, String xonxoffinValue, String xonxoffoutValue) {
        preference = SPTerminalPreference.getInstance();
        preference.setStringValue(SerialPortConfig.KEY_BAUDRATE, baudrateValue);
        preference.setStringValue(SerialPortConfig.KEY_DATABIT, databitValue);
        preference.setStringValue(SerialPortConfig.KEY_PARITY, parityValue);
        preference.setStringValue(SerialPortConfig.KEY_STOPBIT, stopbitValue);
        preference.setStringValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_IN, rtsctsinValue);
        preference.setStringValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_OUT, rtsctsoutValue);
        preference.setStringValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_IN, xonxoffinValue);
        preference.setStringValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_OUT, xonxoffoutValue);
        preference.savePreference();
    }
}
