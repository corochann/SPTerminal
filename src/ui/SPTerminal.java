package ui;

import config.ProjectConfig;
import config.SPTerminalPreference;
import config.SerialPortConfig;
import serial.SerialPortManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import static config.ProjectConfig.APP_TITLE;

/**
 * Main class for SPTerminal
 */
public class SPTerminal extends JFrame implements ActionListener {

    // lafClassName to use
    public static final String WINDOWS_STYLE = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    // Action definition
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DISCONNECT = "disconnect";

    // State definition
    /** currently not playing */
    public static final int STATE_DISCONNECTED = 0;
    /** state to prepare playing: ex parse txt file, push necessary files. */
    public static final int STATE_CONNECTING  = 1;
    /** currently playing */
    public static final int STATE_CONNECTED  = 2;
    public static final String ACTION_REFRESH = "refresh";

    // ----- Relations -------

    // ----- Attributes -------
    private static SPTerminal frame;

      // Top
    private final JComboBox mPortComboBox;

      // Center
    private JTabbedPane mCenterTabbedPane;
    public final TerminalPanel mTerminalPanel;
    private static SerialPortManager serialPortManager;
    private final JButton mConnectButton;
    private int connectionState;
    private final JButton mRefreshButton;

    public static SerialPortManager getSerialPortManager() {
        return serialPortManager;
    }


