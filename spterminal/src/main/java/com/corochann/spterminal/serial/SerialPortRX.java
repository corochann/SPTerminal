package com.corochann.spterminal.serial;

import com.corochann.spterminal.config.ProjectConfig;
import com.corochann.spterminal.log.BufferLogger;
import com.corochann.spterminal.log.MyAnsiLogger;
import com.corochann.spterminal.log.MyLogger;
import com.corochann.spterminal.ui.SPTerminal;
import com.corochann.spterminal.util.MyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

/**
 * Receiver (RX) of serial port
 */
public class SerialPortRX extends Thread {

    private static final int MAX_RECEIVE_BYTE_SIZE = 4096;

    /*--- Relations ---*/
    private InputStream in;

    /*--- Attributes---*/
    private boolean runFlag = true;
    private boolean tabFlag = false;
    private Vector<BufferLogger> bufferLoggerVector = new Vector<>();

    public SerialPortRX(InputStream in) { this.in = in; }

    /**
     *
     * @param logger
     * @return id of this logger
     */
    public synchronized int addBufferLogger(BufferLogger logger) {
        int index = bufferLoggerVector.size();
        bufferLoggerVector.add(logger);
        System.out.println("addBufferLogger index = " + index);
        return index;
    }

    /**
     * Removes buffer logger explicitly with specified index
     * which is obtained at {@link #addBufferLogger(BufferLogger)}.
     *
     * [NOTE] Even if this method is not called, but the BufferLogger's isRunning = false,
     *        then logging will be stopped.
     */
    public synchronized BufferLogger removeBufferLogger(int index) {
        System.out.println("removeBufferLogger index = " + index);
        return bufferLoggerVector.remove(index);
    }

    public synchronized void removeAllBufferLogger() {
        System.out.println("removeAllBufferLogger");
        bufferLoggerVector = new Vector<>();
    }

    /** Receiving Thread: polling receiving byte data from other device's tx */
    public void run() {
        byte[] buffer = new byte[MAX_RECEIVE_BYTE_SIZE];
        try {
            int k;
            // if stream is not bound in.read() method returns -1
            while(runFlag) {
                k = in.read(buffer);
                if (k == -1) {
                    // wait 10ms when stream is broken and check again
                    System.out.println("sleep 10 ms");
                    sleep(10);
                    break;
                }
                handleRXStream(buffer, k);
            }
        } catch (IOException | InterruptedException e) {
            /* IOException happens when SerialPortManager.Finalize() called. */
            e.printStackTrace();
        }

        /* Finalize */
        synchronized (this) {
            try {
                in.close();
                System.out.println("in.close() done successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
            in = null;
            notify();
        }
    }

    /**
     * This is the protocol how to handle received buffer
     * @param buffer received byte stream
     * @param k length of buffer
     */
    private synchronized void handleRXStream(final byte[] buffer, int k) {
        //System.out.println("[Debug] k = " + k + ", buffer = " + Arrays.toString(Arrays.copyOfRange(buffer, 0, k)));

        /* Show console, write to RXText */
        SPTerminal.getFrame().mTerminalPanel.writeRXText(buffer, 0, k);

        /* Save log */
        for (int i = 0; i < bufferLoggerVector.size(); i++) {
            BufferLogger logger = bufferLoggerVector.get(i);
            if (logger != null) {
                logger.addLog(buffer, k);
            } else {
                System.out.println("[ERROR] logger is null at index " + i);
            }
        }

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
        try {
            if (in != null) wait(1000);  // wait until thread finishes.
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}
