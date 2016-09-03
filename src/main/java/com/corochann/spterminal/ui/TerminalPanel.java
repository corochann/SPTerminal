package com.corochann.spterminal.ui;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.config.teraterm.TTLMacroConfig;
import com.corochann.spterminal.data.CommandHistory;
import com.corochann.spterminal.data.model.HighlightableCommand;
import com.corochann.spterminal.serial.SerialPortRX;
import com.corochann.spterminal.serial.SerialPortTX;
import com.corochann.spterminal.teraterm.TTLMacroExecutor;
import com.corochann.spterminal.ui.component.CustomJTextField;
import com.corochann.spterminal.ui.component.HighlightableTextPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.Vector;

import static com.corochann.spterminal.ui.SPTerminal.*;

/**
 *
 */
public class TerminalPanel extends JPanel implements ActionListener {


    /* Constants */
    public static final String ACTION_OUTPUT_STREAM = "actionOutputStream";
    public static final String STATUS_TEXT_DEFAULT = "<html>" +
            "TAB auto-completion sometimes not work correctly...<br/>" +
            "Use ↑, ↓ keys to move suggestion, Ctrl+Enter to auto-fill with selected suggestion.<br/>" +
            "Ctrl+Delete to remove item from suggestion list (but it will not delete command history).</html>";
    public static final int COMMAND_HISTORY_MAX_SIZE = 3000;

    /* Relation */
    private SerialPortTX portTX = null;
    private SerialPortRX portRX = null;

    /* Attribute */
    private final JCheckBox mRXTextAreaAutoScrollCheckBox;
    private final JTextArea mRXTextArea;
    private final CustomJTextField mTXTextField;
    private final HighlightableTextPane mTXTextPane;
    private CommandHistory mCH;
    private String portName;
    private String currentExpectedTXText = "";
    private Vector<HighlightableCommand> currentSuggestionVec = new Vector<>();
    private int currentSelectedSuggestionIndex = 0;
    private Rectangle r = null;

    private final Color defaultForeGroundColor;
    private final Color charHighlightColor;
    private TTLMacroConfig ttlMacroConfig;

