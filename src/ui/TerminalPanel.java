package ui;

import data.CommandHistory;
import serial.SerialPortRX;
import serial.SerialPortTX;
import ui.component.HighlightableTextArea;

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

import static ui.SPTerminal.*;

/**
 *
 */
public class TerminalPanel extends JPanel implements ActionListener {


    /* Constants */
    /* Typing mode */
    /** Default mode */
    public static final int TYPING_MODE_TX = 1;
    /** Suggestion mode, some shortcut keys are assigned for suggestion, and not sent through TX */
    public static final int TYPING_MODE_SUGGESTION = 2;

    public static final String ACTION_OUTPUT_STREAM = "actionOutputStream";
    public static final String STATUS_TEXT_TX_MODE = "<html>Current mode: Direct input mode. Press Ctrl-Space to enter suggestion mode<br/>"
            + "TAB auto-completion is not fully supported now.</html>";
    public static final String STATUS_TEXT_SUGGESTION_MODE = "<html>Current mode: Suggestion mode. Press Ctrl-Space to go back to direct input mode<br/>"
            + "&nbsp;</html>";
    public static final int COMMAND_HISTORY_MAX_SIZE = 3000;

    /* Relation */
    private SerialPortTX portTX = null;
    private SerialPortRX portRX = null;

    /* Attribute */
    private final JCheckBox mRXTextAreaAutoScrollCheckBox;
    private final JTextArea mRXTextArea;
    private final JLabel mTXStatusLabel;  // Shows some tips about TXTextField
    private final JTextField mTXTextField;
    private final HighlightableTextArea mTXTextArea;
    private CommandHistory mCH;
    private String portName;
    private String currentExpectedTXText = "";
    private int currentTypingMode = TYPING_MODE_TX;
    private Vector<String> currentSuggestionVec = new Vector<>();
    private int currentSelectedSuggestionIndex = 0;
    private String smartSuggestion;
    private Rectangle r = null;

