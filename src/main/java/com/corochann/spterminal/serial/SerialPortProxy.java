package com.corochann.spterminal.serial;

import com.corochann.spterminal.config.SPTerminalPreference;
import com.corochann.spterminal.config.SerialPortConfig;
import purejavacomm.SerialPort;

/**
 * Proxy class between purejavacomm library and {@link SerialPortConfig}.
 */
public class SerialPortProxy {
    /*--- Attribute ---*/
    private SerialPortConfig mSerialPortConfig;

    public SerialPortProxy() {
        mSerialPortConfig = SPTerminalPreference.getInstance().getSerialPortConfig();
    }

    public int getBaudRate() {
        /* lazy, no switch proxy method implemented */
        return Integer.parseInt(mSerialPortConfig.getBaudrate());
    }

    public int getDatabit() {
        String databitConfig = mSerialPortConfig.getDatabit();
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
        String stopbitConfig = mSerialPortConfig.getStopbit();
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
        String parityConfig = mSerialPortConfig.getParity();
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
        int rtsctsInConfig = mSerialPortConfig.isRtsctsIn() ? 1 : 0;
        int rtsctsOutConfig = mSerialPortConfig.isRtsctsOut() ? 1 : 0;
        int xonxoffInConfig = mSerialPortConfig.isXonxoffIn() ? 1 : 0;
        int xonxoffOutConfig = mSerialPortConfig.isXonxoffOut() ? 1 : 0;
        int flowControl = SerialPort.FLOWCONTROL_NONE
                        + rtsctsInConfig * SerialPort.FLOWCONTROL_RTSCTS_IN
                        + rtsctsOutConfig * SerialPort.FLOWCONTROL_RTSCTS_OUT
                        + xonxoffInConfig * SerialPort.FLOWCONTROL_XONXOFF_IN
                        + xonxoffOutConfig * SerialPort.FLOWCONTROL_XONXOFF_OUT;
        return flowControl;
    }

}
