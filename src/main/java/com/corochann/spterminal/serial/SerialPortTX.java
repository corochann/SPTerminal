package com.corochann.spterminal.serial;

import com.corochann.spterminal.util.MyUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Transmitter (TX) of serial port
 */
public class SerialPortTX {
    private OutputStream out;

    public SerialPortTX(OutputStream out) {
        this.out = out;
    }

    public void transmitString(String str) {
        // UnEscape |str| to understand which character is transmitted.
        //System.out.println("TX string: " + MyUtils.unEscapeString(str));
        System.out.println("TX string: " + MyUtils.unEscapeString(str) + " = " + Arrays.toString(str.getBytes()));

        transmitBytes(str.getBytes());
    }

    public void transmitAscii(int asciiDecimalValue) {
        System.out.println("TX ascii: " + asciiDecimalValue);
        byte[] bytes = new byte[2];
        bytes[0] = new Integer(asciiDecimalValue).byteValue();
        transmitBytes(bytes);
    }

    public void transmitBytes(byte[] bytes) {
        try {
            //System.out.println("TX: " + new String(bytes, 0, bytes.length));

            // sending through serial port is simply writing into OutputStream
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Special method to transmit new line
     * Usually CR (13) or CR+LF (13, 10) is used.
     */
    public void transmitNewLine() {
        transmitAscii(13);       // CR: Ctrl-M, Carriage feed, Carriage return
        //transmitString("\n");  // LF: Same with Ascii 10. Ctrl-J, Line feed.
    }

    public void Finalize() {
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
