package com.corochann.spterminal.ui.menu;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.style.StyleSelectorConfig;
import com.corochann.spterminal.config.teraterm.TTLMacroConfig;
import com.corochann.spterminal.teraterm.TTLMacro;
import com.corochann.spterminal.ui.component.CustomJButton;
import com.corochann.spterminal.ui.component.CustomJTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
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
 * It is invoked from {@link SPTMenuBar}
 */
public class TTLMacroConfigDialog extends JDialog implements ActionListener {

    /*--- Action definitions ---*/
    private static final String ACTION_OK = "ok";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_CANCEL = "cancel";

    private static final String ADD_NEW = "Add new Teraterm macro";
    private final JList ttlList;

    public static final int preferredTextWidth = 300;
    public static final int preferredTextAreaHeight = 150;
    private final CustomJTextField fileNameTextField;
    private final JTextArea commandTextArea;
    private TTLMacroConfig ttlMacroConfig;

    public TTLMacroConfigDialog(Frame owner) {
        this(owner, "");
    }

    public TTLMacroConfigDialog(Frame owner, String commandTextAreaText) {
        this(owner, commandTextAreaText, "");
    }

    public TTLMacroConfigDialog(Frame owner, String commandTextAreaText, String ttlFileName) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.setTitle("Teraterm macro setup");
        StyleSelectorConfig styleSelectorConfig = SPTerminalPreference.getInstance().getStyleSelectorConfig();
        ttlMacroConfig = SPTerminalPreference.getInstance().getTTLMacroConfig();