    TerminalPanel() {
        super(new BorderLayout());
        ttlMacroConfig = SPTerminalPreference.getInstance().getTTLMacroConfig();
        StyleConfig styleConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig().getStyleConfig();
        defaultForeGroundColor = styleConfig.getBaseForeGroundColor() == null ?
                Color.BLACK : styleConfig.getBaseForeGroundColor();
        charHighlightColor = styleConfig.getCharHighlightColor();


        JSplitPane terminalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        terminalSplitPane.setDividerSize(5);  // Default value 10
        /* |terminalSplitPanel| contains terminalLeftPanel and terminalRight  */
        JPanel terminalLeftPanel = new JPanel();
        terminalLeftPanel.setLayout(new BoxLayout(terminalLeftPanel, BoxLayout.Y_AXIS));
        terminalLeftPanel.setMinimumSize(new Dimension(0, 0));
        JPanel terminalRightPanel = new JPanel();
        terminalRightPanel.setLayout(new BoxLayout(terminalRightPanel, BoxLayout.Y_AXIS));
        terminalRightPanel.setMinimumSize(new Dimension(0, 0));

        /* Left Panel */
        mRXTextAreaAutoScrollCheckBox = new JCheckBox("Auto-scroll", true);
        mRXTextAreaAutoScrollCheckBox.setHorizontalTextPosition(JCheckBox.LEFT);
        mRXTextAreaAutoScrollCheckBox.setToolTipText(
                "When enabled, it automatically scroll below text area to bottom when log comes");


        mRXTextArea = new JTextArea(40, 75);
        mRXTextArea.setLineWrap(false);

        JScrollPane inputStreamScrollPane = new JScrollPane(mRXTextArea);
        terminalLeftPanel.add(mRXTextAreaAutoScrollCheckBox);
        terminalLeftPanel.add(inputStreamScrollPane);


        terminalSplitPane.setLeftComponent(terminalLeftPanel);

        /* Right panel */
        mTXTextField = new CustomJTextField();
        mTXTextField.setToolTipText(STATUS_TEXT_DEFAULT);
        mTXTextField.setPreferredSize(new Dimension(
                500,
                mTXTextField.getPreferredSize().height
        ));
        mTXTextField.setActionCommand(ACTION_OUTPUT_STREAM);
        mTXTextField.addActionListener(this);
        mTXTextField.setFocusTraversalKeysEnabled(false);  // Disable side effect of TAB key
        mTXTextField.addKeyListener(new TXTextFieldKeyListener());  // Key handling logic
        mTXTextField.getDocument().addDocumentListener(new TXTextFieldDocumentListener()); // Document handling logic

        mTXTextPane = new HighlightableTextPane();
        if (styleConfig.getLineHighlightColor() != null) {
            mTXTextPane.setHighlightColor(styleConfig.getLineHighlightColor());
        }

        mTXTextPane.setPreferredSize(new Dimension(30, 600));
        //mTXTextPane.setLineWrap(false);
        JScrollPane outputStreamScrollPane = new JScrollPane(mTXTextPane);

        terminalRightPanel.add(mTXTextField);
        terminalRightPanel.add(outputStreamScrollPane);

        terminalSplitPane.setRightComponent(terminalRightPanel);

        /*--- Font setting ---*/
        if (styleConfig.getTerminalFont() != null) {
            mRXTextArea.setFont(styleConfig.getTerminalFont());
            mTXTextField.setFont(styleConfig.getTerminalFont());
            mTXTextPane.setFont(styleConfig.getTerminalFont());
        }

        /* final layout */
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(terminalSplitPane);
    }

    /**
     * update suggestion from CommandHistory using auto-suggestion algorithm
     */
    private void updateSuggestion() {
        /* Update Command history auto-completion */
        String smartSuggestion = "";
        Vector<HighlightableCommand> suggestionVec = new Vector<>();
        if (currentExpectedTXText.length() <= 0) {  // when no character inputted
            /* get suggestion from commandHistoryVec */
            // Show all history up to 3000 (COMMAND_HISTORY_MAX_SIZE).
            int lb = Math.max(0, mCH.commandHistoryVec.size() - COMMAND_HISTORY_MAX_SIZE);
            for (int i = mCH.commandHistoryVec.size() - 1; i >= lb; i--) {
                String cmd = mCH.commandHistoryVec.get(i);
                smartSuggestion += cmd + "\n";
                suggestionVec.add(new HighlightableCommand(cmd));
            }
            mTXTextPane.setTextWithColor(smartSuggestion, defaultForeGroundColor);
        } else {
            /* get suggestion from commandHistoryMap */
            for (Map.Entry<String, Long> e : mCH.commandHistoryMap.entrySet()) {
                String cmd = e.getKey();
                // suggestionVec is updated during checkMatch()
                if (!cmd.startsWith(TTLMacroConfig.TTL_PREFIX)) { //TODO: Refactor, temporal C/M to avoid duplicated key
                    checkMatch(cmd, currentExpectedTXText, suggestionVec);
                }
            }
            /* get suggestion from TTLMacro */
            for (Map.Entry<String, String> e : ttlMacroConfig.ttlMacroMap.entrySet()) {
                String cmd = e.getKey();
                checkMatch(cmd, currentExpectedTXText, suggestionVec);
                // TODO: Refactor, remove duplicated key.
                // suggestionVec contains duplicated key from both commandHistoryMap & TTLMacroConfigList
            }
            mTXTextPane.setText("");
            for (int i = 0; i < suggestionVec.size(); i++) {
                String cmd = suggestionVec.get(i).command;
                Vector<Integer> idxVec = suggestionVec.get(i).indexVec;

                int idxSize = idxVec.size();
                for (int j = 0; j < idxSize; j++) {
                    int start = j == 0 ? 0 : idxVec.get(j-1) + 1;
                    mTXTextPane.appendTextWithColor(cmd.substring(start, idxVec.get(j)), defaultForeGroundColor);
                    mTXTextPane.appendTextWithColor(Character.toString(cmd.charAt(idxVec.get(j))), charHighlightColor);
                }
                if (idxVec.get(idxSize-1) + 1 < cmd.length()) {
                    mTXTextPane.appendTextWithColor(cmd.substring(idxVec.get(idxSize-1) + 1, cmd.length()), defaultForeGroundColor);
                }
                mTXTextPane.appendTextWithColor("\n", defaultForeGroundColor);
            }
        }

        if (suggestionVec.size() == 0) {
            updateSelectedSuggestionIndex(-1);  // No suggestion available
        } else {
            updateSelectedSuggestionIndex(0);   // Reset index for new suggestion
        }
        // Update currentSuggestionVec
        currentSuggestionVec = suggestionVec;
    }

