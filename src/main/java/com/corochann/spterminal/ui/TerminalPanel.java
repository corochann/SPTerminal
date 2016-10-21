package com.corochann.spterminal.ui;

import com.corochann.spterminal.config.FilterConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.config.teraterm.TTLMacroConfig;
import com.corochann.spterminal.data.CommandHistory;
import com.corochann.spterminal.data.model.FilterRule;
import com.corochann.spterminal.data.model.HighlightableCommand;
import com.corochann.spterminal.serial.SerialPortRX;
import com.corochann.spterminal.serial.SerialPortTX;
import com.corochann.spterminal.teraterm.TTLMacroExecutor;
import com.corochann.spterminal.ui.component.*;
import com.corochann.spterminal.ui.menu.FilterConfigDialog;
import com.corochann.spterminal.ui.menu.RXTextPopupMenu;
import com.corochann.spterminal.util.MyUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.corochann.spterminal.ui.SPTerminal.*;

/**
 *
 */
public class TerminalPanel extends JPanel implements ActionListener {


    /* Constants */
    public static final String ACTION_OUTPUT_STREAM = "actionOutputStream";
    public static final String ACTION_FIND_NEXT = "findnext";
    public static final String ACTION_FIND_PREV = "findprev";
    public static final String ACTION_FIND_END = "findend";
    public static final String ACTION_FILTER_SETUP = "filtersetup";
    public static final String STATUS_TEXT_DEFAULT = "<html>" +
            "TAB auto-completion sometimes not work correctly...<br/>" +
            "Use ↑, ↓ keys to move suggestion, Ctrl+Enter to auto-fill with selected suggestion.<br/>" +
            "Ctrl+Delete to remove item from suggestion list (but it will not delete command history).</html>";
    public static final int COMMAND_HISTORY_MAX_SIZE = 3000;
    // filter
    public static final String NO_FILTER = "No filter";
    public static final String ADD_FILTER = "Add new filter";
    public static final String[] FILTER_TABLE_COLUMN_NAMES = new String[]{"query", "count"};
    public static final int FILTER_TABLE_QUERY_COL = 0;
    public static final int FILTER_TABLE_COUNT_COL = 1;

    /**
     * view state defines which log to be shown.
     * 1. Main log: All log (non-filtered log)
     * 2. Filtered log
     */
    public static final int VIEW_STATE_MAIN_LOG = 1;
    public static final int VIEW_STATE_FILTERED_LOG = 2;

    /** Layout size */
    public static final int RIGHT_PANEL_WIDTH = 300;

