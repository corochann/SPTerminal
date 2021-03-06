package com.corochann.spterminal.ui;

import com.corochann.spterminal.config.*;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.log.MyAnsiLogger;
import com.corochann.spterminal.log.MyLogger;
import com.corochann.spterminal.serial.SerialPortManager;
import com.corochann.spterminal.serial.SerialPortRX;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.menu.SPTMenuBar;
import com.corochann.spterminal.util.MyUtils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
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
    public static final String ACTION_REFRESH = "refresh";  // refresh serial port list
    public static final String ACTION_WEBCAM_START = "webcamstart"; // start webcam panel

    // State definition
    /** currently not playing */
    public static final int STATE_DISCONNECTED = 0;
    /** state to prepare playing: ex parse txt file, push necessary files. */
    public static final int STATE_CONNECTING  = 1;
    /** currently playing */
    public static final int STATE_CONNECTED  = 2;


    private int connectionState;

    /*-- Relations ---*/
    private MyLogger mLogger = null;
    private MyAnsiLogger mAnsiLogger = null;
    private static SPTerminalPreference mPreference;
    private static LayoutConfig mLayoutConfig;

    /*--- Attributes ---*/
    private static SPTerminal frame;

      // Top
    private final JComboBox mPortComboBox;

      // Center
    public final TerminalPanel mTerminalPanel;
    private static SerialPortManager serialPortManager;
    private final CustomJButton mConnectButton;
    private final CustomJButton mRefreshButton;

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
        /*--- folder init ---*/
        mPreference = SPTerminalPreference.getInstance();
        mLayoutConfig = mPreference.getLayoutConfig();
        int ret = -1;
        try {
            // Prepare necessary folders
            MyUtils.prepareDir(ProjectConfig.USER_FOLDER);
            MyUtils.prepareDir(ProjectConfig.LOG_FOLDER);
            MyUtils.prepareDir(ProjectConfig.CACHE_FOLDER);
            MyUtils.prepareDir(ProjectConfig.PLUGIN_FOLDER);

            // Prepare necessary files

            ret = 0;
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    /** Exit SPTerminal */
    private int Finalize() {
        System.out.println("Finalize SPTerminal...");
        int ret = -1;
        if (mTerminalPanel != null) {
            mTerminalPanel.Finalize();
        }
        // Finalize logging
        SerialPortRX portRX = getSerialPortManager().getPortRX();
        if (portRX != null) {
            portRX.removeAllBufferLogger();
        }
        if (mLogger != null) {
            mLogger.Finalize();
            mLogger = null;
        }
        if (mAnsiLogger != null) {
            mAnsiLogger.Finalize();
            mAnsiLogger = null;
        }
        if (mPreference != null) {
            mPreference.Finalize();
        }
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
        frame.addComponentListener(frame.new FrameResizeListener());
        frame.pack(); // Instead of specify size absolute value, it makes frame "packed".
        frame.setLocationRelativeTo(null); // Launch Frame at the center of Window.
        frame.setTitle(APP_TITLE);
        //frame.setIconImage(logo.getImage());

        frame.updateLayout();
        frame.setVisible(true);
    }

    public void updateLayout() {
        System.out.println("updateLayout");
        System.out.println("w = " + mLayoutConfig.getFrameWidth() + ", h = " + mLayoutConfig.getFrameHeight());
        //frame.setPreferredSize(new Dimension(mLayoutConfig.getFrameWidth(), mLayoutConfig.getFrameHeight()));
        //frame.setSize(2000, 199);
        frame.setSize(new Dimension(mLayoutConfig.getFrameWidth(), mLayoutConfig.getFrameHeight()));
        // invoke after setSize done.
        if (mTerminalPanel != null) SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        mTerminalPanel.updateLayout();
                    }
                }
        );
    }

    private static void setupUIDefaults() {
        StyleConfig styleConfig = mPreference.getStyleSelectorConfig().getStyleConfig();
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();

        if (styleConfig.getBaseBackGroundColor() != null) {
            ColorUIResource bgColor = new ColorUIResource(styleConfig.getBaseBackGroundColor());
            //uiDefaults.put("Viewport.background", bgColor); // no effect
            // button have many types, ex JButton, JScrollPane's up/down button,,,
            // it is difficult to set default value, specify for each component instead.
            //uiDefaults.put("Button.background", bgColor);
            uiDefaults.put("CheckBox.background", bgColor);
            uiDefaults.put("ComboBox.background", bgColor);
            uiDefaults.put("Desktop.background", bgColor);
            uiDefaults.put("Label.background", bgColor);
            uiDefaults.put("List.background", bgColor);
            uiDefaults.put("OptionPane.background", bgColor);
            uiDefaults.put("Panel.background", bgColor);
            uiDefaults.put("ScrollPane.background", bgColor);
            uiDefaults.put("SplitPane.background", bgColor);
            uiDefaults.put("TabbedPane.background", bgColor);
            uiDefaults.put("Table.background", bgColor);
            uiDefaults.put("TableHeader.background", bgColor);
            uiDefaults.put("TextField.background", bgColor);
            uiDefaults.put("TextArea.background", bgColor);
            uiDefaults.put("TextPane.background", bgColor);
            uiDefaults.put("Viewport.background", bgColor);
        }
        if (styleConfig.getBaseForeGroundColor() != null) {
            ColorUIResource fgColor = new ColorUIResource(styleConfig.getBaseForeGroundColor());
            uiDefaults.put("Button.foreground", fgColor);
            uiDefaults.put("CheckBox.foreground", fgColor);
            uiDefaults.put("ComboBox.foreground", fgColor);
            uiDefaults.put("Label.foreground", fgColor);
            uiDefaults.put("List.foreground", fgColor);
            uiDefaults.put("OptionPane.foreground", fgColor);
            uiDefaults.put("OptionPane.messageForeground", fgColor);
            uiDefaults.put("Panel.foreground", fgColor);
            uiDefaults.put("ScrollPane.foreground", fgColor);
            //uiDefaults.put("TabbedPane.foreground", fgColor);
            uiDefaults.put("Table.foreground", fgColor);
            uiDefaults.put("TableHeader.foreground", fgColor);
            uiDefaults.put("TextField.foreground", fgColor);
            uiDefaults.put("TextField.caretForeground", fgColor);
            uiDefaults.put("TextArea.foreground", fgColor);
            uiDefaults.put("TextArea.caretForeground", fgColor);
            uiDefaults.put("TextPane.foreground", fgColor);
            uiDefaults.put("TextPane.caretForeground", fgColor);
            uiDefaults.put("Viewport.foreground", fgColor);
        }
        if (styleConfig.getBaseFont() != null) {
            Font baseFont = new FontUIResource(styleConfig.getBaseFont());
            //uiDefaults.put("Viewport.font", baseFont); // no effect
            uiDefaults.put("Button.font", baseFont);
            uiDefaults.put("CheckBox.font", baseFont);
            uiDefaults.put("ComboBox.font", baseFont);
            uiDefaults.put("Label.font", baseFont);
            uiDefaults.put("List.font", baseFont);
            uiDefaults.put("Menu.font", baseFont);
            uiDefaults.put("MenuBar.font", baseFont);
            uiDefaults.put("MenuItem.font", baseFont);
            uiDefaults.put("OptionPane.font", baseFont);
            uiDefaults.put("Panel.font", baseFont);
            uiDefaults.put("ScrollPane.font", baseFont);
            uiDefaults.put("TabbedPane.font", baseFont);
            uiDefaults.put("Table.font", baseFont);
            uiDefaults.put("TableHeader.font", baseFont);
            uiDefaults.put("TextField.font", baseFont);
            uiDefaults.put("TextArea.font", baseFont);
            uiDefaults.put("TextPane.font", baseFont);
        }

        //uiDefaults.put("Table.gridColor", Color.RED);
        //uiDefaults.put("Table.dropLineColor", Color.RED);
        //uiDefaults.put("Table.dropLineShortColor", Color.RED);
        //uiDefaults.put("Table.focusCellBackground", Color.RED);
        //uiDefaults.put("Table.selectionBackground", Color.RED);
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

        // Connect/Disconnect button
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

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new GlobalKeyEventDispatcher());
        refreshPortNames();
        updateCurrentConnectionState(STATE_DISCONNECTED);
    }

    /** Handles global shortcut key */
    private class GlobalKeyEventDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            //System.out.println("[DEBUG] KeyboardFocusManager event.getID: " + e.getID() + ", e.getKeyCode() = " + e.getKeyCode());
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    /* CTRL key pressed */
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_F:  // find
                            mTerminalPanel.startFind();
                            return true;
                        case KeyEvent.VK_T:  // type, terminal, txTextField
                            mTerminalPanel.startType();
                            return true;
                    }
                }
            }
            return false;
        }
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

        SerialPortRX portRX;
        switch(state) {
            case STATE_DISCONNECTED:
                // UI update
                mConnectButton.setText("Connect");
                mConnectButton.setIcon(ProjectConfig.PLAY_ICON_SMALL);
                mConnectButton.setActionCommand(ACTION_CONNECT);
                mPortComboBox.setEnabled(true);

                // Finalize logging
                SerialPortManager spm = getSerialPortManager();
                if (spm != null) {
                    portRX = spm.getPortRX();
                    if (portRX != null) {
                        portRX.removeAllBufferLogger();
                    }
                }
                if (mLogger != null) mLogger.Finalize();
                mLogger = null;
                if (mAnsiLogger != null) mAnsiLogger.Finalize();
                mAnsiLogger = null;
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
                    portRX = getSerialPortManager().getPortRX();
                    if (ProjectConfig.DEBUG) {
                        /* Save Raw log only for debug */
                        mLogger = new MyLogger(logConfig.constructAutoLogFilePath(MyLogger.LOG_FILE_NAME_PREFIX, serialPortManager.getCurrentPortName()));
                        portRX.addBufferLogger(mLogger);
                    }
                    mAnsiLogger = new MyAnsiLogger(logConfig.constructAutoLogFilePath(MyAnsiLogger.LOG_FILE_NAME_PREFIX, serialPortManager.getCurrentPortName()));
                    portRX.addBufferLogger(mAnsiLogger);
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

    /** getInstance() method for this class */
    public static SPTerminal getFrame() {
        return frame;
    }

    public static SerialPortManager getSerialPortManager() {
        return serialPortManager;
    }

    public MyLogger getLogger() {
        return mLogger;
    }

    public MyAnsiLogger getAnsiLogger() {
        return mAnsiLogger;
    }

    /*--- INNER CLASS ---*/
    class FrameResizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            //Recalculate the variable you mentioned
            Component cmp = e.getComponent();
            //System.out.println("FrameResizeListener X = " + cmp.getX()
            //        + ", Y = " + cmp.getY()
            //        + ", w = " + cmp.getWidth()
            //        + ", h = " + cmp.getHeight()
            //);
            mTerminalPanel.onFrameComponentResize(e);
            if (mLayoutConfig.isAutoUpdate()) {
                mLayoutConfig.setFrameWidth(cmp.getWidth());
                mLayoutConfig.setFrameHeight(cmp.getHeight());
            }
        }
    }
}

