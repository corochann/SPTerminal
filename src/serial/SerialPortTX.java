package serial;

import util.MyUtils;

import java.io.IOException;
import java.io.OutputStream;

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
        System.out.println("TX string: " + MyUtils.unEscapeString(str));
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
