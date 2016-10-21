package com.corochann.spterminal.serial;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.log.MyAnsiLogger;
import com.corochann.spterminal.log.MyLogger;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.util.MyUtils;

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

    /**
     * This is the protocol how to handle received buffer
     * @param buffer received byte stream
     * @param k length of buffer
     */
    private synchronized void handleRXStream(final byte[] buffer, int k) {
        /* Show console, write to RXText */
        SPTerminal.getFrame().mTerminalPanel.writeRXText(buffer, 0, k);

        /* Save log */
        MyAnsiLogger myAnsiLogger = SPTerminal.getFrame().getAnsiLogger();
        if (myAnsiLogger != null) {
            myAnsiLogger.addLog(buffer, k);
        }

        {
            /* Save Raw log only for debug */
            MyLogger myLogger = SPTerminal.getFrame().getLogger();
            if (myLogger != null) {
                // Remove "\r" for Windows unnecessary new line string. Always use Linux format.
                String logStr = new String(buffer, 0 , k).replace("\r", "");
                //System.out.println("[Debug] logStr = " + MyUtils.unEscapeString(logStr));
                myLogger.addLog(logStr);
            }
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
