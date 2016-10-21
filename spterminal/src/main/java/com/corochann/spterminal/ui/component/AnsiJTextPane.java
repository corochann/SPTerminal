package com.corochann.spterminal.ui.component;

import com.corochann.spterminal.config.style.StyleConfig;
import com.corochann.spterminal.util.MyUtils;
import org.fusesource.jansi.AnsiOutputStream;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Custom {@link JTextPane} class which supports Ansi escape character.
 *
 */
public class AnsiJTextPane extends HighlightableJTextPane {
    /*--- Relations ---*/
    private StyleConfig styleConfig;
    private CustomAnsiOutputStream customAnsiOutputStream;
    private JScrollBar verticalScrollBar;

    /*--- Attributes ---*/
    private int mCursorPosition = -1;  // cursor is used for ANSI escape string, -1 indicate the end of document.
    private int mCursorPositionRow = 0;
    private int mCursorPositionCol = 0;
    private int mViewPortTopRow = 0;
    private int mCursorPositionBackup = -1;  // cursor is used for ANSI escape string, -1 indicate the end of document.
    private int mCursorPositionRowBackup = 0;
    private int mCursorPositionColBackup = 0;
    private int mViewPortTopRowBackup = 0;
    private Color mCurrentForeGroundColor = null;  // null means use default value
    private Color mCurrentBackGroundColor = null;  // null means use default value


    public AnsiJTextPane(StyleConfig styleConfig) {
        super();
        customAnsiOutputStream = new CustomAnsiOutputStream();
        this.styleConfig = styleConfig;
        setMonospaceFont(styleConfig.getTerminalFont());
    }

    public void setMonospaceFont(Font font) {
        setFont(font);
        // Terminal default tab size is 8,
        // It must be invoked after setFont()
        setTabSize(8);
    }

    public void decrementCursorPositionRow() {
        mCursorPositionRow--;
        if (mCursorPositionRow < 0) mCursorPositionRow = 0;
    }

    /**
     * set vertical scroll bar for reference.
     * @param scrollBar
     */
    public void setVerticalScrollBar(JScrollBar scrollBar) {
        this.verticalScrollBar = scrollBar;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        customAnsiOutputStream.write(b, off, len);
    }

    /**
     * Must be invoked in EDT.
     */
    public void flush() {
        customAnsiOutputStream.flush();  // make sure to flush
    }

    /** Must be invoked in EDT. */
    public void flushWithDelay(int delay) {
        customAnsiOutputStream.flushWithDelay(delay);
    }

    /**
     * Clear this terminal
     * Must be invoked in EDT.
     */
    public synchronized void clearScreen() {
        this.setText("");
        mCursorPosition = -1;
        mCursorPositionRow = 0;
        mCursorPositionCol = 0;
        mViewPortTopRow = 0;
        mCursorPositionBackup = -1;
        mCursorPositionRowBackup = 0;
        mCursorPositionColBackup = 0;
        mViewPortTopRowBackup = 0;
        mCurrentForeGroundColor = null;
        mCurrentBackGroundColor = null;
    }

    /*--- INNER CLASS ---*/
    /**
     * Writes bytes[] to JTextArea.
     */
    private class AnsiJTextAreaOutputStream extends OutputStream {
        private static final int BUFFER_SIZE = 1 << 15;
        byte[] buffer = new byte[BUFFER_SIZE];
        int pos = 0;

        ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
        AnsiJTextPane parent;
        private Timer timer = null;

        AnsiJTextAreaOutputStream() {
            super();
            parent = AnsiJTextPane.this;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            /**
             * Write/replace the string specfied by byte to JTextArea,
             * according to the current state of {@link AnsiJTextPane}.
             */
            char[] chars = {(char)b};
            bb.put(new String(chars).getBytes());
        }

