package com.corochann.spterminal.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * JScrollPane which supporting auto-scroll feature.
 * auto-scroll function is enabled as default, and when user goes to the bottom of scroll bar.
 * auto-scroll function is disabled when user try to move scroll bar (either by scroll bar adjust or mouse wheel)
 */
public class AutoScrollJScrollPane extends JScrollPane {

    private boolean doAutoScroll = true;

    public AutoScrollJScrollPane(final Component view) {
        super(view);

        final JScrollBar verticalScrollBar = getVerticalScrollBar();
        // Same effect with TextArea's setLineWrap(true);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            BoundedRangeModel brm = verticalScrollBar.getModel();
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                // Invoked when user select and move the cursor of scroll by mouse explicitly.
                if (!brm.getValueIsAdjusting()) {
                    view.setPreferredSize(new Dimension(view.getWidth(), view.getHeight() + verticalScrollBar.getVisibleAmount()));
                    view.setMinimumSize(new Dimension(view.getWidth(), view.getPreferredSize().height + verticalScrollBar.getVisibleAmount()));
                    //System.out.println("[DEBUG] mRXTextPane.getWidth() = " + mRXTextPane.getWidth()
                    //        + ", height = " + mRXTextPane.getHeight()
                    //        + ", extent = " + inputStreamVerticalScrollBar.getVisibleAmount()
                    //        + ", line height = " + mRXTextPane.getFontMetrics(mRXTextPane.getFont()).getHeight()
                    //);
                    if (doAutoScroll) brm.setValue(brm.getMaximum());
                } else {
                    // doAutoScroll will be set to true when user reaches at the bottom of document.
                    doAutoScroll = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            BoundedRangeModel brm = verticalScrollBar.getModel();
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Invoked when user use mouse wheel to scroll
                //System.out.println("mouseWheelMoved "
                //        + ", doAutoScroll = " + doAutoScroll
                //        + ", scrollAmount = " + e.getScrollAmount()
                //        // scroll down: positive, scroll up: negative value
                //        + ", wheelRotation = " + e.getWheelRotation()
                //        + ", scrollType = " + e.getScrollType()
                //        + ", scrollType = " + e.getPoint()
                //        + ", isAdjusting = " + brm.getValueIsAdjusting()
                //        + ", value = " + brm.getValue()
                //        + ", extent = " + brm.getExtent()
                //        + ", maximum = " + brm.getMaximum()
                //);

                if (e.getWheelRotation() < 0) {
                    /* If user trying to scroll up, user want to stop auto scroll. doAutoScroll should be false. */
                    doAutoScroll = false;
                } else {
                    /* doAutoScroll will be set to true when user reaches at the bottom of document. */
                    doAutoScroll = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
                }
            }
        });
    }

    public boolean isDoAutoScroll() {
        return doAutoScroll;
    }

    /**
     * Start & Stop auto scrolling
     * @param doAutoScroll
     */
    public void setDoAutoScroll(boolean doAutoScroll) {
        this.doAutoScroll = doAutoScroll;
    }
}
