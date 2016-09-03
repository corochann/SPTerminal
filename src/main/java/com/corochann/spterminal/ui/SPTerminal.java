package com.corochann.spterminal.ui;

import com.corochann.spterminal.config.LogConfig;
import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.SerialPortConfig;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.log.MyLogger;
import com.corochann.spterminal.serial.SerialPortManager;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.menu.SPTMenuBar;
import com.corochann.spterminal.util.MyUtils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import static com.corochann.spterminal.config.ProjectConfig.APP_TITLE;

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
    private MyLogger mLogger = null;
    private static SPTerminalPreference mPreference;

    // ----- Attributes -------
    private static SPTerminal frame;

      // Top
    private final JComboBox mPortComboBox;

      // Center
    public final TerminalPanel mTerminalPanel;
    private static SerialPortManager serialPortManager;
    private final CustomJButton mConnectButton;
    private final CustomJButton mRefreshButton;

    public static SerialPortManager getSerialPortManager() {
        return serialPortManager;
    }

    private int connectionState;

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
        mPreference = SPTerminalPreference.getInstance();
        int ret = -1;
        try {
            // Prepare necessary folders
            MyUtils.prepareDir(ProjectConfig.USER_FOLDER);
            MyUtils.prepareDir(ProjectConfig.LOG_FOLDER);
            MyUtils.prepareDir(ProjectConfig.CACHE_FOLDER);

            // Prepare necessary files

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
        // Finalize logging
        if (mLogger != null) mLogger.Finalize();
        mLogger = null;
        ret = 0;
        return ret;
    }

    /**
     * Must be executed in EDT (Event Dispatch Thread)
     * GUI processing which must be done urgently is placed here.
     */
    private static void createGUIDoFirst() {
        /*--- UIDefaults ---*/
        setupUIDefaults();

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

    private static void setupUIDefaults() {
        StyleConfig styleConfig = mPreference.getStyleSelectorConfig().getStyleConfig();
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();

        if (styleConfig.getBaseBackGroundColor() != null) {
            ColorUIResource bgColor = new ColorUIResource(styleConfig.getBaseBackGroundColor());
            //uiDefaults.put("Viewport.background", bgColor); // no effect
            uiDefaults.put("Panel.background", bgColor);
            uiDefaults.put("TextField.background", bgColor);
            uiDefaults.put("TextArea.background", bgColor);
            uiDefaults.put("TextPane.background", bgColor);
            uiDefaults.put("Label.background", bgColor);
            // button have many types, ex JButton, JScrollPane's up/down button,,,
            // it is difficult to set default value, specify for each component instead.
            //uiDefaults.put("Button.background", bgColor);
            uiDefaults.put("CheckBox.background", bgColor);
            uiDefaults.put("TabbedPane.background", bgColor);
            uiDefaults.put("OptionPane.background", bgColor);
        }
        if (styleConfig.getBaseForeGroundColor() != null) {
            ColorUIResource fgColor = new ColorUIResource(styleConfig.getBaseForeGroundColor());
            //uiDefaults.put("Viewport.foreground", fgColor); // no effect
            uiDefaults.put("Panel.foreground", fgColor);
            uiDefaults.put("TextField.foreground", fgColor);
            uiDefaults.put("TextField.caretForeground", fgColor);
            uiDefaults.put("TextArea.foreground", fgColor);
            uiDefaults.put("TextArea.caretForeground", fgColor);
            uiDefaults.put("TextPane.foreground", fgColor);
            uiDefaults.put("TextPane.caretForeground", fgColor);
            uiDefaults.put("Label.foreground", fgColor);
            uiDefaults.put("Button.foreground", fgColor);
            uiDefaults.put("CheckBox.foreground", fgColor);
            uiDefaults.put("OptionPane.foreground", fgColor);
            uiDefaults.put("OptionPane.messageForeground", fgColor);
            //uiDefaults.put("TabbedPane.foreground", fgColor);
        }
        if (styleConfig.getBaseFont() != null) {
            Font baseFont = new FontUIResource(styleConfig.getBaseFont());
            //uiDefaults.put("Viewport.font", baseFont); // no effect
            uiDefaults.put("Panel.font", baseFont);
            uiDefaults.put("TextField.font", baseFont);
            uiDefaults.put("TextArea.font", baseFont);
            uiDefaults.put("TextPane.font", baseFont);
            uiDefaults.put("Label.font", baseFont);
            uiDefaults.put("Button.font", baseFont);
            uiDefaults.put("CheckBox.font", baseFont);
            uiDefaults.put("TabbedPane.font", baseFont);
            uiDefaults.put("Menu.font", baseFont);
            uiDefaults.put("MenuBar.font", baseFont);
            uiDefaults.put("MenuItem.font", baseFont);
            uiDefaults.put("OptionPane.font", baseFont);
        }

        //uiDefaults.put("TabbedPane.focus", red);
        //uiDefaults.put("TabbedPane.light", green);
        //uiDefaults.put("Button.shadow", red);
        //uiDefaults.put("Button.darkShadow", red);
        //uiDefaults.put("Button.highlight", red);
        //uiDefaults.put("Button.light", yellow);
        //uiDefaults.put("Button.select", green);  // When button pressed

        //SwingUtilities.updateComponentTreeUI(frame);
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
            //UIManager.setLookAndFeel(WINDOWS_STYLE);
            //SwingUtilities.updateComponentTreeUI(this);
        }catch(Exception ex){
            System.out.println("Error L&F Setting");
        }
        StyleConfig styleConfig = mPreference.getStyleSelectorConfig().getStyleConfig();
        /* SPTMenuBar */
        setJMenuBar(new SPTMenuBar());


        /* Top Panel: NORTH */
        JPanel topPanel = new JPanel(); // Panel for BorderLaout.NORTH

        // Connect/Disconnect button
        mRefreshButton = new CustomJButton("Refresh", styleConfig);  // Refresh available ports
        mRefreshButton.setActionCommand(ACTION_REFRESH);
        mRefreshButton.addActionListener(this);

        mPortComboBox = new JComboBox();          // Show available ports
        mConnectButton = new CustomJButton(styleConfig);           // Connect/Disconnect button
        //mConnectButton.setContentAreaFilled(false);
        mConnectButton.addActionListener(this);

        topPanel.add(mRefreshButton);
        topPanel.add(mPortComboBox);
        topPanel.add(mConnectButton);


         /* centerTabbedPane: CENTER */
        //mCenterTabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Only make 1. RecorderPanel. Other remaining Panel will be created at CreateGUILater()
        // 1 Recorder UI
        mTerminalPanel = new TerminalPanel();
        //mCenterTabbedPane.addTab("terminal", mTerminalPanel);
        //mCenterTabbedPane.setMnemonicAt(0, KeyEvent.VK_R);
        getContentPane().add(mTerminalPanel, BorderLayout.CENTER);

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

                /* Update lastConnectedPortName in preference & save */
                SerialPortConfig serialPortConfig = mPreference.getSerialPortConfig();
                serialPortConfig.setLastConnectedPortName(portName);
                serialPortConfig.save();
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

        SerialPortConfig serialPortConfig = mPreference.getSerialPortConfig();
        String lastPortName = serialPortConfig.getLastConnectedPortName();
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

                // Finalize logging
                if (mLogger != null) mLogger.Finalize();
                mLogger = null;
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

                // Initialize logging
                LogConfig logConfig = mPreference.getLogConfig();
                if (logConfig.isAutoLogging()) {
                    mLogger = new MyLogger(logConfig.getAutoLogFileName(), serialPortManager.getCurrentPortName());
                }
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


    public MyLogger getLogger() {
        return mLogger;
    }
}
