package serial;

import config.ProjectConfig;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Serial port manager class is top class to manage all the serial port connection.
 * Currently only one serial port connection is supported.
 */
public class SerialPortManager {

    private static final int TIME_OUT = 1000; // Port open timeout

    public static final int ERROR_PORTINUSE = 1;
    public static final int ERROR_UNSUPPORTEDOPERATION = 2;
    public static final int ERROR_IOEXCEPTION = 3;
    public static final int ERROR_DEFAULT = -1;
    public static final String OWNER_NAME = ProjectConfig.APP_TITLE;

    /* Attribute */
    private SerialPort port = null;  // Currently only one serial port connection is supported.
    private String currentPortName = null;
    private SerialPortRX portRX;
    private SerialPortTX portTX;

    public SerialPortTX getPortTX() {
        return portTX;
    }

    public SerialPortRX getPortRX() {
        return portRX;
    }

    public String getCurrentPortName() {
        return currentPortName;
    }

    /** refresh portnames */
    public static Vector<String> getPortNames() {
        CommPortIdentifier portid = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        Vector<String> portNames = new Vector<>();
        while (portEnum.hasMoreElements()) {
            portid = (CommPortIdentifier)portEnum.nextElement();
            System.out.println("Name: " + portid.getName()
                    + ", CurrentOwner: " + portid.getCurrentOwner()
                    + ", PortType: " + portid.getPortType()
                    + ", Class: " + portid.getClass()
            );
            portNames.add(portid.getName());
        }
        return portNames;
    }

    public int Initialize(String portName) {
        int ret = ERROR_DEFAULT;
        SerialPortProxy spProxy = new SerialPortProxy();
        try {
            CommPortIdentifier portid = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            //while (portid == null && portEnum.hasMoreElements()) {
            while (portEnum.hasMoreElements()) {
                portid = (CommPortIdentifier)portEnum.nextElement();
                if ( portid == null )
                    continue;
                if (portid.getName().equals(portName)) {  // || portid.getName().contains(portName)  // Contain
                    port = (SerialPort) portid.open(OWNER_NAME, TIME_OUT);
                    currentPortName = portName; // if successfully open, update currentPortName.

                        /* setup connection parameters */
                    //port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    port.setSerialPortParams(spProxy.getBaudRate(), spProxy.getDatabit(),
                            spProxy.getStopbit(), spProxy.getParity());
                    port.setFlowControlMode(spProxy.getFlowControl()); // FLOW-CONTROL
                    //input = new BufferedReader(new InputStreamReader( port.getInputStream() ));

                    System.out.println( "Connected on port: " + portid.getName() );

                        /* setup serial port receiver/transmitter */
                    portRX = new SerialPortRX(port.getInputStream());
                    portRX.start();  // Start receiving thread

                    portTX = new SerialPortTX(port.getOutputStream());

                    ret = 0;
                }
            }
        } catch (PortInUseException e) {
            e.printStackTrace();
            ret = ERROR_PORTINUSE;
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
            ret = ERROR_UNSUPPORTEDOPERATION;
        } catch (IOException e) {
            e.printStackTrace();
            ret = ERROR_IOEXCEPTION;
        }
        return ret;
    }

    public synchronized void Finalize() {
        if (portRX != null && portRX.isAlive()) {
            portRX.close();
            portRX = null;
        }
        if (portTX != null) {
            portTX.Finalize();
            portTX = null;
        }
        if (port != null) {
            port.close();
            port = null;
        }
    }
}
