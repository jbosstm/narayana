package org.jboss.narayana.blacktie.jatmibroker.xatmi;

/**
 * This is the exception that is raised when the connection to Blacktie is suffering.
 */
public class ConnectionException extends Exception {
    /**
     * None-default serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The error wrapped in the exception.
     */
    private int tperrno;

    /**
     * Create a new exception giving it the error code.
     * 
     * @param tperrno The error code
     */
    public ConnectionException(int tperrno, String string, Throwable t) {
        super(string + ": " + tperrno, t);
        this.tperrno = tperrno;
    }

    /**
     * Create an exception without a root cause
     * 
     * @param tperrno The error number
     * @param string The message
     */
    public ConnectionException(int tperrno, String string) {
        super("tperrno: " + tperrno + " reason: " + string);
        this.tperrno = tperrno;
    }

    /**
     * Get the error code
     * 
     * @return The error code
     */
    public int getTperrno() {
        return tperrno;
    }

}
