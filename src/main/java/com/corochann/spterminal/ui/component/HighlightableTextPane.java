package com.corochann.spterminal.ui.component;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * JTextPane with extended feature that one line of background can be highlighted
 * Note: use {#repaint()} to update the graphics after setHighlightLine() call. (App-triggered painting)
 */
public class HighlightableTextPane extends JTextPane {
    /** The line to be highlighted. -1 indicates that no line should be highlighted. */
    private int highlightLine = -1;
    /**
     * Highlight highlightColor
     * Color's alpha must be low value, in order to show text behind.
     */
    private Color highlightColor = new Color(51, 153, 255, 30); // Default highlightColor

    public HighlightableTextPane() {
        super();
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

    public void setTextWithColor(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        this.setCharacterAttributes(aset, false);
        this.setText(msg);
    }

    /**
     * Ref: http://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea
     * @param msg
     * @param c
     */
    public void appendTextWithColor(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = this.getDocument().getLength();
        this.setCaretPosition(len);
        this.setCharacterAttributes(aset, false);
        this.replaceSelection(msg);
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

    /**
     * Ref: http://stackoverflow.com/questions/2750073/how-to-use-caret-to-tell-which-line-it-is-in-from-jtextpane-java
     * @param line
     * @return
     * @throws BadLocationException
     */
    public int getLineStartOffset(int line) throws BadLocationException {
        Element map = this.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    public int getLineEndOffset(int line) throws BadLocationException {
        Element map = this.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= map.getElementCount()) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            return lineElem.getEndOffset();
        }
    }
}
