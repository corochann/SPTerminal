package ui.component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * JTextArea with extended feature that one line of background can be highlighted
 * Note: use {#repaint()} to update the graphics after setHighlightLine() call. (App-triggered painting)
 */
public class HighlightableTextArea extends JTextArea {
    /** The line to be highlighted. -1 indicates that no line should be highlighted. */
    private int highlightLine = -1;
    /**
     * Highlight highlightColor
     * Color's alpha must be low value, in order to show text behind.
     */
    private Color highlightColor = new Color(51, 153, 255, 30); // Default highlightColor

    public HighlightableTextArea(int rows, int columns) {
        super(rows, columns);
    }

    /** Update line to be highlighted. It supports only one line highlighting
     * Note: line is 0-indexed.
     */
    public void setHighlightLine(int line) {
        highlightLine = line;
    }

    /**
     * Set highlight color
     */
    public void setHighlightColor(Color color) {
        highlightColor = color;
    }

    @Override
    public void paint(Graphics g) {
        //System.out.println("execute paint with highlinetLine = " + highlightLine);
        super.paint(g);
        highlightLine(g, highlightLine);
    }

    private void highlightLine(Graphics g, int line) {
        //System.out.println("highlightLine " + line);
        if (line < 0) return; // Do nothing when selected line is invalid (no line is selected)
        try {
            /* Remove last drawed rectangle */
            //removeHighlight();

            /* Draw new rectangle */
            Rectangle r = this.modelToView(this.getLineStartOffset(line));

            // Currently set alpha small value, so that we can see original text after drawing rectangle on top of textarea
            g.setColor(highlightColor);
            g.fillRect(0, r.y, this.getWidth(), r.height);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }
}
