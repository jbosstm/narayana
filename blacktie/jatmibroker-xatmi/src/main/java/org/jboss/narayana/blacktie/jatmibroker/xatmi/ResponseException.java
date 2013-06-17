package org.jboss.narayana.blacktie.jatmibroker.xatmi;

/**
 * This is the exception that is raised if a response condition needs to be notified to the client.
 */
public class ResponseException extends ConnectionException {
    /**
     * None-default serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Any event that is being raised.
     */
    private long event;

    /**
     * The returned code.
     */
    private int rcode;

    /**
     * The buffer received.
     */
    private Buffer received;

    /**
     * An exception for reporting events
     * 
     * @param tperrno This will always be TPEEVENT
     * @param string The message
     * @param event The event may be any from Connection
     * @param received A received buffer
     * @param rcode The rcode in case of TPFAIL
     */
    public ResponseException(int tperrno, String string, long event, int rcode, Buffer received) {
        super(tperrno, string);
        this.event = event;
        this.rcode = rcode;
        this.received = received;
    }

    /**
     * Get the event
     * 
     * @return The event
     */
    public long getEvent() {
        return event;
    }

    /**
     * Get the rcode.
     * 
     * @return The rcode.
     */
    public int getRcode() {
        return rcode;
    }

    /**
     * Get a received buffer
     * 
     * @return The received buffer
     */
    public Buffer getReceived() {
        return received;
    }
}
