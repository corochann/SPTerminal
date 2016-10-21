package com.corochann.spterminal.ui.menu;

import javax.swing.*;
import java.awt.event.*;

/**
 *
 */
public class RXTextPopupMenu extends JPopupMenu implements MouseListener, ActionListener {
    private static final String ACTION_FIND = "find";

    private RXTextPopupMenuListener listener = null;

    public RXTextPopupMenu() {
        super();

        JMenuItem findMenuItem = new JMenuItem("Find");
        findMenuItem.setMnemonic(KeyEvent.VK_F);
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        findMenuItem.addActionListener(this);
        findMenuItem.setActionCommand(ACTION_FIND);

        this.add(findMenuItem);
    }

    public void setRXTextPopupMenuListener(RXTextPopupMenuListener listener) {
        this.listener = listener;
    }
    /*--- MouseListener ---*/
    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showRXTextPopupMenu(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showRXTextPopupMenu(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /*--- ActionListener ---*/
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case ACTION_FIND:
                if (listener != null) {
                    listener.onStartFind(e);
                }
                break;
        }
    }

    /*--- private method ---*/
    private void showRXTextPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public interface RXTextPopupMenuListener {
        void onStartFind(ActionEvent e);
    }
}