        @Override
        public void write(byte[] b) throws IOException {
            bb.put(b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            byte[] tmpBuffer = Arrays.copyOfRange(b, off, off+len);
            System.out.println("write off = " + off + ", len = " + len
                    + ", position " + bb.position()
                    + ", capacity " + bb.capacity()
                    + ", limit " + bb.limit()
                    + ", tmpBuffer.length " + tmpBuffer.length
            );
            bb.put(tmpBuffer);
        }

        /** Flush curretnly buffered bb into text pane. */
        public synchronized void flush() {
            /* write bb */
            bb.flip();

            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            //System.out.println("[Debug] flush" + ", b " + Arrays.toString(b) + ", length = " + b.length);

            int offset = 0;
            int len = 0;
            int i = 0;
            while (i < b.length) {
                if (b[i] == 8) { // backspace.
                    len = i - offset;
                    flushString(new String(b, offset, len));
                    // remove 1 character
                    flushBackspace();
                    // update offset
                    i++;
                    offset = i;
                } else {
                    i++;
                }
            }
            len = b.length - offset;
            flushString(new String(b, offset, len));

            if (getLineCount() - 1 == mCursorPositionRow) {
                try {
                    if (getLineEndOffset(mCursorPositionRow) - getLineStartOffset(mCursorPositionRow) == mCursorPositionCol) {
                        mCursorPosition = -1;
                    }
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }

            bb.clear();
        }

        /** Flush currently buffered bb into text pane with following strategy
         *
         * 1. Until the last new line char       -> flush immediately (and reset delay timer)
         * 2. the last line without new line yet -> flush after delay[ms]. (start delay timer)
         */
        public synchronized void flushWithDelay(int delay) {
            /* write bb */
            bb.flip();

            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            //System.out.println("[Debug] flush" + ", b " + Arrays.toString(b) + ", length = " + b.length);

            int offset = 0;
            int len = 0;
            int i = 0;
            int lastLF = 0;
            while (i < b.length) {
                if (b[i] == 8) { // backspace.
                    len = i - offset;
                    flushString(new String(b, offset, len));
                    // remove 1 character
                    flushBackspace();
                    // update offset
                    i++;
                    offset = i;
                } else if (b[i] == 10) {  // LF_CHAR, Line feed, "\n"
                    i++;
                    lastLF = i;
                } else {
                    i++;
                }
            }
            //len = b.length - offset;
            len = lastLF - offset;
            if (len > 0) {
                // reset timer
                if (timer != null) {
                    //System.out.println("reset timer");
                    timer.stop();
                    timer = null;
                }
                flushString(new String(b, offset, len));
                offset = lastLF;
            }

            if (getLineCount() - 1 == mCursorPositionRow) {
                try {
                    if (getLineEndOffset(mCursorPositionRow) - getLineStartOffset(mCursorPositionRow) == mCursorPositionCol) {
                        mCursorPosition = -1;
                    }
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }

            bb.clear();

            len = b.length - offset;
            if (len > 0) {
                bb.put(b, offset, len);
                // start timer
                timer = new Timer(delay, new FlushTimerActionListener());
                timer.setRepeats(false);
                //System.out.println("start timer");
                timer.start();
            }
        }

        /**
         * Remove one character from current cursor position in JTextPane
         */
        private synchronized void flushBackspace() {
            System.out.println("flushBackspace mCursorPositionCol = " + mCursorPositionCol);
            try {
                if (mCursorPositionCol > 0) {
                    int cursor = getCursorPositionOffset();
                    parent.replaceRange("", cursor - 1, cursor);
                    mCursorPositionCol--;
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }

        }

        /**
         * flush str to JTextPane
         * @param str
         */
        private synchronized void flushString(String str) {
            //System.out.println(
            //        "[DEBUG] flush str = " + MyUtils.unEscapeString(str)
            //        + ", mCursorPosition = " + mCursorPosition
            //);
            if (mCursorPosition == -1) {  // append at the end of text
                if (mCurrentForeGroundColor == null) {
                    parent.append(str);  // append without specifying color
                } else {
                    parent.appendTextWithColor(str, mCurrentForeGroundColor, mCurrentBackGroundColor);
                }
                /* Update cursor position */
                mCursorPositionRow = getLineCount() - 1;
                try {
                    mCursorPositionCol = getLineEndOfTextOffset(mCursorPositionRow) - getLineStartOffset(mCursorPositionRow);
                } catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
                updateViewPortTopRow();
            } else {  // current cursor position is not at the end. replace text.
                int pos = 0;

                while (true) {
                    int nPos = str.indexOf('\n', pos);
                    int rPos = str.indexOf('\r', pos);

                    String subStr;
                    boolean newlineFlag = true;
                    if (nPos == -1 && rPos == -1) {
                        subStr = str.substring(pos, str.length());
                        pos = str.length();
                        newlineFlag = false;
                    } else if (nPos == -1) {
                        subStr = str.substring(pos, rPos);
                        pos = rPos + 1;
                    } else if (rPos == -1) {
                        subStr = str.substring(pos, nPos);
                        pos = nPos + 1;
                    } else {
                        if (nPos < rPos) {
                            subStr = str.substring(pos, nPos);
                            pos = nPos + 1;
                        } else {
                            subStr = str.substring(pos, rPos);
                            if (rPos + 1 == nPos) {
                                pos = nPos + 1;
                            } else {
                                pos = rPos + 1;
                            }
                        }
                    }
                    //if (newlineFlag) subStr += "\n";
                    System.out.println("flushing substr = " + MyUtils.unEscapeString(subStr));

                    try {
                        /* NOTE: getLineEndOffset() includes the last "\n",
                         * subtracting 1 means ignoring to replace "\n" at the end of line
                         */
                        int start = getCursorPositionOffset();
                        int endOfTextOffset = getLineEndOfTextOffset(mCursorPositionRow);
                        int end = Math.min(start + subStr.length(), endOfTextOffset);

                        //int end = getLineEndOffset(mCursorPositionRow);
                        if (start + subStr.length() >= endOfTextOffset) {
                            System.out.println("[CAUTION] original text is longer than replaced text!!");
                        }
                        System.out.println("[without color] getLineStartOffset(mCursorPositionRow) " + getLineStartOffset(mCursorPositionRow)
                                + ", getLineEndOffset(mCursorPositionRow) " + getLineEndOffset(mCursorPositionRow)
                                + ", getLineCount " + getLineCount()
                                + ", endOfTextOffset " + endOfTextOffset
                                + ", mCursorPositionRow " + mCursorPositionRow
                                + ", mCursorPositionCol " + mCursorPositionCol
                                + ", subStr.length = " + subStr.length()
                                + ", start = " + start
                                + ", end = " + end);

                        if (start <= end) {
                            if (mCurrentForeGroundColor == null) {
                                parent.replaceRange(subStr, start, end);
                            } else {
                                parent.replaceRangeTextWithColor(subStr, start, end, mCurrentForeGroundColor, mCurrentBackGroundColor);
                            }
                        } else {
                            System.out.println("[ERROR] start > end");
                            parent.replaceRange(subStr, start, start);
                        }
                    } catch (BadLocationException ble) {
                        ble.printStackTrace();
                        System.out.println("mCursorPositionRow = " + mCursorPositionRow);
                        mCursorPositionRow = getLineCount() - 1;
                    }

                    //System.out.println("After write mCursorPositionRow = " + mCursorPositionRow
                    //        + ", getLineCount() = " + getLineCount()
                    //        + ", newlineFlag = " + newlineFlag
                    //);
                    if (newlineFlag) {
                        mCursorPositionRow++;
                        if (mCursorPositionRow >= getLineCount()) {
                            System.out.println("[DEBUG] mCursorPositionRow exceeds getLineCount.");
                            // Already exceeding original text length
                            mCursorPosition = -1;
                            append("\n");
                        }
                        updateViewPortTopRow();
                        mCursorPositionCol = 0;
                    } else {
                        mCursorPositionCol += subStr.length();
                    }


                    if (pos >= str.length()) break;
                }
            }
        }

        private class FlushTimerActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("FlushTimerActionListener actionPerformed");
                flush();
            }
        }
    }

    private int getCursorPositionOffset() throws BadLocationException {
        return getLineStartOffset(mCursorPositionRow) + mCursorPositionCol;
    }

    private void updateViewPortTopRow() {
        int visibleRow = verticalScrollBar.getVisibleAmount() / getFontMetrics(getFont()).getHeight();
        mViewPortTopRow = Math.max(mViewPortTopRow, mCursorPositionRow - visibleRow + 1);
        if (mViewPortTopRow < 0) mViewPortTopRow = 0;
        //System.out.println("[DEBUG] updateViewPortTopRow mViewPortTopRow = " + mViewPortTopRow
        //        + ", mCursorPositionRow = " + mCursorPositionRow
        //        + ", visibleRow = " + visibleRow
        //);
    }

    /**
     * Handles bytes[] with Ansi escape sequence.
     * It will access {@link AnsiJTextAreaOutputStream} to write bytes[] to JTextArea.
     */
    private class CustomAnsiOutputStream extends AnsiOutputStream {
        /*--- Ignore char list ---*/
        //public static final int LF_CHAR = 10;
        public static final int BELL_CHAR = 7;
        // CR and LF will appear in the log, ignore CR and only print LF (Linux style)
        public static final int CR_CHAR = 13;
        public static final int SO_CHAR = 14;
        public static final int SI_CHAR = 15;  // SI Appears in "top" command.

        AnsiJTextPane parent;
        /**
         * Default constructor.
         * See {@link CustomAnsiOutputStream(OutputStream)} for detail.
         */
        public CustomAnsiOutputStream() {
            this(null);
        }

        /**
         * We always use {@link AnsiJTextAreaOutputStream} as argument.
         * So this method cannot be called, use {@link CustomAnsiOutputStream()} instead.
         * @param os
         */
        private CustomAnsiOutputStream(OutputStream os) {
            super(new AnsiJTextAreaOutputStream());
            parent = AnsiJTextPane.this;
        }

        @Override
        public void write(int data) throws IOException {
            //System.out.println("[Debug] CustomAnsiOutputStream write + " + data);
            if (data == BELL_CHAR || data == SO_CHAR || data == SI_CHAR || data == CR_CHAR) {
                // Ignore these characters
                return;
            }
            //TODO: Temporal C/M. Review.
            if (data == 13) { // Reset ANSI Escape attributes at the end of line.
                processAttributeRest();
            }
            super.write(data);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            //System.out.println("[Debug] CustomAnsiOutputStream write " + ", b " + Arrays.toString(Arrays.copyOfRange(b, off, off + len))
            //        + ", off " + off + ", len " + len
            //        + ", str = " + new String(b, off, len)
            //);
            super.write(b, off, len);
        }

        public void flush() {
            ((AnsiJTextAreaOutputStream) out).flush();
        }

        public void flushWithDelay(int delay) {
            ((AnsiJTextAreaOutputStream) out).flushWithDelay(delay);
        }

        /*--- Override various process methods... ---*/
        /* 'm' option */
        @Override
        protected void processSetAttribute(int attribute) throws IOException {
            System.out.println("processSetAttribute, attribute = " + attribute);
            flush();
            // TODO: impl.
        }

        @Override
        protected void processSetForegroundColor(int color) throws IOException {
            flush();
            this.processSetForegroundColor(color, false);
        }

        @Override
        protected void processSetForegroundColor(int color, boolean bright) throws IOException {
            System.out.println("processSetForegroundColor, " + " color = " + color + " bright = " + bright);
            flush();
            mCurrentForeGroundColor = getAnsiColor(color, bright);
        }

        @Override
        protected void processSetBackgroundColor(int color) throws IOException {
            flush();
            processSetBackgroundColor(color, false);
        }

        @Override
        protected void processSetBackgroundColor(int color, boolean bright) throws IOException {
            System.out.println("processSetBackgroundColor, " + " color = " + color + " bright = " + bright);
            flush();
            mCurrentBackGroundColor = getAnsiColor(color, bright);
        }

        @Override
        protected void processDefaultTextColor() throws IOException {
            System.out.println("processDefaultTextColor");
            flush();
            mCurrentForeGroundColor = null;
        }

        @Override
        protected void processDefaultBackgroundColor() throws IOException {
            System.out.println("processDefaultBackgroundColor");
            flush();
            mCurrentBackGroundColor = null;
        }

        @Override
        protected void processAttributeRest() throws IOException {
            //System.out.println("processAttributeRest");
            flush();
            mCurrentForeGroundColor = null;
            mCurrentBackGroundColor = null;
        }

        @Override
        protected void processNegativeImage() {
            flush();
            Color foregroundColor = styleConfig.getBaseForeGroundColor();
            Color backgroundColor = styleConfig.getBaseBackGroundColor();
            if (foregroundColor == null) {
                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                foregroundColor = (ColorUIResource) uiDefaults.get("TextPane.foreground");
            }
            if (backgroundColor == null) {
                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                backgroundColor = (ColorUIResource) uiDefaults.get("TextPane.background");
            }
            System.out.println("processNegativeImage, original foregroundColor = " + foregroundColor + ", backgroundColor = " + backgroundColor);
            mCurrentForeGroundColor = backgroundColor;
            mCurrentBackGroundColor = foregroundColor;
        }

        @Override
        protected void processRestoreCursorPosition() throws IOException {
            flush();
            //TODO: review
            mCursorPosition = mCursorPositionBackup;
            mCursorPositionRow = mCursorPositionRowBackup;
            mCursorPositionCol = mCursorPositionColBackup;
            mViewPortTopRow = mViewPortTopRowBackup;
        }

        @Override
        protected void processSaveCursorPosition() throws IOException {
            flush();
            //TODO: review
            mCursorPositionBackup = mCursorPosition;
            mCursorPositionRowBackup = mCursorPositionRow;
            mCursorPositionColBackup = mCursorPositionCol;
            mViewPortTopRowBackup = mViewPortTopRow;
        }

        @Override
        protected void processScrollDown(int optionInt) throws IOException {
            flush();
            //TODO: impl
            super.processScrollDown(optionInt);
        }

        @Override
        protected void processScrollUp(int optionInt) throws IOException {
            flush();
            //TODO: impl
            super.processScrollUp(optionInt);
        }

        @Override
        protected synchronized void processEraseScreen(int eraseOption) throws IOException {
            flush();
            //TODO: review
            /* It will erase existing log to clear screen.
             * Need to review that which is expected behavior
             * - Remove existing log on Screen              --> current behavior
             * - Keep log and prepare new space to take log
             */
            try {
                int startOffset = getLineStartOffset(mCursorPositionRow);
                replaceRange("", startOffset, getLineEndOffset(getLineCount() - 1));
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }

        @Override
        protected void processEraseLine(int eraseOption) throws IOException {
            flush();
            //TODO: review
            try {
                int startOffset = getLineStartOffset(mCursorPositionRow);
                int cursor = getCursorPositionOffset();
                int endOfTextOffset = getLineEndOfTextOffset(mCursorPositionRow);
                int start, end;
                switch (eraseOption) {
                    case 0:  // Clear from cursor to the end of line
                        start = cursor;
                        end = endOfTextOffset;
                        break;
                    case 1:  // Clear from cursor to the beginning of line
                        start = startOffset;
                        end = cursor;
                        break;
                    case 2:  // Clear entire line
                        start = startOffset;
                        end = endOfTextOffset;
                        break;
                    default:
                        System.out.println("[ERROR] processEraseLine Invalid option " + eraseOption);
                        return;
                }

                if (start <= end) {
                    replaceRange("", start, end);
                } else {
                    System.out.println("[ERROR] processEraseLine option" + eraseOption
                            + ", start = " + start
                            + ", end = " + end
                    );
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }

        @Override
        protected void processCursorTo(int row, int col) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            mCursorPositionRow = mViewPortTopRow + row - 1;
            if (mCursorPositionRow >= getLineCount()) {
                System.out.println("[ERROR] mCusrorPositionRow exceed linecount " + mCursorPositionRow + ", " + getLineCount());
                mCursorPositionRow = getLineCount() - 1;
            }
            mCursorPositionCol = col - 1;
            if (mCursorPositionCol < 0) {
                System.out.println("[ERROR] mCusrorPositionCol " + mCursorPositionCol);
                mCursorPositionCol = 0;
            }
        }

        @Override
        protected void processCursorToColumn(int x) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            mCursorPositionCol = x - 1;
        }

        @Override
        protected void processCursorUpLine(int count) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            mCursorPositionRow -= count;
            if (mCursorPositionRow < 0) mCursorPositionRow = 0;
        }

        @Override
        protected void processCursorDownLine(int count) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            int rowLength = getLineCount();
            mCursorPositionRow += count;
            if (mCursorPositionRow > rowLength - 1) {
                int exceed = mCursorPositionRow - rowLength + 1;
                mCursorPositionRow = rowLength - 1;
                for (int i = 0; i < exceed; i++) {
                    // append "\n".
                    write(13);
                }
            }
        }

        @Override
        protected void processCursorLeft(int count) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            mCursorPositionCol -= count;
            if (mCursorPositionCol < 0) mCursorPositionCol = 0;
        }

