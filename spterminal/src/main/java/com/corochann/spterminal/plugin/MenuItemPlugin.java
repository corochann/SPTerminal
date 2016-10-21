package com.corochann.spterminal.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Plugin which appears in "Plugins" MenuItem
 */
public interface MenuItemPlugin extends BasePlugin, ActionListener {
    /** Specifies text appears on the menu. */
    String getText();
    /** action when user press menu item */
    void actionPerformed(ActionEvent e);
}