    /** highlight color for find text */
    private static final Highlighter.HighlightPainter highlightPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0, 180, 200));  // dark cyan
    /** highlight color for current selected find text */
    private static final Highlighter.HighlightPainter currentHighlightPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0, 50, 200));  // dark blue

    /* Relation */
    private SerialPortTX portTX = null;
    private SerialPortRX portRX = null;

    /* Attribute */
    // UI
    private final AnsiJTextPane mRXTextPane;
    private final AnsiJTextPane mRXFilteredTextPane;
    private RXTextPopupMenu mRXTextPopupMenu;
    private final CustomJTextField mTXTextField;
    private final HighlightableJTextPane mTXTextPane;
    // findPanel
    private final JPanel findPanel;
    private final CustomJTextField findQueryTextField;
    private final JCheckBox regexCheckBox;
    private final JCheckBox matchCaseCheckBox;
    private final JLabel foundNumberLabel;
    // filterPanel
    private final CustomJButton filterSetupButton;
    private final CustomJComboBox filterNameComboBox;
    private final JTable filterCountTable;

    private CommandHistory mCH;
    private String portName;
    private String currentExpectedTXText = "";
    private Vector<HighlightableCommand> currentSuggestionVec = new Vector<>();
    private int currentSelectedSuggestionIndex = 0;
    private int currentViewState = VIEW_STATE_MAIN_LOG;

    private final Color defaultForeGroundColor;
    private final Color charHighlightColor;
    private TTLMacroConfig ttlMacroConfig;
    private FilterConfig filterConfig;
    private int findCurrentPos = 0;
    private final AutoScrollJScrollPane mInputStreamScrollPane;
    private final AutoScrollJScrollPane mInputStreamFilteredScrollPane;
    private final JList filterList;
    private String currentFilterRuleName;
    private FilterRule currentFilterRule;
    private FilterCountTableModel currentFilterTableModel;


    TerminalPanel() {
        super(new BorderLayout());
        ttlMacroConfig = SPTerminalPreference.getInstance().getTTLMacroConfig();
        filterConfig = SPTerminalPreference.getInstance().getFilterConfig();
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

        /*--- LEFT Panel ---*/
        mRXTextPane = new AnsiJTextPane(styleConfig);
        mInputStreamScrollPane = new AutoScrollJScrollPane(mRXTextPane);
        setupRXTextPane(mRXTextPane, mInputStreamScrollPane);

        mRXFilteredTextPane = new AnsiJTextPane(styleConfig);
        mInputStreamFilteredScrollPane = new AutoScrollJScrollPane(mRXFilteredTextPane);
        setupRXTextPane(mRXFilteredTextPane, mInputStreamFilteredScrollPane);
        //mRXFilteredTextPane.setVisible(false);  // default to invisible
        ((AbstractDocument) mRXFilteredTextPane.getDocument()).setDocumentFilter(new FilteredTextPaneDocumentFilter()); // Document handling logic

          /* findPanel setup */
        findPanel = new JPanel();
        JLabel findLabel = new JLabel("Find:");
        findQueryTextField = new CustomJTextField();
        findQueryTextField.setPreferredWidth(150);
        findQueryTextField.addActionListener(this);
        findQueryTextField.setActionCommand(ACTION_FIND_NEXT);
        findQueryTextField.addKeyListener(new FindQueryTextFieldKeyListener());  // Key handling logic
        findQueryTextField.getDocument().addDocumentListener(new FindQueryTextFieldDocumentListener()); // Document handling logic
        matchCaseCheckBox = new JCheckBox("match case");
        matchCaseCheckBox.setSelected(false);
        regexCheckBox = new JCheckBox("regex");
        regexCheckBox.setSelected(false);
        foundNumberLabel = new JLabel("0 / 0"); // Initial text
        //foundNumberLabel.setMinimumSize(new Dimension(
        //        100,
        //        (int) foundNumberLabel.getPreferredSize().getHeight()
        //));

        CustomJButton prevButton = new CustomJButton("↑", styleConfig);
        prevButton.addActionListener(this);
        prevButton.setActionCommand(ACTION_FIND_PREV);
        prevButton.setToolTipText("Shortcut key: Ctrl-r");
        prevButton.setMargin(new Insets(2,2,2,2));
        prevButton.setPreferredSize(new Dimension(26,26));

        CustomJButton nextButton = new CustomJButton("↓", styleConfig);
        nextButton.addActionListener(this);
        nextButton.setActionCommand(ACTION_FIND_NEXT);
        nextButton.setToolTipText("Shortcut key: Ctrl-s");
        nextButton.setMargin(new Insets(2,2,2,2));
        nextButton.setPreferredSize(new Dimension(26,26));
        //System.out.println("nextButton preferred height " + nextButton.getPreferredSize().getHeight()); //26

        CustomJButton endFindButton = new CustomJButton("×", styleConfig); // TODO: use 'X' icon
        endFindButton.addActionListener(this);
        endFindButton.setActionCommand(ACTION_FIND_END);
        endFindButton.setToolTipText("Shortcut key: ESC");
        endFindButton.setMargin(new Insets(2,2,2,2));
        endFindButton.setPreferredSize(new Dimension(26,26));


        findPanel.add(findLabel);
        findPanel.add(findQueryTextField);
        findPanel.add(foundNumberLabel);
        findPanel.add(prevButton);
        findPanel.add(nextButton);
        findPanel.add(matchCaseCheckBox);
        findPanel.add(regexCheckBox);
        findPanel.add(endFindButton);
        findPanel.setVisible(false);  // default to invisible

        terminalLeftPanel.add(mInputStreamScrollPane);
        terminalLeftPanel.add(mInputStreamFilteredScrollPane);
        terminalLeftPanel.add(findPanel);

        terminalSplitPane.setLeftComponent(terminalLeftPanel);

        /*--- RIGHT Panel ---*/
        /* 1. TXTextField for typing command */
        mTXTextField = new CustomJTextField();
        mTXTextField.setToolTipText(STATUS_TEXT_DEFAULT);
        mTXTextField.setPreferredSize(new Dimension(
                RIGHT_PANEL_WIDTH,
                mTXTextField.getPreferredSize().height
        ));
        mTXTextField.setActionCommand(ACTION_OUTPUT_STREAM);
        mTXTextField.addActionListener(this);
        mTXTextField.setFocusTraversalKeysEnabled(false);  // Disable side effect of TAB key
        mTXTextField.addKeyListener(new TXTextFieldKeyListener());  // Key handling logic
        mTXTextField.getDocument().addDocumentListener(new TXTextFieldDocumentListener()); // Document handling logic


        /* 2. TXTextPane for suggesting command */
        mTXTextPane = new HighlightableJTextPane();
        mTXTextPane.setEditable(false);
        if (styleConfig.getLineHighlightColor() != null) {
            mTXTextPane.setHighlightColor(styleConfig.getLineHighlightColor());
        }

        //mTXTextPane.setLineWrap(false);
        JScrollPane outputStreamScrollPane = new JScrollPane(mTXTextPane);
        outputStreamScrollPane.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 400));

        /* 3. filterPanel for log filtering feature */
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        /* 3.1 filterSelectPanel for log filter selection */
        JPanel filterSelectPanel = new JPanel();
        filterSelectPanel.setLayout(new BoxLayout(filterSelectPanel, BoxLayout.X_AXIS));

        JLabel filterNameLabel = new JLabel("Log filter");
        filterNameComboBox = new CustomJComboBox();
        Vector<String> filterNameListValue = constructFilterNameListData();
        filterNameComboBox.setModel(new DefaultComboBoxModel(filterNameListValue));
        filterNameComboBox.setSelectedItem(NO_FILTER);
        filterNameComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // !((JComboBox) e.getSource()).isPopupVisible()
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String filterRuleName = (String)e.getItem();
                    switch (filterRuleName) {
                        case NO_FILTER:
                            notifyViewStateChanged(VIEW_STATE_MAIN_LOG, filterRuleName);
                            break;
                        default:
                            notifyViewStateChanged(VIEW_STATE_FILTERED_LOG, filterRuleName);
                            break;
                    }
                }
            }
        });
        filterSetupButton = new CustomJButton(styleConfig);
        filterSetupButton.setText("setup");
        filterSetupButton.setActionCommand(ACTION_FILTER_SETUP);
        filterSetupButton.addActionListener(this);
        filterSetupButton.setMargin(new Insets(2,5,2,5));
        //filterSetupButton.setPreferredSize(new Dimension(60,26));

        filterSelectPanel.add(filterNameLabel);
        filterSelectPanel.add(filterNameComboBox);
        filterSelectPanel.add(filterSetupButton);

        /* 3.1 filterCountSP shows the current count of log filtering */
        Object[][] tableData = {
                {"query1", 100},
                {"query2", 200}
        };
        FilterCountTableModel tableModel = new FilterCountTableModel(tableData, FILTER_TABLE_COLUMN_NAMES);

        filterCountTable = new JTable();
        filterCountTable.setModel(tableModel);
        filterCountTable.setFocusable(false);
        filterCountTable.setCellSelectionEnabled(false);
        //filterCountTable.setDragEnabled(false);


        JScrollPane filterCountSP = new JScrollPane(filterCountTable);

        filterPanel.add(filterSelectPanel);
        filterPanel.add(filterCountSP);

        /* 3. filterScrollPane for log filtering feature */
        filterList = new JList();
        //Vector<String> filterListValue = new Vector<>();
        //filterListValue.add(NO_FILTER);
        //filterListValue.add(ADD_FILTER);
        Vector<String> filterRuleListValue = constructFilterRuleListData();
        filterList.setListData(filterRuleListValue);
        filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterList.setSelectedValue(NO_FILTER, true);
        // SelectionListener
        filterList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    String filterRuleName = (String) list.getSelectedValue();
                    if (filterRuleName == null) return; //TODO: handle null case.
                    switch (filterRuleName) {
                        case NO_FILTER:
                            // do nothing
                            notifyViewStateChanged(VIEW_STATE_MAIN_LOG, filterRuleName);
                            break;
                        case ADD_FILTER:
                            SPTerminal frame = SPTerminal.getFrame();
                            new FilterConfigDialog(frame, filterRuleName).showDialog();
                            // Go back from dialog, filterRuleListValue may be updated.
                            Vector<String> filterRuleListValue = constructFilterRuleListData();
                            filterList.setListData(filterRuleListValue);
                            break;
                        default:
                            notifyViewStateChanged(VIEW_STATE_FILTERED_LOG, filterRuleName);
                            break;
                    }
                }
            }
        });
        // Double click detection
        filterList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList theList = (JList) e.getSource();
                if (e.getClickCount() == 2) {  // Detect double click
                    int index = theList.locationToIndex(e.getPoint());
                    SPTerminal frame = SPTerminal.getFrame();
                    FilterConfigDialog filterConfigDialog;
                    if (index >= 0) {
                        String filterRuleName = (String)theList.getModel().getElementAt(index);
                        System.out.println("Double-clicked on: " + filterRuleName);
                        switch (filterRuleName) {
                            case NO_FILTER:
                                // do nothing
                                break;
                            case ADD_FILTER:
                            default:
                                filterConfigDialog = new FilterConfigDialog(frame, filterRuleName);
                                filterConfigDialog.showDialog();
                                // Go back from dialog, filterRuleListValue may be updated.
                                Vector<String> filterRuleListValue = constructFilterRuleListData();
                                filterList.setListData(filterRuleListValue);
                                break;
                        }
                    }
                }
            }
        });
        JScrollPane filterSP = new JScrollPane(filterList);
        filterSP.setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 100));

        /* rightVerticalSplitPane handles layout size of 2. and 3. */
        JSplitPane rightVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                true,
                outputStreamScrollPane,
                filterPanel);
                //filterSP);
        rightVerticalSplitPane.setOneTouchExpandable(true);
        rightVerticalSplitPane.setDividerSize(10);
        rightVerticalSplitPane.setDividerLocation(0.8);
        //rightVerticalSplitPane.setDividerLocation(600);

        mTXTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightVerticalSplitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        terminalRightPanel.add(mTXTextField);
        terminalRightPanel.add(rightVerticalSplitPane);

        terminalSplitPane.setRightComponent(terminalRightPanel);

        /*--- Font setting ---*/
        if (styleConfig.getTerminalFont() != null) {
            //mRXTextPane.setFont(styleConfig.getTerminalFont());
            //mRXFilteredTextPane.setFont(styleConfig.getTerminalFont());
            mTXTextField.setFont(styleConfig.getTerminalFont());
            mTXTextPane.setFont(styleConfig.getTerminalFont());
        }

        /*--- PopupMenu setting ---*/
        mRXTextPopupMenu = new RXTextPopupMenu();
        mRXTextPopupMenu.setRXTextPopupMenuListener(new RXTextPopupMenu.RXTextPopupMenuListener() {
            @Override
            public void onStartFind(ActionEvent e) {
                startFind();
            }
        });
        mRXTextPane.addMouseListener(mRXTextPopupMenu);
        mRXFilteredTextPane.addMouseListener(mRXTextPopupMenu);

        /* final layout */
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(terminalSplitPane);
        notifyViewStateChanged(VIEW_STATE_MAIN_LOG, null);
    }

    class FilterCountTableModel extends DefaultTableModel {
        FilterCountTableModel(String[] columnNames, int rowNum) {
            super(columnNames, rowNum);


        }

        public FilterCountTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return getValueAt(0, columnIndex).getClass();
        }

        /**
         * Ref: http://stackoverflow.com/questions/1990817/how-to-make-a-jtable-non-editable
         * All cell is not editable
         * @param row
         * @param column
         * @return
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
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

    /**
     * Setup RXTextPane wrapped in AutoScrollJScrollPane
     * @param textPane
     * @param scrollPane
     */
    private void setupRXTextPane(AnsiJTextPane textPane, AutoScrollJScrollPane scrollPane) {
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(600, 400)); // width, height
        textPane.addKeyListener(new RXTextPaneKeyListener());
        textPane.setVerticalScrollBar(scrollPane.getVerticalScrollBar());
        /*
         * Configure JTextPane to not update the cursor position after
         * inserting or appending text to the JTextPane. This disables the
         * default behavior of scrolling automatically whenever
         * inserting or appending text into the JTextPane: we want scrolling
         * to only occur at our discretion, not blindly.
         * NOTE that this breaks normal typing into the JTextPane.
         * This approach assumes that all updates to the ScrollingJTextPane are programmatic.
         */
        //DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        //caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
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

    /*--- RXTextPane Utils ---*/
    public void appendRXText(final String str) {
        //System.out.println("[DEBUG] appendRXText " + str);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                mRXTextPane.append(str);
            }
        });
    }

    public void writeRXText(final byte[] b, final int off, final int len) {
        //System.out.println("[DEBUG] appendRXText " + str);
        /* b will be changed by outside, so we MUST copy this byte[] to another buffer to prevent overwritten */
        final byte[] buffer = new byte[len];
        System.arraycopy(b, off, buffer, 0, len);
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run(){
                    try {
                        //mRXTextPane.write(b, off, len);
                        mRXTextPane.write(buffer, 0, len);
                        mRXTextPane.flush();

                        if (currentViewState == VIEW_STATE_FILTERED_LOG) {
                            mRXFilteredTextPane.write(buffer, 0, len);
                            mRXFilteredTextPane.flush();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
    }

    public void setRXText(final String str) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                mRXTextPane.setText(str);
            }
        });
    }

    public void removeLastlineRXText() {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                int lastLine = mRXTextPane.getLineCount() - 1;
                try {
                    int startOffset = mRXTextPane.getLineStartOffset(lastLine);
                    int endOffset = mRXTextPane.getLineEndOffset(lastLine);
                    System.out.println("[DEBUG]last line -> " + lastLine
                            + ", mRXTextPane.getLineStartOffset(lastLine) " + startOffset
                            + ", mRXTextPane.getLineEndOffset(lastLine) " + endOffset
                            + ", str \n" + mRXTextPane.getText(startOffset, endOffset - startOffset)
                    );
                    mRXTextPane.replaceRange("", startOffset, endOffset);
                    mRXTextPane.decrementCursorPositionRow();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void replaceLastlineRXText(final String str) {
        //System.out.println("[DEBUG] replaceLastlineRXText " + str);
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                int lastLine = mRXTextPane.getLineCount() - 1;
                try {
                    //System.out.println("[DEBUG]last line -> " + lastLine
                    //        + ", mRXTextPane.getLineStartOffset(lastLine) " + mRXTextPane.getLineStartOffset(lastLine)
                    //        + ", mRXTextPane.getLineEndOffset(lastLine) " + mRXTextPane.getLineEndOffset(lastLine));
                    mRXTextPane.replaceRange(str,
                            mRXTextPane.getLineStartOffset(lastLine),
                            mRXTextPane.getLineEndOffset(lastLine));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void clearRXScreen() {
        if (SwingUtilities.isEventDispatchThread()) {
            mRXTextPane.clearScreen();
            mRXFilteredTextPane.clearScreen();
            // Init filterCountTable
            currentFilterTableModel = new FilterCountTableModel(constructFilterCountTableData(), FILTER_TABLE_COLUMN_NAMES);
            filterCountTable.setModel(currentFilterTableModel);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    clearRXScreen();
                }
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        //System.out.println("[DEBUG] " + action + " received");
        switch (action) {
            case ACTION_OUTPUT_STREAM:
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
                break;
            /*--- findPanel ---*/
            case ACTION_FIND_NEXT:
                findNext();
                break;
            case ACTION_FIND_PREV:
                findPrev();
                break;
            case ACTION_FIND_END:
                endFind();
                break;
            /*--- filterPanel ---*/
            case ACTION_FILTER_SETUP:  // filter setup
                SPTerminal frame = SPTerminal.getFrame();
                String filterRuleName = (String)filterNameComboBox.getSelectedItem();
                System.out.println("Double-clicked on: " + filterRuleName);
                switch (filterRuleName) {
                    case NO_FILTER:
                        filterRuleName = null; // overwrite filter rule name
                    default:
                        FilterConfigDialog filterConfigDialog = new FilterConfigDialog(frame, filterRuleName);
                        filterConfigDialog.showDialog();
                        // Go back from dialog, filterRuleListValue may be updated.
                        Vector<String> filterNameListData = constructFilterNameListData();
                        filterNameComboBox.setModel(new DefaultComboBoxModel(filterNameListData));
                        if (filterNameListData.contains(filterRuleName)) {
                            // Re-select same (updated) filter rule
                            filterNameComboBox.setSelectedItem(filterRuleName);
                            notifyViewStateChanged(VIEW_STATE_FILTERED_LOG, filterRuleName);
                        } else {
                            filterNameComboBox.setSelectedItem(NO_FILTER);
                            notifyViewStateChanged(VIEW_STATE_MAIN_LOG, null);
                        }
                        break;
                }
                break;
        }
    }

    /*--- type, terminal, txtextfield ---*/
    /** Jump to start type */
    public void startType() {
        mTXTextField.requestFocus();
    }


    /*--- Find Utils ---*/
    /** find next query */
    private synchronized void findNext() {
        findCurrentPos++;
        updateFindHighlight();
    }

    /** find previous query */
    private synchronized void findPrev() {
        findCurrentPos--;
        updateFindHighlight();
    }

    /** Show findPanel */
    public void startFind() {
        findPanel.setVisible(true);
        findQueryTextField.requestFocus();
    }

    /** Hide findPanel */
    public void endFind() {
        mRXTextPane.getHighlighter().removeAllHighlights();
        mRXFilteredTextPane.getHighlighter().removeAllHighlights();
        findPanel.setVisible(false);
    }

    /** update position of current selected find text */
    private void updateFindHighlight() {
        HighlightableJTextPane textPane = (currentViewState == VIEW_STATE_MAIN_LOG) ?
                mRXTextPane : mRXFilteredTextPane;
        AutoScrollJScrollPane autoScrollPane = (currentViewState == VIEW_STATE_MAIN_LOG) ?
                mInputStreamScrollPane : mInputStreamFilteredScrollPane;
        //HighlightableJTextPane textPane = mRXFilteredTextPane;
        //AutoScrollJScrollPane autoScrollPane = mInputStreamScrollPane;

        autoScrollPane.setDoAutoScroll(false);
        String query = findQueryTextField.getText();
        boolean checkCase = matchCaseCheckBox.isSelected();
        boolean regex = regexCheckBox.isSelected();
        try {
            Highlighter highlighter = textPane.getHighlighter();
            highlighter.removeAllHighlights();
            Document doc = textPane.getDocument();
            String text = doc.getText(0, doc.getLength());
            if (regex) { // regex
                Pattern pattern = getPattern(query, checkCase, false);
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(text);
                    int pos = 0;
                    while (matcher.find(pos)) {
                        int start = matcher.start();
                        int end = matcher.end();
                        highlighter.addHighlight(start, end, highlightPainter);
                        pos = end;
                    }
                }
            } else {  // No regex case
                if (!checkCase) {
                    text = text.toLowerCase();
                    query = query.toLowerCase();
                }
                if (query != null && query.length() > 0) {
                    int pos = 0;
                    while (true) {
                        int start = text.indexOf(query, pos);
                        if (start == -1) break;
                        int end = start + query.length();
                        highlighter.addHighlight(start, end, highlightPainter);
                        pos = end;
                    }
                }
            }

            Highlighter.Highlight[] highlightArray = highlighter.getHighlights();
            int hits = highlightArray.length;
            if (hits == 0) {
                findCurrentPos = -1;
                //findQueryTextField.setBackground(Color.PINK); // Notify user that no result found.
            } else {
                findCurrentPos = (findCurrentPos + hits) % hits;
                Highlighter.Highlight hh = highlighter.getHighlights()[findCurrentPos];
                highlighter.removeHighlight(hh);
                highlighter.addHighlight(
                        hh.getStartOffset(), hh.getEndOffset(), currentHighlightPainter
                );
                scrollToCenter(textPane, hh.getStartOffset());
            }
            foundNumberLabel.setText(String.format("%d / %d", findCurrentPos + 1, hits));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

    private Pattern getPattern(String query, boolean checkCase, boolean checkWord) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        try {
            String cw = checkWord ? "\\b" : "";
            String pattern = String.format("%s%s%s", cw, query, cw);
            int flags = checkCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException ex) {
            //mRXTextPane.setBackground(WARNING_COLOR);
            ex.printStackTrace();
            return null;
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

    /**
     * Updates currentViewState, currentFilterRule and currentFilterRuleName.
     * @param viewState
     * @param filterRuleName
     */
    public synchronized void notifyViewStateChanged(int viewState, String filterRuleName) {
        currentViewState = viewState;
        currentFilterRuleName = filterRuleName;  // May be not necessary
        switch (currentViewState) {
            case VIEW_STATE_MAIN_LOG:
                currentFilterRule = null;
                // Update filterCountTable, filter count data is null for no filter
                currentFilterTableModel = new FilterCountTableModel(null, FILTER_TABLE_COLUMN_NAMES);
                filterCountTable.setModel(currentFilterTableModel);
                mInputStreamScrollPane.setVisible(true);
                mInputStreamFilteredScrollPane.setVisible(false);
                /** [Note] This will stop take log of filtered textpane. see {@link #writeRXText}. */
                break;
            case VIEW_STATE_FILTERED_LOG:
                currentFilterRule = FilterRule.load(currentFilterRuleName);
                // Update filterCountTable, it must be done before document copy
                currentFilterTableModel = new FilterCountTableModel(constructFilterCountTableData(), FILTER_TABLE_COLUMN_NAMES);
                filterCountTable.setModel(currentFilterTableModel);
                // Update RXTextPane
                // Copy from mRXTextPane to mRXFilteredTextPane
                //mRXFilteredTextPane.setText(mRXTextPane.getText());
                //mRXFilteredTextPane.getStyledDocument().getText()
                StyledDocument mainDoc = mRXTextPane.getStyledDocument();
                StyledDocument filteredDoc = mRXFilteredTextPane.getStyledDocument();
                try {
                    filteredDoc.remove(0, filteredDoc.getLength());
                    filteredDoc.insertString(0, mainDoc.getText(0, mainDoc.getLength()), null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
                mInputStreamScrollPane.setVisible(false);
                mInputStreamFilteredScrollPane.setVisible(true);
                /** [Note] This will Start take log of filtered textpane. see {@link #writeRXText}. */
                break;
        }

        this.revalidate();
        this.repaint();
    }

    private static void scrollToCenter(JTextComponent tc, int pos) throws BadLocationException {
        Rectangle rect = tc.modelToView(pos);
        Container c = SwingUtilities.getAncestorOfClass(JViewport.class, tc);
        if (rect != null && c instanceof JViewport) {
            rect.x      = (int) (rect.x - c.getWidth() * .5);
            rect.width  = c.getWidth();
            rect.height = (int) (c.getHeight() * .5);
            tc.scrollRectToVisible(rect);
        }
    }

    private Vector<String> constructFilterNameListData() {
        Vector<String> listData = new Vector<>();
        for (Map.Entry<String, String> e : filterConfig.filterRuleMap.entrySet()) {
            String fileNameKey = e.getKey();
            listData.add(fileNameKey);
        }
        Collections.sort(listData);

        // First data is no filter
        listData.add(0, NO_FILTER);
        return listData;
    }

    // TODO: remove
    private Vector<String> constructFilterRuleListData() {
        Vector<String> listData = new Vector<>();
        for (Map.Entry<String, String> e : filterConfig.filterRuleMap.entrySet()) {
            String fileNameKey = e.getKey();
            listData.add(fileNameKey);
        }
        Collections.sort(listData);

        // First data is to add new filter rule
        listData.add(0, NO_FILTER);
        listData.add(listData.size(), ADD_FILTER);
        return listData;
    }

    private synchronized Object[][] constructFilterCountTableData() {
        Vector<FilterRule.FilterRuleElement> vec = currentFilterRule.getFilterRuleVec();
        Object[][] data = new Object[vec.size()][2];
        for (int i = 0; i < vec.size(); i++) {
            data[i][FILTER_TABLE_QUERY_COL] = vec.get(i).getQuery();
            data[i][FILTER_TABLE_COUNT_COL] = 0L; // Init with 0
        }
        return data;
    }

    /*--- INNER CLASS ---*/
    private class FindQueryTextFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("FindQueryText keyPressed: e.getKeyCode() = " + e.getKeyCode());
            if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                /* CTRL key pressed */
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_S:  // find next
                        findNext();
                        break;
                    case KeyEvent.VK_R:  // find previous
                        findPrev();
                        break;
                }
            } else {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:  // close find
                        endFind();
                        break;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private class RXTextPaneKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("RXTextPaneKeyListener keyPressed: e.getKeyCode() = " + e.getKeyCode());
            if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                /* CTRL key pressed */
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F:
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_R:
                        startFind();
                        return;
                    default :
                        break;
                }
            } else {
                switch (e.getKeyCode()) {  // Normal key press
                    case KeyEvent.VK_ENTER:
                        startType();
                        break;
                }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    private class TXTextFieldKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println("[DEBUG] TXTextField keyPressed: e.getKeyCode() = " + e.getKeyCode());
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
                                dialog.showDialog();
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
            } else if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_F:
                        startFind();  // Alt-F to start find.
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

    private class FilteredTextPaneDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            //System.out.println("[DEBUG] FilteredTextPane insertString: " + MyUtils.unEscapeString(string));
            super.insertString(fb, offset, string, attr);

            StyledDocument doc = (StyledDocument) fb.getDocument();  // document that fired the event
            //StyledDocument doc = mRXFilteredTextPane.getStyledDocument();
            //e.getLength();  // length of the change
            //e.getOffset();  // offset that the first character changed
            try {
                int off = offset;
                int len = string.length();

                Element map = doc.getDefaultRootElement();
                int lineStart = map.getElementIndex(off);
                int lineEnd = map.getElementIndex(off+len);

                for (int line = lineEnd; line >= lineStart; line--) {
                    int lineStartOffset = mRXFilteredTextPane.getLineStartOffset(line);
                    int lineEndOffset = mRXFilteredTextPane.getLineEndOffset(line);
                    //System.out.println("FilteredTextPane insertString off = " + off
                    //        + ", len = " + len
                    //        + ", line = " + line
                    //        + ", start = " + start
                    //        + ", end = " + end
                    //);
                    String lineStr = doc.getText(lineStartOffset, lineEndOffset-lineStartOffset);
                    if (lineStr.contains("\n")) {  // remove line only after confirmed that lineStr is already completed sentence
                        int pos = 0;
                        int alreadyRemovedLength = 0;
                        while (pos < lineStr.length()) {
                            int crPos = lineStr.indexOf("\r", pos);
                            if (crPos < 0) {
                                break;
                                //crPos = lineStr.length();
                            }
                            //if (pos == crPos) {
                            //    // '\r' is at pos, remove this character.
                            //    System.out.println("doc.remove str = " + MyUtils.unEscapeString(doc.getText(lineStartOffset + pos, crPos + 1 - pos))
                            //            + "  start = " + lineStartOffset + ", end = " + lineEndOffset + ", len = " + doc.getLength());
                            //    fb.remove(lineStartOffset + pos, crPos + 1 - pos);// remove following \r
                            //    pos = crPos + 1;
                            //    continue;
                            //}
                            String lineStrPart = lineStr.substring(pos, crPos);
                            if (pos == crPos || filter(lineStrPart)) {
                                //System.out.println("doc.remove start = " + lineStartOffset + ", end = " + lineEndOffset + ", len = " + doc.getLength());
                                // TODO: somehow this remove method makes scrollbar to jump to caret position
                                //doc.remove(start, end-start);
                                //fb.remove(lineStartOffset, lineEndOffset-lineStartOffset);

                                int start = lineStartOffset + pos;
                                //if (start == lineStartOffset) lineStartOffset--;
                                // BE careful, lineStartOffset may vary.
                                //int start = mRXFilteredTextPane.getLineStartOffset(line) + pos;
                                int length = crPos + 1 - pos; // remove only \r
                                if (pos != crPos && lineStr.length() > crPos + 1 && lineStr.charAt(crPos+1) == '\n') {
                                    // remove following \r\n together
                                    length++;
                                }
                                start -= alreadyRemovedLength;
                                //System.out.println("doc.remove str = " + MyUtils.unEscapeString(doc.getText(start, length))
                                //        + ", start = " + start + ", end = " + (start+length) + ", doc.len = " + doc.getLength());//fb.remove(lineStartOffset + pos, crPos - pos);
                                fb.remove(start, length);
                                alreadyRemovedLength += length;
                            }
                            pos = crPos + 1; // update pos
                        }
                    }
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, text, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
        }

        /**
         * Check whether lineStr is filtered by filter rule element
         * @param lineStr
         * @return true when lineStr matches one of the filter rule element (should be filtered),
         *         false otherwise (should not be filtered).
         */
        boolean filter(String lineStr) {
            //System.out.println("[DEBUG] filtering lineStr = " + MyUtils.unEscapeString(lineStr));
            if (currentFilterRule == null) return false;
            Vector<FilterRule.FilterRuleElement> vec = currentFilterRule.getFilterRuleVec();
            for (int i = 0; i < vec.size(); i++) {
                FilterRule.FilterRuleElement elem = vec.get(i);
                int ruleType = elem.getRuleType();
                boolean match = false;
                if (ruleType == FilterRule.FilterRuleElement.RULE_TYPE_CONTAIN) {
                    boolean matchCase = elem.isMatchCase();
                    boolean regex = elem.isRegex();
                    String query = elem.getQuery();
                    if (regex) {
                        // getPattern
                        Pattern pattern = getPattern(query, matchCase, false);
                        if (pattern != null) {
                            Matcher matcher = pattern.matcher(lineStr);
                            if (matcher.find()) match = true;
                        } else {
                            // skip this filter element
                        }
                    } else {
                        if (matchCase) {
                            match = lineStr.contains(query);
                        } else{
                            match = lineStr.toLowerCase().contains(query.toLowerCase());
                        }
                    }
                } else {
                    System.out.printf("[ERROR] rule type " + ruleType + " not supported");
                }
                if (match) {
                    // update count
                    synchronized (this) {
                        currentFilterTableModel.setValueAt(
                                (long)currentFilterTableModel.getValueAt(i, FILTER_TABLE_COUNT_COL) + 1,
                                i,
                                FILTER_TABLE_COUNT_COL
                        );
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private class FindQueryTextFieldDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            // Auto-update works only when no regex find.
            if (!regexCheckBox.isSelected()) {
                updateFindHighlight();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            if (doc.getLength() == 0) {
                // Reset when no query text.
                findCurrentPos = 0;
            }
            // Auto-update works only when no regex find.
            if (!regexCheckBox.isSelected()) {
                updateFindHighlight();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

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