    /**
     * auto-suggestion algorithm.
     * returns that targetText satisfies the condition specified by filterText or not.
     *
     * Example 1. targetText = "cat /proc/cpuinfo" & filterText = "cat cpuinfo" -> true
     * Example 2. targetText = "cat /proc/cpuinfo" & filterText = "cat proccpu" -> true
     * Example 3. targetText = "cat /proc/cpuinfo" & filterText = "cat proccpu" -> false
     *
     * @param targetText
     * @param filterText
     * @return
     */
    private boolean isMatch(String targetText, String filterText) {
        if (filterText == null) return true; // No filtering
        if (targetText == null) {
            System.out.println("[ERROR] targetText must not be null");
            return false; // No filtering
        } else if (targetText.length() == 0) {
            System.out.println("[WARNING] targetText is length 0");
            return true;
        }
        int pos = 0;
        for (int i = 0; i < filterText.length(); i++) {
            if (pos == targetText.length()) return false;
            while (targetText.charAt(pos++) != filterText.charAt(i)) {
                if (pos == targetText.length()) return false;
            }
        }
        return true;
    }

    /**
     * auto-suggestion algorithm.
     * returns that targetText satisfies the condition specified by filterText or not.
     * if it matches the result, it is added to the vec.
     *
     * Example 1. targetText = "cat /proc/cpuinfo" & filterText = "cat cpuinfo" -> true
     * Example 2. targetText = "cat /proc/cpuinfo" & filterText = "cat proccpu" -> true
     * Example 3. targetText = "cat /proc/cpuinfo" & filterText = "cat proccpu" -> false
     *
     * @param targetText
     * @param filterText
     * @param vec
     * @return
     */
    private boolean checkMatch(String targetText, String filterText, Vector<HighlightableCommand> vec) {
        if (filterText == null) return true; // No filtering
        if (targetText == null) {
            System.out.println("[ERROR] targetText must not be null");
            return false; // No filtering
        } else if (targetText.length() == 0) {
            System.out.println("[WARNING] targetText is length 0");
            return true;
        }

        int pos = 0;
        HighlightableCommand hc = new HighlightableCommand(targetText);
        for (int i = 0; i < filterText.length(); i++) {
            if (pos == targetText.length()) return false;
            while (targetText.charAt(pos) != filterText.charAt(i)) {
                pos++;
                if (pos == targetText.length()) return false;
            }
            hc.addIndex(pos);
            pos++;
        }
        // hc is found.
        // TODO: Better to use HashMap or HashSet rather than Vector if we need to check duplicate.
        // TODO: Refactor remove duplicated key
        vec.add(hc);
        return true;
    }

