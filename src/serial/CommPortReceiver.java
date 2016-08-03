/* Referred from http://www.kuligowski.pl/java/rs232-in-java-for-windows,1 */
package serial;

import java.io.IOException;
import java.io.InputStream;


public class CommPortReceiver extends Thread {

    InputStream in;
    Protocol protocol = new ProtocolImpl();

    public CommPortReceiver(InputStream in) {
        this.in = in;
    }

    public void run() {
        try {
            int b;
            while(true) {

                // if stream is not bound in.read() method returns -1  
                while((b = in.read()) != -1) {
                    protocol.onReceive((byte) b);
                }
                protocol.onStreamClosed();

                // wait 10ms when stream is broken and check again  
                sleep(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}