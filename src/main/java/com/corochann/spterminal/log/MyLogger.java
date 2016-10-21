package com.corochann.spterminal.log;

import com.corochann.spterminal.config.ProjectConfig;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Handles logging functionality
 * {@link #addLog(String)} method receives {@link String} to save log in file.
 */
public class MyLogger {
    public static final String LOG_FILE_NAME_PREFIX = "raw_";

    private BufferedWriter mBW = null;
    private final String logFilePath;
    private final LoggingThread loggingThread;

    /**
     * Constructor.
     * @param logFileName Should be specified in {@link SimpleDateFormat} notation.
     */
    public MyLogger(String logFileName, String portName) {
        SimpleDateFormat sdf = new SimpleDateFormat(logFileName);
        Calendar c = Calendar.getInstance();
        String actualLogFileName = sdf.format(c.getTime()).replace("&h", portName);
        logFilePath = ProjectConfig.LOG_FOLDER + LOG_FILE_NAME_PREFIX + actualLogFileName;
        System.out.println("Start logging. log file path = " + logFilePath);
        loggingThread = new LoggingThread();

        try {
            mBW = new BufferedWriter(new FileWriter(logFilePath));
            loggingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("loggingThread not start.");
        }
    }

    public void addLog(String log) {
        loggingThread.send(log);
    }

    public int Finalize() {
        int ret = -1;
        if (mBW != null) {
            System.out.println("Finish writing to log " + logFilePath);
            loggingThread.isActive = false;
            ret = 0;
        }
        return ret;
    }

    private class LoggingThread extends Thread {

        private String buf = "";
        private boolean isActive = true;

        public LoggingThread() {

        }

        public void run() {
            while (isActive) {
                synchronized (this) {
                    if (buf.equals("")) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                    try {
                        mBW.write(buf);
                        mBW.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    buf = "";
                }
            }

            /* Finalize */
            try {
                mBW.write(buf);
                mBW.flush();
                mBW.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        private synchronized void send(String str) {
                buf += str;
                notify();
        }
    }
}
