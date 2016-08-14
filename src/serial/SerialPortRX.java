package serial;

import ui.SPTerminal;
import util.MyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Receiver (RX) of serial port
 */
public class SerialPortRX extends Thread {

    private static final int MAX_RECEIVE_BYTE_SIZE = 4096;

    private InputStream in;
    private boolean runFlag = true;
    private boolean tabFlag = false;

    public SerialPortRX(InputStream in) { this.in = in; }

    /** Receiving Thread: polling receiving byte data from other device's tx */
    public void run() {
        byte[] buffer = new byte[MAX_RECEIVE_BYTE_SIZE];
        try {
            int k;
            while(runFlag) {
                // if stream is not bound in.read() method returns -1
                while(runFlag) {
                    k = in.read(buffer);
                    if (k == -1) break;
                    handleRXStream(buffer, k);
                }
                // wait 10ms when stream is broken and check again
                System.out.println("sleep 10 ms");
                sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            /* IOException happens when SerialPortManager.Finalize() called. */
            e.printStackTrace();
        }

        /* Finalize */
        try {
            in.close();
            System.out.println("in.close() done successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private byte[] formattedBuffer = new byte[MAX_RECEIVE_BYTE_SIZE];
    private byte[] lastLineBuffer = new byte[MAX_RECEIVE_BYTE_SIZE];
    private int lastLineCurrentPos = 0;

    /**
     * This is the protocol how to handle received buffer
     * @param buffer received byte stream
     * @param k length of buffer
     */
    private synchronized void handleRXStream(final byte[] buffer, int k) {
        /* copy lastLineBugger to formattedBuffer */
        boolean replaceFlag = lastLineCurrentPos > 0;
        int currentPos = lastLineCurrentPos;
        System.arraycopy(lastLineBuffer, 0, formattedBuffer, 0, lastLineCurrentPos);

        /* formatting received buffer. */
        for (int scan = 0; scan < k; scan++) {
            if (buffer[scan] == 7) { // 7 == BELL
                // Ignore BELL string, do nothing
            } else if (buffer[scan] == 8) { // 8 == backspace
                    if (currentPos == 0 || lastLineCurrentPos == 0) {
                        System.out.println("[ERROR] backspace detected with currentPos = " + currentPos + ", lastLineCurrentPos = " + lastLineCurrentPos);
                    } else {
                        currentPos--;
                        lastLineCurrentPos--;
                    }
            } else {
                formattedBuffer[currentPos++] = buffer[scan];
                if (buffer[scan] == 10) { // 10 == "\n"
                    lastLineCurrentPos = 0;
                } else {
                    lastLineBuffer[lastLineCurrentPos++] = buffer[scan];
                }
            }
        }

        /* Show it on log */
        if (!replaceFlag) {
            /* It is ok to just append received buffer */
            String rxStr = new String(formattedBuffer, 0, currentPos);
            SPTerminal.getFrame().mTerminalPanel.appendRXText(rxStr);
        } else {
            /* Need to replace last line with received buffer */
            String rxStr = new String(formattedBuffer, 0, currentPos);
            SPTerminal.getFrame().mTerminalPanel.replaceLastlineRXText(rxStr);
        }

        //String lastLineLog = new String(lastLineBuffer, 0, lastLineCurrentPos);
        //System.out.print(message);  // Debug print
        //System.out.println("[Debug] k = " + k + ", buffer = " + Arrays.toString(Arrays.copyOfRange(buffer, 0, k)));
        //System.out.println("[Debug] lastLineCurrentPos = " + lastLineCurrentPos + ", lastLineBuffer = " + Arrays.toString(Arrays.copyOfRange(lastLineBuffer, 0, lastLineCurrentPos)));
        //System.out.println("[Debug] laseLineLog = " + lastLineLog);

        /* Tab auto-completion. This is independent process */
        if (tabFlag) {
            System.out.println("TAB k = " + k + ", buffer = " + Arrays.toString(Arrays.copyOfRange(buffer, 0, k)));
            tabFlag = false;
            //TODO: handle received message
            // compare received message and last line of mRXTextArea
            // if message not contain "\n" && mRXTextArea last line text == ~~~ (#/$) mTXTextField + message,
            // we can successfully auto-complete, other case considered as exception case for now.

            String guess = new String(buffer, 0, k);

            if (guess.contains("\n")) {
                guess = "";
            } else {
                guess = guess.replace("\007", "");    // remove BELL
                if (!guess.equals("")) {
                    SPTerminal.getFrame().mTerminalPanel.appendTXTextFieldText(guess);
                }
            }
            System.out.println("TAB AUTO-COMPLETION guess = " + MyUtils.unEscapeString(guess));
        }
    }

    public synchronized void setTabFlag() {
        tabFlag = true;
    }

    /** To stop/destory Thread. */
    public synchronized void close() {
        runFlag = false;
    }

}
