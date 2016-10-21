package com.corochann.spterminal.test;

import java.io.*;

/**
 *
 */
public class MockRX extends Thread {

    public static final int CASE_1 = 1;


    /*--- Attribute ---*/
    private int debugCase = CASE_1;

    private PipedInputStream in;
    private PipedOutputStream out;



    public MockRX() {
        this(CASE_1);
    }

    public MockRX(int debugCase) {
        this.debugCase = debugCase;
        this.out = new PipedOutputStream();

        try {
            this.in = new PipedInputStream(out);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public InputStream getMockInputStream() {
        return in;
    }

    @Override
    public void run() {
        try {
            sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        try {
            switch (debugCase) {
                case CASE_1:
                    byte[] b1 = {
                            97, 98, 99, 13, 10, 65, 66, 67, 13, 10, // "abc\r\nABC\r\n"
                            27, 91, 72, 68, 69, 70, 71, 72, 73, 13, 10, 74, 75, 76, 13, 10, 99, 100,     // "CSI-H DEFGHI\r\nJKL\r\nde"
                            27, 91, 50, 59, 49, 72, 104, 105, 13, 10, 112, 113, 114,      // "CSI-2;1-H hi\r\npqr"
                    };
                    out.write(b1);
                    out.flush();
                    break;
                default:
                    break;
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