    public static void main(String[] args) {
        initialSetup();

        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                // We want to make this UI as first priority, we don't want other task to interrupt.
                // So invokeAndWait method is used.
                @Override
                public void run(){
                    createGUIDoFirst();
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return ;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return ;
        }
        // We need to use invokeAndWait method for createGUIDoFirst,
        // otherwise createGUIDoLater will interrupt to createGUIDoFirst.

        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                frame.createGUIDoLater();
            }
        });

        serialPortManager = new SerialPortManager();
    }


    /**
     * Initial setup for this app. It is for backend setup, not UI part.
     * @return
     */
    private static int initialSetup() {
        int ret = -1;
        try {
            // Prepare necessary folders
            if (new File(ProjectConfig.USER_FOLDER).mkdirs()) {
                System.out.println(ProjectConfig.USER_FOLDER + " directory created.");
            }
            if (new File(ProjectConfig.CACHE_FOLDER).mkdirs()) {
                System.out.println(ProjectConfig.CACHE_FOLDER + " directory created.");
            }
            // Prepare necessary files
            File f = new File(ProjectConfig.SPTERMINAL_INI_PATH);
            if (!f.exists() || f.isDirectory()) {
                if (f.createNewFile()) {
                    System.out.println(ProjectConfig.SPTERMINAL_INI_PATH + " created.");
                } else {
                    System.out.println("[ERROR]: " + ProjectConfig.SPTERMINAL_INI_PATH + " create fail.");
                }
            }
            ret = 0;
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    private int Finalize() {
        int ret = -1;
        if (mTerminalPanel != null) {
            mTerminalPanel.Finalize();
        }
        ret = 0;
        return ret;
    }

    /**
     * Must be executed in EDT (Event Dispatch Thread)
     * GUI processing which must be done urgently is placed here.
     */
    private static void createGUIDoFirst() {
        //final Debugger frame = new Debugger();
        frame = new SPTerminal();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Finalization callback at EXIT timing
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent){
                /* Finalization process here */
                frame.Finalize();
                System.out.println("windowClosing...");
                //System.exit(0);
            }
        });

        //frame.setBounds(50, 50, 1300, 800);
        frame.pack(); // Instead of specify size absolute value, it makes frame "packed".
        frame.setLocationRelativeTo(null); // Launch Frame at the center of Window.
        frame.setTitle(APP_TITLE);
        //frame.setIconImage(logo.getImage());
        frame.setVisible(true);
    }


    /**
     * Must be executed in EDT (Event Dispatch Thread)
     * This is executed after {@link #createGUIDoFirst} finishes,
     * GUI processing which can be done not urgently is placed here.
     */
    private void createGUIDoLater() {

    }

    private SPTerminal() {
        try{
            UIManager.setLookAndFeel(WINDOWS_STYLE);
            //SwingUtilities.updateComponentTreeUI(this);
        }catch(Exception ex){
            System.out.println("Error L&F Setting");
        }
        /* MenuBar */
        setJMenuBar(new MenuBar());

        /* Top Panel: NORTH */
        JPanel topPanel = new JPanel(); // Panel for BorderLaout.NORTH

        // Connect/Disconnect button
        mRefreshButton = new JButton("Refresh");  // Refresh available ports
        mRefreshButton.setActionCommand(ACTION_REFRESH);
        mRefreshButton.addActionListener(this);
        mPortComboBox = new JComboBox();          // Show available ports
        mConnectButton = new JButton();           // Connect/Disconnect button
        mConnectButton.addActionListener(this);

        topPanel.add(mRefreshButton);
        topPanel.add(mPortComboBox);
        topPanel.add(mConnectButton);


         /* centerTabbedPane: CENTER */
        mCenterTabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Only make 1. RecorderPanel. Other remaining Panel will be created at CreateGUILater()
        // 1 Recorder UI
        mTerminalPanel = new TerminalPanel();
        mCenterTabbedPane.addTab("terminal", mTerminalPanel);
        mCenterTabbedPane.setMnemonicAt(0, KeyEvent.VK_R);
        getContentPane().add(mCenterTabbedPane, BorderLayout.CENTER);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        refreshPortNames();
        updateCurrentConnectionState(STATE_DISCONNECTED);
    }

    /** getInstance() method for this class */
    public static SPTerminal getFrame() {
        return frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ACTION_REFRESH)) {
            refreshPortNames();
        } else if (action.equals(ACTION_CONNECT)) {
            String portName = (String) mPortComboBox.getSelectedItem();
            //updateCurrentConnectionState(STATE_CONNECTING); // maybe not necessary, Initialize() finish quick enough
            int ret = serialPortManager.Initialize(portName);
            if (ret == 0) {
                /* Success */
                updateCurrentConnectionState(STATE_CONNECTED);

                /* Update preference & save */
                SPTerminalPreference preference = SPTerminalPreference.getInstance();
                preference.setStringValue(SerialPortConfig.KEY_LAST_CONNECTED_PORTNAME, portName);
                preference.savePreference();
            } else {
                /* Some error happend during Initialize */
                System.out.println("Error connecting to " + portName + ", ret = " + ret);
                switch (ret) {
                    case SerialPortManager.ERROR_DEFAULT:
                        // port is not opened. It is highly likely that port did not found. Better to refresh portNames
                        JOptionPane.showMessageDialog(frame,
                                "Port " + portName + " cannot be opened.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        refreshPortNames();
                        break;
                    case SerialPortManager.ERROR_PORTINUSE:  // port already connected to another application.
                        JOptionPane.showMessageDialog(frame,
                                "Port " + portName + " is already used.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    case SerialPortManager.ERROR_UNSUPPORTEDOPERATION:
                    case SerialPortManager.ERROR_IOEXCEPTION:
                    default: // Show error message (Reason unknown)
                        JOptionPane.showMessageDialog(frame, "Error while opening port " + portName + ".");
                        break;
                }
                serialPortManager.Finalize();
                updateCurrentConnectionState(STATE_DISCONNECTED);
            }
        } else if (action.equals(ACTION_DISCONNECT)) {
            serialPortManager.Finalize();  // Currently no error handling
            updateCurrentConnectionState(STATE_DISCONNECTED);
        }
    }

    /**
     * Refresh port names in {@link #mPortComboBox}.
     * Must be executed in EDT (Event Dispatch Thread)
     */
    private void refreshPortNames() {
        Vector<String> portNames = SerialPortManager.getPortNames();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(portNames);
        mPortComboBox.setModel(model);

        SPTerminalPreference preference = SPTerminalPreference.getInstance();
        String lastPortName = preference.getStringValue(SerialPortConfig.KEY_LAST_CONNECTED_PORTNAME, "Undefined");
        if (portNames.contains(lastPortName)) {
            mPortComboBox.setSelectedItem(lastPortName);
        }
    }

    /**
     * updates UI and state
     * @param state
     */
    private void updateCurrentConnectionState(int state) {
        if(mConnectButton == null
                || mPortComboBox == null
                || mTerminalPanel == null){
            System.out.println("Error: component is not initialized");
            return;
        }

        switch(state) {
            case STATE_DISCONNECTED:
                // UI update
                mConnectButton.setText("Connect");
                mConnectButton.setIcon(ProjectConfig.PLAY_ICON_SMALL);
                mConnectButton.setActionCommand(ACTION_CONNECT);
                mPortComboBox.setEnabled(true);
                break;
            case STATE_CONNECTING:
                mConnectButton.setText("Connecting...");
                mConnectButton.setIcon(ProjectConfig.STOP_ICON_SMALL);
                mConnectButton.setActionCommand(ACTION_DISCONNECT);
                mPortComboBox.setEnabled(false);
                break;
            case STATE_CONNECTED:
                mConnectButton.setText("Disconnect");
                mConnectButton.setIcon(ProjectConfig.STOP_ICON_SMALL);
                mConnectButton.setActionCommand(ACTION_DISCONNECT);
                mPortComboBox.setEnabled(false);
                break;
            default:
                System.out.println("Unknown state: " + state);
                break;
        }
        // update current state
        connectionState = state;
        /* notify to TerminalPanel */
        mTerminalPanel.notifyCurrentConnectionState(state);
    }

}