    private void updateSelectedSuggestionIndex(int index) {
        // index < 0 is ok, it indicates that no suggestion is available
        if (index >= currentSuggestionVec.size()) index = currentSuggestionVec.size();

        currentSelectedSuggestionIndex = index;
        mTXTextPane.setHighlightLine(currentSelectedSuggestionIndex);
        mTXTextPane.repaint(); // to update highlight
        if (index >= 0) {
            try {
                System.out.printf("setCaretPosition to " + mTXTextPane.getLineEndOffset(currentSelectedSuggestionIndex) + ", " + currentSelectedSuggestionIndex);
                //mTXTextPane.scrollRectToVisible(mTXTextPane.modelToView(mTXTextPane.getLineStartOffset(currentSelectedSuggestionIndex)));
                mTXTextPane.setCaretPosition(mTXTextPane.getLineStartOffset(currentSelectedSuggestionIndex));
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }

    public void appendRXText(final String str) {
        //System.out.println("[DEBUG] appendRXText " + str);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                mRXTextArea.append(str);
                if (mRXTextAreaAutoScrollCheckBox.isSelected()) {
                    /* Scroll to bottom */
                    try {
                        mRXTextArea.setCaretPosition(mRXTextArea.getLineStartOffset(mRXTextArea.getLineCount() - 1));
                    } catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                }
            }
        });
    }

    public void setRXText(final String str) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                mRXTextArea.setText(str);
                if (mRXTextAreaAutoScrollCheckBox.isSelected()) {
                    /* Scroll to bottom */
                    mRXTextArea.setCaretPosition(mRXTextArea.getDocument().getLength());
                }
            }
        });
    }

    public void replaceLastlineRXText(final String str) {
        //System.out.println("[DEBUG] replaceLastlineRXText " + str);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                int lastLine = mRXTextArea.getLineCount() - 1;
                try {
                    //System.out.println("[DEBUG]last line -> " + lastLine
                    //        + ", mRXTextArea.getLineStartOffset(lastLine) " + mRXTextArea.getLineStartOffset(lastLine)
                    //        + ", mRXTextArea.getLineEndOffset(lastLine) " + mRXTextArea.getLineEndOffset(lastLine));
                    mRXTextArea.replaceRange(str,
                            mRXTextArea.getLineStartOffset(lastLine),
                            mRXTextArea.getLineEndOffset(lastLine));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                if (mRXTextAreaAutoScrollCheckBox.isSelected()) {
                    /* Scroll to bottom */
                    mRXTextArea.setCaretPosition(mRXTextArea.getDocument().getLength());
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand() + " received");
        if(e.getActionCommand().equals(ACTION_OUTPUT_STREAM)) {
            String command = mTXTextField.getText().trim();
            /* stdout TextArea */
            if (command.startsWith(TTLMacroConfig.TTL_PREFIX)) {
                /* [ttl] Execute TTLMacro */
                /* 1st: delete TXTextField text before execute actual macro command */
                for (int i = 0; i <= mTXTextField.getText().length(); i++) {
                    // execute one time more than enough
                    portTX.transmitAscii(8); // CTRL-H
                }
                /* 2nd: execute macro if exists. */
                if (ttlMacroConfig.ttlMacroMap.containsKey(command)) {
                    String fileName = ttlMacroConfig.ttlMacroMap.get(command);
                    TTLMacroExecutor ttlMacroExecutor = new TTLMacroExecutor(fileName, portTX);
                    // Run another thread for TTL Macro.
                    // TODO: review
                    ttlMacroExecutor.start();
                }
            } else {
                portTX.transmitNewLine();
            }
            updateTXTextFieldText("");

            /* Update command history */
            if (command.length() > 0) {
                System.out.println("Command: " + command);
                mCH.insertCommand(command);
            }
        }
    }

    /**
     * Updates mTXTextField text without side effect of Document Listener
     * @param str
     */
    public void updateTXTextFieldText(final String str) {
        currentExpectedTXText = str;
        synchronized (mTXTextField) {
            if (SwingUtilities.isEventDispatchThread()) {
                mTXTextField.setText(str);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            mTXTextField.setText(str);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Appends str to mTXTextField text without side effect of Document Listener
     * @param str
     */
    public void appendTXTextFieldText(final String str) {
        currentExpectedTXText = mTXTextField.getText() + str;

        synchronized (mTXTextField) {
            if (SwingUtilities.isEventDispatchThread()) {
                int len = mTXTextField.getDocument().getLength();
                mTXTextField.setCaretPosition(len);
                mTXTextField.replaceSelection(str);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            int len = mTXTextField.getDocument().getLength();
                            mTXTextField.setCaretPosition(len);
                            mTXTextField.replaceSelection(str);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifyCurrentConnectionState(int state) {
        switch(state) {
            case STATE_DISCONNECTED:
                // UI update
                mTXTextField.setEditable(false);
                if (mCH != null) {
                    mCH.save();
                }
                break;
            case STATE_CONNECTING:
                mTXTextField.setEditable(false);

                break;
            case STATE_CONNECTED:
                mTXTextField.setEditable(true);
                /* portTX & portRX should be ready now */
                portName = SPTerminal.getSerialPortManager().getCurrentPortName();
                portTX = SPTerminal.getSerialPortManager().getPortTX();
                portRX = SPTerminal.getSerialPortManager().getPortRX();
                mCH = CommandHistory.load(portName);
                break;
            default:
                System.out.println("Unknown state: " + state);
                break;
        }
    }

    public int Finalize() {
        int ret = -1;
        if (mCH != null) {
            mCH.save();
        }
        ret = 0;
        return ret;
    }


    /*--- INNER CLASS ---*/
    private class TXTextFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("keyPressed: e.getKeyCode() = " + e.getKeyCode());

            if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    /* CTRL key pressed */
                    /* Reference for Ascii code: http://www.physics.udel.edu/~watson/scen103/ascii.html */
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        portTX.transmitAscii(1); break;
                    case KeyEvent.VK_B:
                        portTX.transmitAscii(2); break;
                    case KeyEvent.VK_C:
                        portTX.transmitAscii(3); break;
                    case KeyEvent.VK_D:
                        portTX.transmitAscii(4); break;
                    case KeyEvent.VK_E:
                        portTX.transmitAscii(5); break;
                    case KeyEvent.VK_F:
                        portTX.transmitAscii(6); break;
                    case KeyEvent.VK_G:
                        portTX.transmitAscii(7); break;
                    case KeyEvent.VK_H:
                    case KeyEvent.VK_BACK_SPACE:
                            /*
                             * Delete both in/out text is done by removeUpdate method,
                             * So not necessary to send ascii code here.
                             */
                        //portTX.transmitAscii(8);
                        break;
                    case KeyEvent.VK_I:
                        portTX.transmitAscii(9); break;
                    case KeyEvent.VK_J:
                        portTX.transmitAscii(10); break;
                    case KeyEvent.VK_K:
                        portTX.transmitAscii(11); break;
                    case KeyEvent.VK_L:
                        portTX.transmitAscii(12); break;
                    case KeyEvent.VK_M:
                        portTX.transmitAscii(13); break;
                    case KeyEvent.VK_N:
                        portRX.setTabFlag();
                        portTX.transmitAscii(14);
                        e.consume();
                        break;
                    case KeyEvent.VK_O:
                        portTX.transmitAscii(15); break;
                    case KeyEvent.VK_P:
                        portRX.setTabFlag();
                        portTX.transmitAscii(16);
                        e.consume();
                        break;
                    case KeyEvent.VK_Q:
                        portTX.transmitAscii(17); break;
                    case KeyEvent.VK_R:
                        portTX.transmitAscii(18); break;
                    case KeyEvent.VK_S:
                        portTX.transmitAscii(19); break;
                    case KeyEvent.VK_T:
                        portTX.transmitAscii(20); break;
                    case KeyEvent.VK_U:
                        portTX.transmitAscii(21); break;
                    case KeyEvent.VK_V:
                        portTX.transmitAscii(22);
                        //TODO: Reconfirm side-effect, paste function is assigned to C-V for now.
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        try {
                            String pasteText = (String) clipboard.getData(DataFlavor.stringFlavor);
                            System.out.println("paste text: " + pasteText);
                            if (pasteText.contains("\n")) {
                                    /* Multi-line text, execute it. */
                                MultilineCommandConfirmDialog dialog =
                                        new MultilineCommandConfirmDialog(SPTerminal.getFrame(), pasteText);
                                dialog.setLocationRelativeTo(null);
                                dialog.setVisible(true);
                                switch (dialog.getOption()) {
                                    case JOptionPane.OK_OPTION:  // JOptionPane.YES_OPTION is same
                                            /* transmit pasteText, added "\n" is to ensure executing last command */
                                        portTX.transmitString(pasteText + "\n");
                                        break;
                                    case JOptionPane.NO_OPTION:
                                    case JOptionPane.CANCEL_OPTION:
                                    default:
                                        // Cancel operation, do nothing
                                        break;
                                }
                                e.consume(); // prevent default behavior
                            } else {
                                    /* Single-line text, proceed with default behavior, do nothing here. */
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        break;
                    case KeyEvent.VK_W:
                        portTX.transmitAscii(23); break;
                    case KeyEvent.VK_X:
                        portTX.transmitAscii(24); break;
                    case KeyEvent.VK_Y:
                        portTX.transmitAscii(25); break;
                    case KeyEvent.VK_Z:
                        portTX.transmitAscii(26); break;
                    case KeyEvent.VK_OPEN_BRACKET: // "["
                        portTX.transmitAscii(27); break;
                    case KeyEvent.VK_BACK_SLASH: // "\"
                        portTX.transmitAscii(28); break;
                    case KeyEvent.VK_CLOSE_BRACKET: // "]"
                        portTX.transmitAscii(29); break;
                    case KeyEvent.VK_CIRCUMFLEX: // "^"
                        portTX.transmitAscii(30); break;
                    case KeyEvent.VK_UNDERSCORE: // "_"
                        portTX.transmitAscii(31); break;
                    case KeyEvent.VK_SLASH: // "/"
                        break;
                    case KeyEvent.VK_SPACE:
                        break;
                    case KeyEvent.VK_DELETE:
                            /* Delete command from CommandHistoryMap */
                        mCH.commandHistoryMap.remove(currentSuggestionVec.get(currentSelectedSuggestionIndex).command);
                        updateSuggestion();
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                            /* Ctrl-Enter to auto-fill suggestion */
                        if (currentSelectedSuggestionIndex >= 0) {
                            String suggestion = currentSuggestionVec.get(currentSelectedSuggestionIndex).command;
                            mTXTextField.setText(suggestion);  // We want to have take effect.
                        }
                        break;
                    default:
                        break;
                }
            }

            switch (e.getKeyCode()) {
                // Usual key press (No modifier)
                case KeyEvent.VK_TAB:
                    System.out.println("TAB pressed");
                    if (portRX != null) {
                        portRX.setTabFlag();
                    }
                    portTX.transmitString("\t");
                    break;
                case KeyEvent.VK_ESCAPE:
                    portTX.transmitAscii(27);
                    e.consume();
                    break;
                case KeyEvent.VK_UP:  // TODO: refactor
                        /* move up suggestion */
                    if (currentSuggestionVec != null) {
                        if (currentSuggestionVec.size() == 0) {
                            updateSelectedSuggestionIndex(-1);
                        } else {
                            updateSelectedSuggestionIndex((currentSelectedSuggestionIndex - 1 + currentSuggestionVec.size()) % currentSuggestionVec.size());
                        }
                    }
                    e.consume();
                    break;
                case KeyEvent.VK_DOWN:  // TODO: refactor
                        /* move down suggestion */
                    if (currentSuggestionVec != null) {
                        if (currentSuggestionVec.size() == 0) {
                            updateSelectedSuggestionIndex(-1);
                        } else {
                            updateSelectedSuggestionIndex((currentSelectedSuggestionIndex + 1) % currentSuggestionVec.size());
                        }
                    }
                    e.consume();
                    break;
                case KeyEvent.VK_DELETE:
                        /* Do nothing, let it be default behavior
                         * NOTE: Ctrl-DELETE will delete commandhisotry.
                         * Assign same function to usual DELETE key may introduce confusion to user.
                         */
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private class TXTextFieldDocumentListener implements DocumentListener {
        @Override
        public synchronized void insertUpdate(DocumentEvent e) {

            Document doc = e.getDocument();  // document that fired the event
            //e.getLength();  // length of the change
            //e.getOffset();  // offset that the first character changed
            System.out.println("[DEBUG] insertUpdate e.getOffset: " + e.getOffset() + ", e.getLength: " + e.getLength() + ", doc.getLength: " + doc.getLength());
            try {
                String currentDoc = doc.getText(0, doc.getLength());
                if (!currentDoc.equals(currentExpectedTXText)) {  // Ignore to transmit if currentDoc is already expected
                    if (e.getOffset() + e.getLength() == doc.getLength()) {
                            /* Inserted at last */
                        // only append
                        String str;
                        try {
                            str = doc.getText(e.getOffset(), e.getLength());
                            portTX.transmitString(str);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace();
                        }
                    } else {
                            /* Inserted at middle */
                        // Delete
                        int deleteNum = doc.getLength() - e.getOffset() - e.getLength();

                        for (int i = 0; i < deleteNum; i++) {
                            portTX.transmitAscii(8); // CTRL-H
                        }
                        // Append
                        String str;
                        try {
                            str = doc.getText(e.getOffset(), doc.getLength() - e.getOffset());
                            portTX.transmitString(str);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace();
                        }
                    }
                }
                currentExpectedTXText = currentDoc;

                    /* update suggestion */
                updateSuggestion();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public synchronized void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();  // document that fired the event
            System.out.println("[DEBUG] removeUpdate e.getOffset: " + e.getOffset() + ", e.getLength: " + e.getLength() + ", doc.getLength: " + doc.getLength());
            try {
                String currentDoc = doc.getText(0, doc.getLength());
                if (!currentDoc.equals(currentExpectedTXText)) {  // Ignore to transmit if currentDoc is already expected
                    if (e.getOffset() == doc.getLength()) {
                            /* Deleted at last */
                        // Delete
                        int deleteNum = e.getLength();
                        for (int i = 0; i < deleteNum; i++) {
                            portTX.transmitAscii(8); // CTRL-H
                        }
                    } else {
                            /* Deleted at middle */
                        // Delete
                        int deleteNum = doc.getLength() + e.getLength() - e.getOffset();
                        for (int i = 0; i < deleteNum; i++) {
                            portTX.transmitAscii(8); // CTRL-H
                        }
                        // Append
                        String str;
                        try {
                            str = doc.getText(e.getOffset(), doc.getLength() - e.getOffset());
                            portTX.transmitString(str);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace();
                        }
                    }
                }
                ///* Temporal counter measure, delete extra try to delete unseen bell text etc. */
                //// TODO: refactor
                //if (e.getDocument().getLength() == 0) {
                //    System.out.println("document length == 0, deleting extra to ensure empty...");
                //    for (int i = 0; i < 10; i++) {
                //        portTX.transmitAscii(8); // CTRL-H
                //    }
                //}
                currentExpectedTXText = currentDoc;

                    /* update suggestion */
                updateSuggestion();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
                /* Called when atrribute change happens */
        }
    }
}
