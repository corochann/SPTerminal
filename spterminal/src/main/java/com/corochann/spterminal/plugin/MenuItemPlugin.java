package com.corochann.spterminal.plugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Plugin which appears in "Plugins" MenuItem
 */
public interface MenuItemPlugin extends BasePlugin {
    /** Specifies text appears on the menu. */
    String getText();

    /** returns this plugin's instance */
    JMenuItem createJMenuItemInstance();
}