        @Override
        protected void processCursorRight(int count) throws IOException {
            flush();
            //TODO: review
            mCursorPosition = 0;
            try {
                int colLength = getLineEndOffset(mCursorPositionRow) - getLineStartOffset(mCursorPositionRow);
                mCursorPositionCol += count;
                if (mCursorPositionCol > colLength) {
                    int exceed = mCursorPositionCol - colLength + 1;
                    mCursorPositionCol = colLength;
                    for (int i = 0; i < exceed; i++) {
                        // append " ".
                        write(32);
                    }
                }
            } catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }

        @Override
        protected void processCursorUp(int count) throws IOException {
            flush();
            //TODO: review, now same with processCursorUpLine
            mCursorPosition = 0;
            mCursorPositionRow -= count;
            if (mCursorPositionRow < 0) mCursorPositionRow = 0;
        }

        @Override
        protected void processCursorDown(int count) throws IOException {
            flush();
            //TODO: review, now same with processCursorDownLine
            mCursorPosition = 0;
            int rowLength = getLineCount();
            mCursorPositionRow += count;
            if (mCursorPositionRow > rowLength - 1) {
                int exceed = mCursorPositionRow - rowLength + 1;
                mCursorPositionRow = rowLength - 1;
                for (int i = 0; i < exceed; i++) {
                    // append "\n".
                    write(13);
                }
            }
        }