    TerminalPanel() {
        super(new BorderLayout());
        /* |this| contains terminalConfigRowPanel and textAreasPanel in Y_AXIS  */
        JPanel terminalConfigRowPanel = new JPanel();

        mRXTextAreaAutoScrollCheckBox = new JCheckBox("Auto-scroll", true);
        mRXTextAreaAutoScrollCheckBox.setHorizontalTextPosition(JCheckBox.LEFT);
        mRXTextAreaAutoScrollCheckBox.setToolTipText(
                "When enabled, it automatically scroll below text area to bottom when log comes");
        terminalConfigRowPanel.add(mRXTextAreaAutoScrollCheckBox);

        JPanel textAreasPanel = new JPanel();

        JPanel inputStreamTextAreaRowPanel = new JPanel(new BorderLayout());
        JPanel outputStreamTextAreaRowPanel = new JPanel();
        outputStreamTextAreaRowPanel.setLayout(new BoxLayout(outputStreamTextAreaRowPanel, BoxLayout.Y_AXIS));

        mRXTextArea = new JTextArea(40, 75);
        mRXTextArea.setLineWrap(false);

        mTXStatusLabel = new JLabel("");
        //mTXStatusLabel.setBorder(BorderFactory.createLineBorder(Color.red));
        mTXStatusLabel.setPreferredSize(new Dimension(600, 50));
        mTXStatusLabel.setMinimumSize(new Dimension(600, 50));
        mTXStatusLabel.setMaximumSize(new Dimension(600, 50));

        mTXTextField = new JTextField();
        mTXTextField.setActionCommand(ACTION_OUTPUT_STREAM);
        mTXTextField.addActionListener(this);
        mTXTextField.setFocusTraversalKeysEnabled(false);
        mTXTextField.addKeyListener(new KeyListener() {
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
                            if (currentTypingMode == TYPING_MODE_TX) {
                                portTX.transmitAscii(14);
                            } else if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                                /* move down suggestion */
                                if (currentSuggestionVec != null) {
                                    if (currentSuggestionVec.size() == 0) {
                                        updateSelectedSuggestionIndex(-1);
                                    } else {
                                        updateSelectedSuggestionIndex((currentSelectedSuggestionIndex + 1) % currentSuggestionVec.size());
                                    }
                                }
                            }
                            e.consume();
                            break;
                        case KeyEvent.VK_O:
                            portTX.transmitAscii(15); break;
                        case KeyEvent.VK_P:
                            if (currentTypingMode == TYPING_MODE_TX) {
                                portTX.transmitAscii(16);
                            } else if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                                /* move up suggestion */
                                if (currentSuggestionVec != null) {
                                    if (currentSuggestionVec.size() == 0) {
                                        updateSelectedSuggestionIndex(-1);
                                    } else {
                                        updateSelectedSuggestionIndex((currentSelectedSuggestionIndex - 1 + currentSuggestionVec.size()) % currentSuggestionVec.size());
                                    }
                                }
                            }
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
                                    int option = JOptionPane.showConfirmDialog(
                                            SPTerminal.getFrame(),
                                            "Transmit below text?\n" + pasteText,
                                            "Transmit multi-line text",
                                            JOptionPane.OK_CANCEL_OPTION,
                                            JOptionPane.QUESTION_MESSAGE
                                    );
                                    switch (option) {
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
                        case KeyEvent.VK_SPACE:
                            toggleTypingMode();
                            break;
                        case KeyEvent.VK_DELETE:
                            /* Delete command from CommandHistoryMap */
                            mCH.commandHistoryMap.remove(currentSuggestionVec.get(currentSelectedSuggestionIndex));
                            updateSuggestion();
                            e.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                            /* Ctrl-Enter to quickly execute suggested command */
                            if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                                String suggestion = currentSuggestionVec.get(currentSelectedSuggestionIndex);
                                updateTXTextFieldText(suggestion);
                                mTXTextField.requestFocusInWindow();
                                try {
                                    Robot robot = new Robot();
                                    robot.keyPress(KeyEvent.VK_ENTER);
                                } catch (AWTException awte) {
                                    awte.printStackTrace();
                                }
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
                        if (currentTypingMode == TYPING_MODE_TX) {
                            if (portRX != null) {
                                portRX.setTabFlag();
                            }
                            portTX.transmitString("\t");
                        } else if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                            if (currentSelectedSuggestionIndex != -1) {
                                String suggestion = currentSuggestionVec.get(currentSelectedSuggestionIndex);
                                mTXTextField.setText(suggestion);
                            }
                        }
                        break;
                    case KeyEvent.VK_UP:  // TODO: refactor, currently copy from Ctrl-P
                        if (currentTypingMode == TYPING_MODE_TX) {
                            portTX.transmitAscii(16);
                        } else if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                                /* move up suggestion */
                            if (currentSuggestionVec != null) {
                                if (currentSuggestionVec.size() == 0) {
                                    updateSelectedSuggestionIndex(-1);
                                } else {
                                    updateSelectedSuggestionIndex((currentSelectedSuggestionIndex - 1 + currentSuggestionVec.size()) % currentSuggestionVec.size());
                                }
                            }
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_DOWN:  // TODO: refactor, currently copy from Ctrl-P
                        if (currentTypingMode == TYPING_MODE_TX) {
                            portTX.transmitAscii(14);
                        } else if (currentTypingMode == TYPING_MODE_SUGGESTION) {
                                /* move down suggestion */
                            if (currentSuggestionVec != null) {
                                if (currentSuggestionVec.size() == 0) {
                                    updateSelectedSuggestionIndex(-1);
                                } else {
                                    updateSelectedSuggestionIndex((currentSelectedSuggestionIndex + 1) % currentSuggestionVec.size());
                                }
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
        });
        mTXTextField.getDocument().addDocumentListener(new DocumentListener() {

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
                    if (e.getDocument().getLength() == 0) {
                        System.out.println("document length == 0, deleting extra to ensure empty...");
                        for (int i = 0; i < 10; i++) {
                            portTX.transmitAscii(8); // CTRL-H
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
            public void changedUpdate(DocumentEvent e) {
                /* Called when atrribute change happens */
            }
        });

        mTXTextArea = new HighlightableTextArea(30, 75);
        mTXTextArea.setLineWrap(false);

        JScrollPane inputStreamScrollPane = new JScrollPane(mRXTextArea);
        JScrollPane outputStreamScrollPane = new JScrollPane(mTXTextArea);


        inputStreamTextAreaRowPanel.add(inputStreamScrollPane, BorderLayout.CENTER);

        outputStreamTextAreaRowPanel.add(mTXTextField);
        outputStreamTextAreaRowPanel.add(mTXStatusLabel);
        outputStreamTextAreaRowPanel.add(outputStreamScrollPane);

        textAreasPanel.setLayout(new BoxLayout(textAreasPanel, BoxLayout.X_AXIS));
        textAreasPanel.add(inputStreamTextAreaRowPanel);
        textAreasPanel.add(outputStreamTextAreaRowPanel);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(terminalConfigRowPanel);
        this.add(textAreasPanel);

        updateTypingMode(TYPING_MODE_TX);
    }

    /**
     * update suggestion from CommandHistory using auto-suggestion algorithm
     */
    private void updateSuggestion() {
        /* Update Command history auto-completion */
        smartSuggestion = "";
        Vector<String> suggestionVec = new Vector<>();
        if (currentExpectedTXText.length() <= 0) {  // Only when no characted inputted
            /* get suggestion from commandHistoryVec */
            // Show all history up to 3000 (COMMAND_HISTORY_MAX_SIZE).
            int lb = Math.max(0, mCH.commandHistoryVec.size() - COMMAND_HISTORY_MAX_SIZE);
            for (int i = mCH.commandHistoryVec.size() - 1; i >= lb; i--) {
                String cmd = mCH.commandHistoryVec.get(i);
                smartSuggestion += cmd + "\n";
                suggestionVec.add(cmd);
            }
        } else {
            /* get suggestion from commandHistoryMap */
            for (Map.Entry<String, Long> e : mCH.commandHistoryMap.entrySet()) {
                String cmd = e.getKey();
                //if (cmd.contains(currentExpectedTXText)) {  // Algorithm 1. just check if text is contained or not
                if (isMatch(cmd, currentExpectedTXText)) {
                    smartSuggestion += cmd + "\n";
                    suggestionVec.add(cmd);
                }
            }
        }
        mTXTextArea.setText(smartSuggestion);
        if (currentTypingMode != TYPING_MODE_SUGGESTION || suggestionVec.size() == 0) {
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

    private void updateSelectedSuggestionIndex(int index) {
        // index < 0 is ok, it indicates that no suggestion is available
        if (index >= currentSuggestionVec.size()) index = currentSuggestionVec.size();

        currentSelectedSuggestionIndex = index;
        mTXTextArea.setHighlightLine(currentSelectedSuggestionIndex);
        mTXTextArea.repaint(); // to update highlight
        if (index >= 0) {
            try {
                System.out.printf("setCaretPosition to " + mTXTextArea.getLineEndOffset(currentSelectedSuggestionIndex) + ", " + currentSelectedSuggestionIndex);
                //mTXTextArea.scrollRectToVisible(mTXTextArea.modelToView(mTXTextArea.getLineStartOffset(currentSelectedSuggestionIndex)));
                mTXTextArea.setCaretPosition(mTXTextArea.getLineStartOffset(currentSelectedSuggestionIndex));
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
                    mRXTextArea.setCaretPosition(mRXTextArea.getDocument().getLength());
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
            /* stdout TextArea */
            //CommPortSender.send(new ProtocolImpl().getMessage(mTXTextField.getText()));
            portTX.transmitString("\n");

            /* Update command history */
            String command = mTXTextField.getText().trim();
            if (command.length() > 0) {
                System.out.println("Command: " + command);
                mCH.insertCommand(command);
            }

            //mTXTextField.setRXText("");  // Reset textField
            // disable removeUpdate callback for this setRXText update.
            //updateTXTextFieldText(""); // Exception -> Cannot call invokeAndWait from the event dispatcher thread
            currentExpectedTXText = "";
            mTXTextField.setText("");
        }
    }

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

    public void appendTXTextFieldText(final String str) {
        updateTXTextFieldText(mTXTextField.getText() + str);
    }

    /** toggle {@link #currentExpectedTXText} between {@link #TYPING_MODE_TX} and {@link #TYPING_MODE_SUGGESTION} */
    private void toggleTypingMode() {
        if (currentTypingMode == TYPING_MODE_TX) {
            updateTypingMode(TYPING_MODE_SUGGESTION);
        } else {
            updateTypingMode(TYPING_MODE_TX);
        }
    }

    /**
     * update {@link #currentExpectedTXText} to typingMode
     * @param typingMode
     */
    private void updateTypingMode(int typingMode) {
        currentTypingMode = typingMode;
        switch (currentTypingMode) {
            case TYPING_MODE_TX:
                mTXStatusLabel.setText(STATUS_TEXT_TX_MODE);
                updateSelectedSuggestionIndex(-1);
                break;
            case TYPING_MODE_SUGGESTION:
                mTXStatusLabel.setText(STATUS_TEXT_SUGGESTION_MODE);
                if (currentSuggestionVec == null || currentSuggestionVec.size() == 0) {
                    updateSelectedSuggestionIndex(-1);  // No suggestion available
                } else {
                    updateSelectedSuggestionIndex(0);   // Reset index for new suggestion
                }
                break;
            default:
                System.out.println("[ERROR] Unhandled typingMode = " + typingMode);
                break;
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
}
