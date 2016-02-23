package org.jboss.narayana.blacktie.jatmibroker.xatmi.impl;

import java.io.Serializable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Session;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;

/**
 * This structure contains the data that the client presented for processing to the service during its invocation from either
 * tpcall, tpacall or tpconnect.
 */
public class TPSVCINFO_Impl implements Serializable, TPSVCINFO {
    /**
     * The logger to use.
     */
    private static final Logger log = LogManager.getLogger(TPSVCINFO_Impl.class);

    /**
     * A non default id
     */
    private static final long serialVersionUID = 1L;

    /**
     * The service name
     */
    private String name;

    /**
     * The service data
     */
    private Buffer buffer;

    /**
     * The flags the service was called with
     */
    private int flags;

    /**
     * The connection descriptor
     */
    private Session session;

    /**
     * The connection for the service to use.
     */
    private Connection connection;

    /**
     * The length of the buffer provided.
     */
    private int len;

    /**
     * Create a new tpsvcinfo wrapper class
     * 
     * @param name The name of the service
     * @param buffer The data sent by the client
     * @param flags The flags that the client issued
     * @param session The connection descriptor used
     * @param connection The connection to use
     * @param len The length of the said data
     */
    public TPSVCINFO_Impl(String name, Buffer buffer, int flags, Session session, Connection connection, int len) {
        this.name = name;
        this.buffer = buffer;
        this.flags = flags;
        this.session = session;
        this.connection = connection;
        this.len = len;
    }

    /**
     * Get the name of the service the client thought it invoked
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the data
     * 
     * @return The data
     */
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * Get the length of the buffer that was sent
     * 
     * @return The length of the buffer
     */
    public int getLen() {
        return len;
    }

    /**
     * Get the flags that were issued
     * 
     * @return The flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Get the connection descriptor
     * 
     * @return The connection descriptor
     * @throws ConnectionException
     */
    public Session getSession() throws ConnectionException {
        if (session == null) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO, "Not a TPCONV session");
        }
        return session;
    }

    /**
     * Get a reference to the connection that the service holds.
     * 
     * @return The connection
     */
    public Connection getConnection() {
        return connection;
    }

}
