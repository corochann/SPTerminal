package com.corochann.spterminal.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * CustomJTextPane
 * - setWrapText(): supports to show horizontal scroll bar when it is wrapped by JScrollPane.
 */
public class CustomJTextPane extends JTextPane {

    private boolean wrapText = true;
    /**
     * When this CustomJTextPane is wrapped by JScrollPane,
     * Default value is true, text is automatically wrapped and horizontal scroll bar never appear.
     * setWrapText to false enables horizontal scroll bar to appear.
     * [Caution] However, even set to false, text is wrapped at next white space.
     * You need to use noWrapPanel if you want to disable wrapping text entirely.
     * Ref: https://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
     */
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
    }

    /**
     * These 2 methods override is to allow text pane to NOT to wrap the text
     * See https://coderanch.com/t/334723/java/add-JTextPane-JScrollPane
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (wrapText) return super.getScrollableTracksViewportWidth();
        else return (getSize().width < getParent().getSize().width);
    }

    @Override
    public void setSize(Dimension d) {
        if (wrapText) {
            super.setSize(d);
        } else {
            if (d.width < getParent().getSize().width) {
                d.width = getParent().getSize().width;
                //d.width = getParent().getPreferredSize().width;
            }
            super.setSize(d);
            super.setPreferredSize(d);
        }
        System.out.println("setSize: getSize width = " + getSize().getWidth() + ", pf w = " + getPreferredSize().getWidth());
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        System.out.println("CustomJTextPane setPreferredSize");
        if (wrapText) {
            super.setPreferredSize(preferredSize);
        } else {
            if (preferredSize.width < getParent().getSize().width) {
                //preferredSize.width = getParent().getPreferredSize().width;
                preferredSize.width = getParent().getSize().width;
            }
            super.setPreferredSize(preferredSize);
        }
    }
}