        /*--- Create components ---*/
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* 1st panel: selectPanel
         * Existing ttl macro can be selected in this Panel for editting. */
        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));

        JLabel ttlListLabel = new JLabel("ttl macro list");

        JScrollPane ttlListSP = new JScrollPane();
        ttlList = new JList<>();

        Vector<String> ttlListValue = constructTTLListData();

        ttlList.setListData(ttlListValue);
        ttlList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ttlList.setSelectedValue(ADD_NEW, true);

        ttlListSP.getViewport().setView(ttlList);
        ttlListSP.setPreferredSize(new Dimension(200, 200));

        selectPanel.add(ttlListLabel);
        selectPanel.add(ttlListSP);

        /* 2nd row: edit panel
         * TTL can be edited here.
         */
        JPanel ttlEditPanel = new JPanel();
        ttlEditPanel.setLayout((new BoxLayout(ttlEditPanel, BoxLayout.Y_AXIS)));
        /* 2-1: fileName */
        JPanel fileNamePanel = new JPanel();
        JLabel fileNameLabel = new JLabel("File name");
        fileNameTextField = new CustomJTextField(ttlFileName);
        fileNameTextField.setPreferredWidth(preferredTextWidth);

        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(fileNameTextField);

        /* 2-3: command */
        JPanel commandPanel = new JPanel();
        JLabel commandLabel = new JLabel("Command");
        commandTextArea = new JTextArea(commandTextAreaText);
        //commandTextArea.setPreferredSize(new Dimension(preferredTextWidth, preferredTextAreaHeight));
        commandTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        JScrollPane commandTextAreaSP = new JScrollPane();
        commandTextAreaSP.setPreferredSize(new Dimension(preferredTextWidth, preferredTextAreaHeight));
        commandTextAreaSP.getViewport().setView(commandTextArea);
        //commandTextAreaSP.setPreferredSize(new Dimension(200, 200));

        commandPanel.add(commandLabel);
        commandPanel.add(commandTextAreaSP);

        ttlEditPanel.add(fileNamePanel);
        ttlEditPanel.add(commandPanel);

        /* 3rd row: form button */
        JPanel formButtonPanel = new JPanel();
        formButtonPanel.setLayout(new BoxLayout(formButtonPanel, BoxLayout.X_AXIS));
        CustomJButton okButton = new CustomJButton("Ok", styleSelectorConfig.getStyleConfig());
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        CustomJButton deleteButton = new CustomJButton("Delete", styleSelectorConfig.getStyleConfig());
        deleteButton.setActionCommand(ACTION_DELETE);
        deleteButton.addActionListener(this);
        CustomJButton cancelButton = new CustomJButton("Cancel", styleSelectorConfig.getStyleConfig());
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        formButtonPanel.add(okButton);
        formButtonPanel.add(deleteButton);
        formButtonPanel.add(cancelButton);

        mainPanel.add(selectPanel);
        mainPanel.add(ttlEditPanel);
        mainPanel.add(formButtonPanel);

        // Listener must be set after all components made
        ttlList.addListSelectionListener(new AliasListSelectionListener());

        this.getContentPane().add(mainPanel);
        this.pack();
    }

    public void showDialog() {
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private Vector<String> constructTTLListData() {
        Vector<String> listData = new Vector<>();
        for (Map.Entry<String, String> e : ttlMacroConfig.ttlMacroMap.entrySet()) {
            String fileNameKey = e.getKey();
            listData.add(fileNameKey);
        }
        Collections.sort(listData);

        // First data is to add new TTL macro
        listData.add(0, ADD_NEW);
        return listData;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        String ttlFileNameKey;
        switch (action) {
            case ACTION_OK:
                System.out.println("Ok pressed: " + ttlList.getSelectedValue());
                // Save ttl
                ttlFileNameKey = (String) ttlList.getSelectedValue();
                if (ttlFileNameKey.equals(ADD_NEW)) {
                    TTLMacro ttlMacro = new TTLMacro();
                    ttlMacro.setFileName(fileNameTextField.getText());
                    ttlMacro.setCommand(commandTextArea.getText());
                     saveNewTTLMacro(ttlMacro);
                    //String styleName = ((String) styleNameComboBox.getSelectedItem());
                    //saveStyleSelectorConfig(styleName);
                } else {
                    if (ttlMacroConfig.ttlMacroMap.containsKey(ttlFileNameKey)) {
                        TTLMacro ttlMacro = new TTLMacro();
                        ttlMacro.setFileName(fileNameTextField.getText());
                        ttlMacro.setCommand(commandTextArea.getText());
                        updateTTLMacro(ttlFileNameKey, ttlMacro);
                    }
                }
                this.dispose();
                break;
            case ACTION_DELETE:
                System.out.println("Delete pressed: " + ttlList.getSelectedValue());
                ttlFileNameKey = (String) ttlList.getSelectedValue();
                if (ttlMacroConfig.ttlMacroMap.containsKey(ttlFileNameKey)) {
                    // Delete alias;
                    deleteTTLMacro(ttlFileNameKey);
                }
                this.dispose();
                break;
            case ACTION_CANCEL:
                System.out.println("Cancel pressed");
                this.dispose();
                break;
        }
    }

    /**
     *
     * @param ttlMacro
     * @return false when save fails. true when save success
     */
    private boolean saveNewTTLMacro(TTLMacro ttlMacro) {
        // Check key is already existing or not
        String aliasKey = TTLMacroConfig.constructKey(ttlMacro.getFileName());
        if (ttlMacroConfig.ttlMacroMap.containsKey(aliasKey)) {
            System.out.println("[WARNING] " + ttlMacro.getFileName() + " already existing, " +
                    "will be deleted and overwritten.");
            deleteTTLMacro(aliasKey);
        }
        // Save xml & Add to SPTAliasListConfig
        try {
            ttlMacro.save();
            ttlMacroConfig.addTTLMacro(ttlMacro.getFileName()); // Added only when save succeed.
        } catch (TTLMacro.FormatErrorException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Save failed, some parameter is not proper.");
            return false;
        }
        return true;
    }

    private void deleteTTLMacro(String aliasKey) {
        // Delete ttl file
        TTLMacro.deleteFile(ttlMacroConfig.ttlMacroMap.get(aliasKey));
        // Remove from map
        ttlMacroConfig.ttlMacroMap.remove(aliasKey);
    }

    /**
     * update TTLMacro. Delete existing file specified by aliasKey and save new ttlMacro.
     * @param aliasKey
     * @param ttlMacro
     */
    private boolean updateTTLMacro(String aliasKey, TTLMacro ttlMacro) {
        deleteTTLMacro(aliasKey);
        return saveNewTTLMacro(ttlMacro);
        //TODO: Currently if save fails, file is already deleted so nothing will remain.
        // Better spec is to confirm save succeed and then delete file if aliasName is different from new name.
    }

    private class AliasListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                // Get value from selectPanel
                String aliasKey = (String) ttlList.getSelectedValue();
                // Update aliasEditPanel
                if (aliasKey.equals(ADD_NEW)) {
                    fileNameTextField.setText("");
                    commandTextArea.setText("");
                } else {
                    if (ttlMacroConfig.ttlMacroMap.containsKey(aliasKey)) {
                        TTLMacro ttlMacro = TTLMacro.load(ttlMacroConfig.ttlMacroMap.get(aliasKey));
                        fileNameTextField.setText(ttlMacro.getFileName());
                        commandTextArea.setText(ttlMacro.getCommand());
                    }
                }
            }
        }
    }
}