        @Override
        protected void processUnknownExtension(ArrayList<Object> options, int command) {
            flush();
            super.processUnknownExtension(options, command);
        }

        @Override
        protected void processChangeIconNameAndWindowTitle(String label) {
            flush();
            super.processChangeIconNameAndWindowTitle(label);
        }

        @Override
        protected void processChangeIconName(String label) {
            flush();
            super.processChangeIconName(label);
        }

        @Override
        protected void processChangeWindowTitle(String label) {
            flush();
            super.processChangeWindowTitle(label);
        }

        @Override
        protected void processUnknownOperatingSystemCommand(int command, String param) {
            flush();
            super.processUnknownOperatingSystemCommand(command, param);
        }
    }

    private Color getAnsiColor(int color, boolean bright) {
        switch (color) {
            case 0: return bright ? styleConfig.getAnsiBlack() : styleConfig.getAnsiBrightBlack();
            case 1: return bright ? styleConfig.getAnsiRed(): styleConfig.getAnsiBrightRed();
            case 2: return bright ? styleConfig.getAnsiGreen(): styleConfig.getAnsiBrightGreen();
            case 3: return bright ? styleConfig.getAnsiYellow(): styleConfig.getAnsiBrightYellow();
            case 4: return bright ? styleConfig.getAnsiBlue(): styleConfig.getAnsiYellow();
            case 5: return bright ? styleConfig.getAnsiMagenta(): styleConfig.getAnsiBrightMagenta();
            case 6: return bright ? styleConfig.getAnsiCyan(): styleConfig.getAnsiBrightCyan();
            case 7: return bright ? styleConfig.getAnsiWhite(): styleConfig.getAnsiBrightWhite();
            default: return null;
        }

    }
}
