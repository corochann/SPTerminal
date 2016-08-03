/* Referred from http://www.kuligowski.pl/java/rs232-in-java-for-windows,1 */
package serial;

public interface Protocol {

    // protocol manager handles each received byte
    void onReceive(byte b);

    // protocol manager handles broken stream
    void onStreamClosed();
}