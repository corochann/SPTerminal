package com.corochann.spterminal.config;

import com.corochann.spterminal.util.MyUtils;

import java.io.FileNotFoundException;
import java.io.Serializable;

/**
 * Serial port configuration, this is preference class.
 * preference class owns values in Attributes, which can be stored/loaded in XML format.
 */
public class SerialPortConfig implements Serializable {

    /*--- Attributes ---*/
    // right side value is considered as "default" value.
    private String baudrate = BAUDRATE_9600;
    private String databit = DATABIT_8;
    private String parity = PARITY_NONE;
    private String stopbit = STOPBIT_1;
    // flow control
    private boolean rtsctsIn = false;
    private boolean rtsctsOut = false;
    private boolean xonxoffIn = false;
    private boolean xonxoffOut = false;
    private String lastConnectedPortName = "";

    /*--- CONSTANTS ---*/
    /* Baud rate */
    public static final String BAUDRATE_110 = "110";
    public static final String BAUDRATE_300 = "300";
    public static final String BAUDRATE_600 = "600";
    public static final String BAUDRATE_1200 = "1200";
    public static final String BAUDRATE_2400 = "2400";
    public static final String BAUDRATE_4800 = "4800";
    public static final String BAUDRATE_9600 = "9600";
    public static final String BAUDRATE_14400 = "14400";
    public static final String BAUDRATE_19200 = "19200";
    public static final String BAUDRATE_38400 = "38400";
    public static final String BAUDRATE_57600 = "57600";
    public static final String BAUDRATE_115200 = "115200";
    public static final String BAUDRATE_230400 = "230400";
    public static final String BAUDRATE_460800 = "460800";
    public static final String BAUDRATE_921600 = "921600";

    public static final String[] BAUDRATE_LIST = {
            BAUDRATE_110,
            BAUDRATE_300,
            BAUDRATE_600,
            BAUDRATE_1200,
            BAUDRATE_2400,
            BAUDRATE_4800,
            BAUDRATE_9600,
            BAUDRATE_14400,
            BAUDRATE_19200,
            BAUDRATE_38400,
            BAUDRATE_57600,
            BAUDRATE_115200,
            BAUDRATE_230400,
            BAUDRATE_460800,
            BAUDRATE_921600,
    };

    /* Data bit */
    public static final String DATABIT_5 = "5 bit";
    public static final String DATABIT_6 = "6 bit";
    public static final String DATABIT_7 = "7 bit";
    public static final String DATABIT_8 = "8 bit";

    public static final String[] DATABIT_LIST = {
            DATABIT_5,
            DATABIT_6,
            DATABIT_7,
            DATABIT_8,
    };

    /* Parity */
    public static final String PARITY_NONE = "None";
    public static final String PARITY_ODD = "Odd";
    public static final String PARITY_EVEN = "Even";
    public static final String PARITY_MARK = "Mark";
    public static final String PARITY_SPACE = "Space";

    public static final String[] PARITY_LIST = {
            PARITY_NONE,
            PARITY_ODD,
            PARITY_EVEN,
            PARITY_MARK,
            PARITY_SPACE,
    };

    /* Stop bit */
    public static final String STOPBIT_1 = "1 bit";
    public static final String STOPBIT_1_5 = "1.5 bit";
    public static final String STOPBIT_2 = "2 bit";

    public static final String[] STOPBIT_LIST = {
            STOPBIT_1,
            STOPBIT_1_5,
            STOPBIT_2,
    };

    /* FlowControl */

    /* Last connected portName */

    /*--- Save & Load ---*/
    /**
     * save this preference
     */
    public synchronized void save() {
        System.out.println("Saving SerialPortConfig to " + ProjectConfig.SERIAL_PORT_CONFIG_XML_PATH);
        try {
            MyUtils.writeToXML(this, ProjectConfig.SERIAL_PORT_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * load this preference
     */
    public static synchronized SerialPortConfig load() {
        SerialPortConfig serialPortConfig;
        try {
            serialPortConfig = (SerialPortConfig)MyUtils.readFromXML(ProjectConfig.SERIAL_PORT_CONFIG_XML_PATH);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println(ProjectConfig.SERIAL_PORT_CONFIG_XML_PATH + " not found, default value will be used.");
            serialPortConfig = new SerialPortConfig();
        }
        return serialPortConfig;
    }

    /*--- Getter and Setter ---*/
    public String getBaudrate() {
        return baudrate;
    }

    public synchronized void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    public String getDatabit() {
        return databit;
    }

    public synchronized void setDatabit(String databit) {
        this.databit = databit;
    }

    public String getParity() {
        return parity;
    }

    public synchronized void setParity(String parity) {
        this.parity = parity;
    }

    public String getStopbit() {
        return stopbit;
    }

    public synchronized void setStopbit(String stopbit) {
        this.stopbit = stopbit;
    }

    public boolean isRtsctsIn() {
        return rtsctsIn;
    }

    public synchronized void setRtsctsIn(boolean rtsctsIn) {
        this.rtsctsIn = rtsctsIn;
    }

    public boolean isRtsctsOut() {
        return rtsctsOut;
    }

    public synchronized void setRtsctsOut(boolean rtsctsOut) {
        this.rtsctsOut = rtsctsOut;
    }

    public boolean isXonxoffIn() {
        return xonxoffIn;
    }

    public synchronized void setXonxoffIn(boolean xonxoffIn) {
        this.xonxoffIn = xonxoffIn;
    }

    public boolean isXonxoffOut() {
        return xonxoffOut;
    }

    public synchronized void setXonxoffOut(boolean xonxoffOut) {
        this.xonxoffOut = xonxoffOut;
    }

    public String getLastConnectedPortName() {
        return lastConnectedPortName;
    }

    public synchronized void setLastConnectedPortName(String lastConnectedPortName) {
        this.lastConnectedPortName = lastConnectedPortName;
    }
}
