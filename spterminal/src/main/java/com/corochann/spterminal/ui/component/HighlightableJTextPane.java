package com.corochann.spterminal.ui.component;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * JTextPane with extended feature that one line of background can be highlighted
 * Note: use {#repaint()} to update the graphics after setHighlightLine() call. (App-triggered painting)
 */
public class HighlightableJTextPane extends JTextPane {
    private static final int TAB_STOP_SIZE = 100;

    /** The line to be highlighted. -1 indicates that no line should be highlighted. */
    private int highlightLine = -1;
    /**
     * Highlight highlightColor
     * Color's alpha must be low value, in order to show text behind.
     */
    private Color highlightColor = new Color(51, 153, 255, 30); // Default highlightColor

    public HighlightableJTextPane() {
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

    /*--- StyleDocument util functions ---*/
    public void setTextWithColor(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        this.setCharacterAttributes(aset, false);
        this.setText(msg);
    }

    /**
     * Copied from {@link JTextArea} implementation.
     * Appends the given text to the end of the document.  Does nothing if
     * the model is null or the string is null or empty.
     *
     * @param str the text to insert
     */
    public void append(String str) {
        Document doc = getDocument();
        if (doc != null) {
            try {
                doc.insertString(doc.getLength(), str, null);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }

    /**
     * Ref: http://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea
     * @param msg
     * @param c
     */
    public void appendTextWithColor(String msg, Color c) {
        appendTextWithColor(msg, c, null);
    }

    /**
     *
     * @param str
     * @param fc  foreground color
     * @param bc  background color
     */
    public void appendTextWithColor(String str, Color fc, Color bc) {
        //System.out.println("appendTextWithColor, str = " + str + ", fc = " + fc + ", bc " + bc);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, fc);
        if(bc != null) aset = sc.addAttribute(aset, StyleConstants.Background, bc);
        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        StyledDocument doc = getStyledDocument();
        if (doc != null) {
            try {
                doc.insertString(doc.getLength(), str, aset);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }

    /**
     * (Copied from {@link JTextArea})
     * Replaces text from the indicated start to end position with the
     * new text specified.  Does nothing if the model is null.  Simply
     * does a delete if the new string is null or empty.
     *
     * @param str the text to use as the replacement
     * @param start the start position &gt;= 0
     * @param end the end position &gt;= start
     * @exception IllegalArgumentException  if part of the range is an
     *  invalid position in the model
     */
    public void replaceRange(String str, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end before start");
        }
        Document doc = getDocument();
        if (doc != null) {
            try {
                if (doc instanceof AbstractDocument) {
                    //System.out.println("[Debug] abstract doc, end = " + end + ", start = " + start + ", length = " + ((AbstractDocument)doc).getLength());
                    ((AbstractDocument)doc).replace(start, end - start, str, null);
                }
                else {
                    doc.remove(start, end - start);
                    doc.insertString(start, str, null);
                }
            } catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    /**
     *
     * @param str
     * @param fc  foreground color
     * @param bc  background color
     */
    public void replaceRangeTextWithColor(String str, int start, int end, Color fc, Color bc) {
        System.out.println("replaceRangeTextWithColor, str = " + str +
                ", start = " + start +
                ", end = " + end +
                ", fc = " + fc +
                ", bc " + bc
        );
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, fc);
        if(bc != null) aset = sc.addAttribute(aset, StyleConstants.Background, bc);
        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        StyledDocument doc = getStyledDocument();
        if (doc != null) {
            try {
                int len = end - start;
                if (len >= 0) {
                    doc.remove(start, len);
                } else {
                    throw new IllegalArgumentException("end before start");
                }
                doc.insertString(start, str, aset);
            } catch (BadLocationException ble) {
                ble.printStackTrace();
                throw new IllegalArgumentException(ble.getMessage());
            }
        }

        /* Deprecated way, use caret to update text. */
        //int caretPosition = this.getCaretPosition();  // Backup caret position.
        ////int len = this.getDocument().getLength();
        //this.setCaretPosition(start);
        //this.moveCaretPosition(end);
        //this.replaceSelection(str);
        //// set attribute
        //this.setCaretPosition(start);
        //this.moveCaretPosition(start + str.length());
        //this.setCharacterAttributes(aset, false);
        //
        //this.setCaretPosition(caretPosition);  // Revert the caret position.
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

    /**
     * Refer from {@link JTextArea} implementation.
     * Determines the offset of the end of the given line.
     *
     * @param line  the line &gt;= 0
     * @return the offset &gt;= 0
     * @exception BadLocationException Thrown if the line is
     * less than zero or greater or equal to the number of
     * lines contained in the document (as reported by
     * getLineCount).
     */
    public int getLineEndOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        Element map = this.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            // hide the implicit break at the end of the document
            return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
        }
    }

    /**
     * Determines the offset of the end of the text of given line.
     *
     * @param line  the line &gt;= 0
     * @return the offset &gt;= 0
     * @exception BadLocationException Thrown if the line is
     * less than zero or greater or equal to the number of
     * lines contained in the document (as reported by
     * getLineCount).
     */
    public int getLineEndOfTextOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        Element map = this.getDocument().getDefaultRootElement();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", this.getDocument().getLength() + 1);
        } else {
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            // hide the implicit break at the end of the document
            return endOffset - 1;
        }
    }

    /**
     * (Copied from {@link JTextArea})
     * Determines the number of lines contained in the area.
     * @return the number of lines &gt; 0
     */
    public int getLineCount() {
        Element map = getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    /**
     * Set Tab size, it should be called after {@link #setFont} with monospace font.
     * Raf:
     * http://stackoverflow.com/questions/33544621/java-setting-indent-size-on-jtextpane
     * http://www.java2s.com/Code/Java/Swing-JFC/ATabSetinaJTextPane.htm
     * @see {@link JTextArea#setTabSize(int)}
     * @param size
     */
    public void setTabSize(int size) {
        FontMetrics fm = getFontMetrics(getFont());
        int spaceWidth = fm.charWidth(' ');// one space pixel width
        int tabWidth = spaceWidth * size;  // Tab should be stopped at this pixel

        TabStop[] tabs = new TabStop[TAB_STOP_SIZE];
        for (int i = 0; i < TAB_STOP_SIZE; i++) {
            tabs[i] = new TabStop((i+1) * tabWidth);
        }
        TabSet tabSet = new TabSet(tabs);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        setParagraphAttributes(aset, false);
    }
}
