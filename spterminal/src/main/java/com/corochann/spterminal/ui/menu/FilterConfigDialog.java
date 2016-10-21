package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.FilterConfig;
import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.data.model.FilterRule;
import com.corochann.spterminal.teraterm.TTLMacro;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.component.CustomJComboBox;
import com.corochann.spterminal.ui.component.CustomJTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

/**
 * TTLMacro config dialog
 * It is invoked from filterList in {@link com.corochann.spterminal.ui.TerminalPanel}
 */
public class FilterConfigDialog extends JDialog implements ActionListener {

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_ADD_RULE = "addrule";
    private static final String ADD_NEW = "Add new filter";
    private static final String RULE_TYPE_STARTSWITH = "startswith";
    private static final String RULE_TYPE_CONTAIN = "contains";
    private final JList filterList;

    public static final int preferredTextWidth = 300;
    public static final int preferredTextAreaHeight = 150;
    private final CustomJTextField fileNameTextField;
    private FilterConfig filterConfig;
    private JScrollPane filterRuleSP;
    private JPanel filterRulePanel;
    private JPanel filterRuleEditPanel;

    public FilterConfigDialog(Frame owner) {
        this(owner, "");
    }

    /**
     *
     * @param owner
     * @param filterRuleName  filter rule to be modified. This filter rule name is initially selecte.
     *                        set null, if filter rule is newly added.
     */
    public FilterConfigDialog(Frame owner, String filterRuleName) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("Log filter setup");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();
        StyleConfig styleConfig = styleSelectorConfig.getStyleConfig();
        filterConfig = SPTerminalPreference.getInstance().getFilterConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st panel: selectPanel
         * Existing filter rule can be selected in this Panel for editting. */
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));

        JLabel filterRuleLabel = new JLabel("Filter rule list");

        JScrollPane filterRuleListSP = new JScrollPane();
        filterList = new JList<>();

        Vector<String> filterRuleListValue = constructFilterRuleListData();
        if (filterRuleName == null) filterRuleName = ADD_NEW;

        filterList.setListData(filterRuleListValue);
        filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filterList.setSelectedValue(filterRuleName, true);

        filterRuleListSP.getViewport().setView(filterList);
        filterRuleListSP.setPreferredSize(new Dimension(200, 200));

        selectPanel.add(filterRuleLabel);
        selectPanel.add(filterRuleListSP);

        /* 2nd row: edit panel
         * Filter rule can be edited here.
         */
        filterRuleEditPanel = new JPanel();
        filterRuleEditPanel.setLayout((new BoxLayout(filterRuleEditPanel, BoxLayout.Y_AXIS)));

        /* 2-1: fileName */
        JPanel fileNamePanel = new JPanel();
        JLabel fileNameLabel = new JLabel("File name");
        fileNameTextField = new CustomJTextField();
        fileNameTextField.setPreferredWidth(preferredTextWidth);

        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(fileNameTextField);

        /* 2-2: filter rule elements */
        filterRulePanel = new JPanel();
        filterRulePanel.setLayout(new BoxLayout(filterRulePanel, BoxLayout.Y_AXIS));
        filterRuleSP = new JScrollPane(filterRulePanel);
        filterRuleSP.setPreferredSize(new Dimension(400, 200));

        // construct UI from filterRuleMap
        String key = FilterConfig.constructKey(filterRuleName);
        if (filterConfig.filterRuleMap.containsKey(key)) {
            constructAndUpdateFilterRuleUI(key);
        } else if (filterRuleName.equals(ADD_NEW)) {
            addFilterRuleElementPanel(); // Add one extra empty rule for convenience
            updateFilterRuleUI();
        }
        //String filterRuleFileName = filterConfig.filterRuleMap.get(key);
        //FilterRule filterRule = FilterRule.load(filterRuleFileName);
        //if (filterRule != null) {
        //    Vector<FilterRule.FilterRuleElement> filterRuleVec = filterRule.getFilterRuleVec();
        //    for (int i = 0; i < filterRuleVec.size(); i++) {
        //        FilterRule.FilterRuleElement elem = filterRuleVec.get(i);
        //
        //        addFilterRuleElementPanel();
        //    }
        //}

        /* 2-3: add */
        JPanel addRulePanel = new JPanel();
        //JLabel addRuleLabel = new JLabel("Command");
        CustomJButton addRuleButton = new CustomJButton("Add filter rule", styleConfig);
        addRuleButton.setActionCommand(ACTION_ADD_RULE);
        addRuleButton.addActionListener(this);

        addRulePanel.add(addRuleButton);

        fileNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRuleSP.setAlignmentX(Component.LEFT_ALIGNMENT);
        addRulePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterRuleEditPanel.add(fileNamePanel);
        filterRuleEditPanel.add(filterRuleSP);
        filterRuleEditPanel.add(addRulePanel);

        /* 3rd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleConfig);
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        CustomJButton deleteButton = new CustomJButton("Delete", styleConfig);
        deleteButton.setActionCommand(ACTION_DELETE);
        deleteButton.addActionListener(this);
        CustomJButton cancelButton = new CustomJButton("Cancel", styleConfig);
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(deleteButton);
        formButtonPanel.add(cancelButton);

        selectPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        filterRuleEditPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(selectPanel);
        mainPanel.add(filterRuleEditPanel);
        mainPanel.add(formButtonPanel);

        // Listener must be set after all components made
        filterList.addListSelectionListener(new AliasListSelectionListener());

        this.getContentPane().add(mainPanel);
        this.pack();
        this.setPreferredSize(new Dimension(500, getHeight()));
    }

    public void showDialog() {
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private void addFilterRuleElementPanel() {
        FilterRuleElementPanel filterRuleElementPanel = new FilterRuleElementPanel();
        filterRulePanel.add(filterRuleElementPanel);
        updateFilterRuleUI();
        System.out.println("current component count = " + filterRulePanel.getComponentCount());
    }

    private void updateFilterRuleUI() {
        filterRuleEditPanel.revalidate();
        filterRulePanel.revalidate();
        filterRuleSP.revalidate();
        filterRuleEditPanel.repaint();
        filterRulePanel.repaint();
        filterRuleSP.repaint();
    }

    private Vector<String> constructFilterRuleListData() {
        Vector<String> listData = new Vector<>();
        for (Map.Entry<String, String> e : filterConfig.filterRuleMap.entrySet()) {
            String fileNameKey = e.getKey();
            listData.add(fileNameKey);
        }
        Collections.sort(listData);

        // First data is to add new filter rule
        listData.add(0, ADD_NEW);
        return listData;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String filterRuleNameKey;
        switch (action) {
            case ACTION_OK:
                System.out.println("Ok pressed: " + filterList.getSelectedValue());
                // Save filter rule
                filterRuleNameKey = (String) filterList.getSelectedValue();
                if (filterRuleNameKey.equals(ADD_NEW)) {
                    FilterRule filterRule = constructFilterRule();
                    saveNewFilterRule(filterRule);
                } else {  // When editing existing filterRule
                    if (filterConfig.filterRuleMap.containsKey(filterRuleNameKey)) {
                        FilterRule filterRule = constructFilterRule();
                        updateFilterRule(filterRuleNameKey, filterRule);
                    }
                }
                this.dispose();
                break;
            case ACTION_DELETE:
                System.out.println("Delete pressed: " + filterList.getSelectedValue());
                filterRuleNameKey = (String) filterList.getSelectedValue();
                if (filterConfig.filterRuleMap.containsKey(filterRuleNameKey)) {
                    // Delete alias;
                    deleteFilterRule(filterRuleNameKey);
                }
                this.dispose();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                this.dispose();
                break;
            case ACTION_ADD_RULE:
                System.out.println("Add rule pressed");
                addFilterRuleElementPanel();

                break;
        }
    }

    /**
     * Constructs {@link FilterRule} from UI (filterRulePanel).
     * @return
     */
    private FilterRule constructFilterRule() {
        int count = filterRulePanel.getComponentCount();
        FilterRule filterRule = new FilterRule();
        Vector<FilterRule.FilterRuleElement> filterRuleVec = new Vector<>();

        for (int i = 0; i < count; i++) {
            FilterRuleElementPanel filterRuleElementPanel = (FilterRuleElementPanel)filterRulePanel.getComponent(i);
            String ruleTypeString = filterRuleElementPanel.getRuleType();
            int ruleType = convertRuleType(ruleTypeString);
            String query = filterRuleElementPanel.getQuery();
            boolean isMatchCase = filterRuleElementPanel.isMatchCaseSelected();
            boolean isRegex = filterRuleElementPanel.isRegexSelected();

            FilterRule.FilterRuleElement element = new FilterRule.FilterRuleElement(
                    ruleType,
                    query,
                    isMatchCase,
                    isRegex
            );
            System.out.println("[Debug] add filterRuleElement with"
                    + " ruleType = " + ruleType
                    + ", query = " + query
                    + ", isMatchCase = " + isMatchCase
                    + ", isRegex = " + isRegex
            );
            if (element.validate()) {  // check if element is in proper format.
                filterRuleVec.add(element);
            }
        }

        filterRule.setFileName(fileNameTextField.getText());
        filterRule.setFilterRuleVec(filterRuleVec);
        return filterRule;
    }

    private int convertRuleType(String ruleTypeString) {
        int ruleType = -1;
        switch (ruleTypeString) {
            case RULE_TYPE_STARTSWITH:
                ruleType = FilterRule.FilterRuleElement.RULE_TYPE_STARTSWITH;
                break;
            case RULE_TYPE_CONTAIN:
                ruleType = FilterRule.FilterRuleElement.RULE_TYPE_CONTAIN;
                break;
        }
        return ruleType;
    }

    private String convertRuleType(int ruleType) {
        String ruleTypeString = null;
        switch (ruleType) {
            case FilterRule.FilterRuleElement.RULE_TYPE_STARTSWITH:
                ruleTypeString = RULE_TYPE_STARTSWITH;
                break;
            case FilterRule.FilterRuleElement.RULE_TYPE_CONTAIN:
                ruleTypeString = RULE_TYPE_CONTAIN;
                break;
        }
        return ruleTypeString;
    }


    private void saveNewFilterRule(FilterRule filterRule) {
        // Check key is already existing or not
        String aliasKey = FilterConfig.constructKey(filterRule.getFileName());
        if (filterConfig.filterRuleMap.containsKey(aliasKey)) {
            System.out.println("[WARNING] " + filterRule.getFileName() + " already existing, " +
                    "will be deleted and overwritten.");
            deleteFilterRule(aliasKey);
        }
        // Save xml & Add to FilterConfig
        try {
            filterRule.save();
            filterConfig.addFilterRule(filterRule.getFileName()); // Added only when save succeed.
        } catch (FilterRule.FormatErrorException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Save failed, some parameter is not proper.");
        }
    }

    /**
     * Delete filter rule specified by alias key
     * It will delete both from file (.xml file) and preference(filterConfig.filterRuleMap).
     * @param aliasKey
     */
    private void deleteFilterRule(String aliasKey) {
        // Delete ttl file
        FilterRule.deleteFile(filterConfig.filterRuleMap.get(aliasKey));
        // Remove from map
        filterConfig.filterRuleMap.remove(aliasKey);
    }

    /**
     * update FilterRule. Delete existing file specified by aliasKey and save new filterRule.
     * @param aliasKey
     * @param filterRule
     */
    private void updateFilterRule(String aliasKey, FilterRule filterRule) {
        deleteFilterRule(aliasKey);
        saveNewFilterRule(filterRule);
        //TODO: Currently if save fails, file is already deleted so nothing will remain.
        // Better spec is to confirm save succeed and then delete file if aliasName is different from new name.
    }


    /*--- Inner class ---*/
    private static class FilterRuleElementPanel extends JPanel {
        private final CustomJComboBox<String> ruleTypeComboBox;
        private final CustomJTextField queryTextField;
        private final JCheckBox matchCaseCheckBox;
        private final JCheckBox regexCheckBox;

        FilterRuleElementPanel() {
            this(null, null, false, false);
        }

        FilterRuleElementPanel(String ruleType, String query, boolean matchCase, boolean regex) {
            super();

            ruleTypeComboBox = new CustomJComboBox<>();
            Vector<String> ruleTypeStrings = new Vector<>();
            ruleTypeStrings.add(RULE_TYPE_STARTSWITH);
            ruleTypeStrings.add(RULE_TYPE_CONTAIN);
            ruleTypeComboBox.setModel(new DefaultComboBoxModel<String>(ruleTypeStrings));

            queryTextField = new CustomJTextField();
            queryTextField.setPreferredWidth(100);
            matchCaseCheckBox = new JCheckBox("match case");
            regexCheckBox = new JCheckBox("regex");

            /*--- Set initial value ---*/
            if (ruleType != null) {
                ruleTypeComboBox.setSelectedItem(ruleType);
            }
            if (query != null) {
                queryTextField.setText(query);
            }
            matchCaseCheckBox.setSelected(matchCase);
            regexCheckBox.setSelected(regex);

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(ruleTypeComboBox);
            this.add(queryTextField);
            this.add(matchCaseCheckBox);
            this.add(regexCheckBox);
        }

        public String getRuleType() {
            return (String)ruleTypeComboBox.getSelectedItem();
        }

        public String getQuery() {
            return queryTextField.getText();
        }

        public boolean isMatchCaseSelected() {
            return matchCaseCheckBox.isSelected();
        }

        public boolean isRegexSelected() {
            return regexCheckBox.isSelected();
        }
    }


    private class AliasListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                // Get value from selectPanel
                String aliasKey = (String) filterList.getSelectedValue();
                // Update aliasEditPanel
                if (aliasKey.equals(ADD_NEW)) {
                    fileNameTextField.setText("");
                    filterRulePanel.removeAll();
                    addFilterRuleElementPanel();
                    updateFilterRuleUI();
                } else {
                    if (filterConfig.filterRuleMap.containsKey(aliasKey)) {
                        constructAndUpdateFilterRuleUI(aliasKey);
                    }
                }
            }
        }
    }

    private void constructAndUpdateFilterRuleUI(String aliasKey) {
        FilterRule filterRule = FilterRule.load(filterConfig.filterRuleMap.get(aliasKey));
        fileNameTextField.setText(filterRule.getFileName());
        Vector<FilterRule.FilterRuleElement> filterRuleVec = filterRule.getFilterRuleVec();
        filterRulePanel.removeAll();
        for (int i = 0; i < filterRuleVec.size(); i++) {
            FilterRule.FilterRuleElement element = filterRuleVec.get(i);
            int ruleType = element.getRuleType();
            String ruleTypeStr = convertRuleType(ruleType);
            String query = element.getQuery();
            boolean matchCase = element.isMatchCase();
            boolean regex = element.isRegex();

            FilterRuleElementPanel elementPanel = new FilterRuleElementPanel(
                    ruleTypeStr, query, matchCase, regex
            );
            filterRulePanel.add(elementPanel);
        }
        addFilterRuleElementPanel(); // Add one extra empty rule for convenience
        updateFilterRuleUI();
    }
}
