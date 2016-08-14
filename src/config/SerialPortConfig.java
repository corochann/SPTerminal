package config;

/**
 * Serial port configuration
 */
public class SerialPortConfig {

    /* Baud rate */
    public static final String KEY_BAUDRATE = "baud_rate";
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
    public static final String KEY_DATABIT = "data_bit";
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
    public static final String KEY_PARITY = "parity";
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
    public static final String KEY_STOPBIT = "stop_bit";
    public static final String STOPBIT_1 = "1 bit";
    public static final String STOPBIT_1_5 = "1.5 bit";
    public static final String STOPBIT_2 = "2 bit";

    public static final String[] STOPBIT_LIST = {
            STOPBIT_1,
            STOPBIT_1_5,
            STOPBIT_2,
    };

    /* FlowControl */
    public static final String KEY_FLOWCONTROL_RTSCTS_IN = "flowcontrol_rtscts_in";
    public static final String KEY_FLOWCONTROL_RTSCTS_OUT = "flowcontrol_rtscts_out";
    public static final String KEY_FLOWCONTROL_XONXOFF_IN = "flowcontrol_xonxoff_in";
    public static final String KEY_FLOWCONTROL_XONXOFF_OUT = "flowcontrol_xonxoff_out";

    /* Last connected portName */
    public static final String KEY_LAST_CONNECTED_PORTNAME = "last_connected_portname";


}
