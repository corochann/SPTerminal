package serialterminal;

import purejavacomm.CommPortIdentifier;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;

/**
 *
 */
public class SerialTerminal2 implements SerialPortEventListener {
    SerialPort port = null;

    private String appName = getClass().getName();
    private BufferedReader input = null;

    private static final int TIME_OUT = 1000; // Port open timeout

    private static final String PORT_NAMES[] = {  // PORTS
//        "tty.usbmodem", // Mac OS X
//        "usbdev", // Linux
//        "tty", // Linux
//        "serial", // Linux
          "COM12", // Windows
    };

    public static void main(String[] args) {
        //ArduinoJavaComms lightSensor = new ArduinoJavaComms();
        //lightSensor.initialize();
        new SerialTerminal2().initialize();
    }

    public void initialize() {
        try {
            CommPortIdentifier portid = null;
            Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

            //while (portid == null && portEnum.hasMoreElements()) {
            while (portEnum.hasMoreElements()) {
                portid = (CommPortIdentifier)portEnum.nextElement();
                if ( portid == null )
                    continue;

                System.out.println("Trying: " + portid.getName());
                for ( String portName: PORT_NAMES ) {
                    if ( portid.getName().equals(portName)
                            || portid.getName().contains(portName)) {  // CONTAINS
                        port = (SerialPort) portid.open("SerialTerminal2", TIME_OUT);

                        /* setup connection parameters */
                        port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                        port.setFlowControlMode(
                                SerialPort.FLOWCONTROL_XONXOFF_IN+
                                        SerialPort.FLOWCONTROL_XONXOFF_OUT); // FLOW-CONTROL
                        input = new BufferedReader(
                                new InputStreamReader( port.getInputStream() ));

                        System.out.println( "Connected on port: " + portid.getName() );

                        // setup serial port writer
                        CommPortSender.setWriterStream(port.getOutputStream());

                        // setup serial port reader
                        new CommPortReceiver(port.getInputStream()).start();
                        //port.addEventListener(this);
                        //port.notifyOnDataAvailable(true);
                    }
                }
            }

            while ( true) {
                try { Thread.sleep(100); } catch (Exception ex) { }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            switch (event.getEventType() ) {
                case SerialPortEvent.DATA_AVAILABLE:
                    String inputLine = input.readLine();
                    System.out.println(inputLine);
                    break;

                default:
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
