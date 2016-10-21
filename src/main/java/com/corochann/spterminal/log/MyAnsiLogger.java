package com.corochann.spterminal.log;

import com.corochann.spterminal.config.ProjectConfig;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Handles logging functionality
 * {@link #addLog} method receives {@link Byte} to save log in file.
 */
public class MyAnsiLogger {
    public static final String LOG_FILE_NAME_PREFIX = "";  // No prefix

    private AnsiLogOutputStream mAOS = null;
    private final String logFilePath;
    private final LoggingThread loggingThread;

    /**
     * Constructor.
     * @param logFileName Should be specified in {@link SimpleDateFormat} notation.
     */
    public MyAnsiLogger(String logFileName, String portName) {
        SimpleDateFormat sdf = new SimpleDateFormat(logFileName);
        Calendar c = Calendar.getInstance();
        String actualLogFileName = sdf.format(c.getTime()).replace("&h", portName);
        logFilePath = ProjectConfig.LOG_FOLDER + LOG_FILE_NAME_PREFIX + actualLogFileName;
        System.out.println("Start logging. log file path = " + logFilePath);
        loggingThread = new LoggingThread();

        try {
            mAOS = new AnsiLogOutputStream(new FileOutputStream(logFilePath));
            loggingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("loggingThread not start.");
        }
    }

    public void addLog(byte[] b, int len) {
        loggingThread.send(b, len);
    }

    public int Finalize() {
        int ret = -1;
        if (mAOS != null) {
            System.out.println("Finish writing to log " + logFilePath);
            loggingThread.isActive = false;
            ret = 0;
        }
        return ret;
    }

    private class LoggingThread extends Thread {
        private static final int MAX_RECEIVE_BYTE_SIZE = 4096;

        //private String buf = "";

        private byte[] buffer = new byte[MAX_RECEIVE_BYTE_SIZE];
        private int currentLen = 0; // buffer length
        private boolean isActive = true;

        public LoggingThread() {

        }

        public void run() {
            while (isActive) {
                synchronized (this) {
                    if (currentLen <= 0) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                    try {
                        mAOS.write(buffer, 0, currentLen);
                        mAOS.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    currentLen = 0;
                }
            }

            /* Finalize */
            try {
                mAOS.write(buffer, 0, currentLen);
                mAOS.flush();
                mAOS.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        private synchronized void send(byte[] b, int len) {
            System.arraycopy(b, 0, buffer, currentLen, len);
            currentLen += len;
            notify();
        }
    }

    /**
     * This output stream class handles ansi escape character
     * and ignore some ASCII bytes to print in the log file.
     */
    public static class AnsiLogOutputStream extends AnsiOutputStream {
        // TODO: Review, which char should be ignored for logging.
        /*--- Ignore char list ---*/
        //public static final int LF_CHAR = 10;
        // CR and LF will appear in the log, ignore CR and only print LF (Linux style)
        public static final int CR_CHAR = 13;  // TODO:review. Is it ok to ignore CR?
        public static final int SO_CHAR = 14;
        public static final int SI_CHAR = 15;  // SI Appears in "top" command.

        public AnsiLogOutputStream(OutputStream os) {
            super(os);
        }

        @Override
        public void write(int data) throws IOException {
            if (data == CR_CHAR || data == SO_CHAR || data == SI_CHAR) {
                // Ignore these characters
                return;
            }
            super.write(data);
        }
    }
}
