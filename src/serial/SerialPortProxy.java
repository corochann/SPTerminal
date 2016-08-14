package serial;

import config.SPTerminalPreference;
import config.SerialPortConfig;
import purejavacomm.SerialPort;

/**
 * Proxy class between purejavacomm library and {@link config.SerialPortConfig}.
 */
public class SerialPortProxy {

    /* Default value setting */
    public static final String DEFAULT_BAUDRATE = SerialPortConfig.BAUDRATE_115200;
    public static final int DEFAULT_BAUDRATE_INT = Integer.parseInt(DEFAULT_BAUDRATE);
    public static final String DEFAULT_DATABIT = SerialPortConfig.DATABIT_8;
    public static final String DEFAULT_STOPBIT = SerialPortConfig.STOPBIT_1;
    public static final String DEFAULT_PARITY = SerialPortConfig.PARITY_NONE;
    public static final int DEFAULT_RTSCTS_IN = 0;
    public static final int DEFAULT_RTSCTS_OUT = 0;
    public static final int DEFAULT_XONXOFF_IN = 0;
    public static final int DEFAULT_XONXOFF_OUT = 0;


    SPTerminalPreference preference = null;

    public SerialPortProxy() {
        preference = SPTerminalPreference.getInstance();
    }

    public int getBaudRate() {
        /* lazy, no switch proxy method implemented */
        return preference.getIntValue(SerialPortConfig.KEY_BAUDRATE, DEFAULT_BAUDRATE_INT);
    }

    public int getDatabit() {
        String databitConfig = preference.getStringValue(SerialPortConfig.KEY_DATABIT, DEFAULT_DATABIT);
        switch (databitConfig) {
            case SerialPortConfig.DATABIT_5: return SerialPort.DATABITS_5;
            case SerialPortConfig.DATABIT_6: return SerialPort.DATABITS_6;
            case SerialPortConfig.DATABIT_7: return SerialPort.DATABITS_7;
            case SerialPortConfig.DATABIT_8: return SerialPort.DATABITS_8;
            default:
                System.out.println("[ERROR] Unknown databitConfig = " + databitConfig);
                return -1;
        }
    }

    public int getStopbit() {
        String stopbitConfig = preference.getStringValue(SerialPortConfig.KEY_STOPBIT, DEFAULT_STOPBIT);
        switch (stopbitConfig) {
            case SerialPortConfig.STOPBIT_1: return SerialPort.STOPBITS_1;
            case SerialPortConfig.STOPBIT_1_5: return SerialPort.STOPBITS_1_5;
            case SerialPortConfig.STOPBIT_2: return SerialPort.STOPBITS_2;
            default:
                System.out.println("[ERROR] Unknown stopbitConfig = " + stopbitConfig);
                return -1;
        }
    }

    public int getParity() {
        String parityConfig = preference.getStringValue(SerialPortConfig.KEY_PARITY, DEFAULT_PARITY);
        switch (parityConfig) {
            case SerialPortConfig.PARITY_NONE: return SerialPort.PARITY_NONE;
            case SerialPortConfig.PARITY_ODD: return SerialPort.PARITY_ODD;
            case SerialPortConfig.PARITY_EVEN: return SerialPort.PARITY_EVEN;
            case SerialPortConfig.PARITY_MARK: return SerialPort.PARITY_MARK;
            case SerialPortConfig.PARITY_SPACE: return SerialPort.PARITY_SPACE;
            default:
                System.out.println("[ERROR] Unknown parityConfig = " + parityConfig);
                return -1;
        }
    }

    public int getFlowControl() {
        int rtsctsInConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_IN, DEFAULT_RTSCTS_IN);
        int rtsctsOutConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_RTSCTS_OUT, DEFAULT_RTSCTS_OUT);
        int xonxoffInConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_IN, DEFAULT_XONXOFF_IN);
        int xonxoffOutConfig = preference.getIntValue(SerialPortConfig.KEY_FLOWCONTROL_XONXOFF_OUT, DEFAULT_XONXOFF_OUT);
        int flowControl = SerialPort.FLOWCONTROL_NONE
                        + rtsctsInConfig * SerialPort.FLOWCONTROL_RTSCTS_IN
                        + rtsctsOutConfig * SerialPort.FLOWCONTROL_RTSCTS_OUT
                        + xonxoffInConfig * SerialPort.FLOWCONTROL_XONXOFF_IN
                        + xonxoffOutConfig * SerialPort.FLOWCONTROL_XONXOFF_OUT;
        return flowControl;
    }

}
