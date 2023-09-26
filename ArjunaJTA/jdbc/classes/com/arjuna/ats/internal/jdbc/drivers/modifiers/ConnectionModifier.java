/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.xa.XAModifier;

/**
 * Instances of this class enable us to work around problems
 * in certain databases (specifically Oracle).
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ConnectionModifier.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public interface ConnectionModifier extends XAModifier
{

    /**
     * Initialise the modifier.
     *
     * @return the database name to use.
     * @since JTS 2.2.
     */

    public String initialise (String dbName);
 
    /**
     * Return a new connection.
     * @deprecated This is no longer used by the transaction manager
     */

    public XAConnection getConnection (XAConnection conn) throws SQLException, NotImplementedException;

    /**
     * Does their JDBC driver support multiple connections in a single transaction?
     */

    public boolean supportsMultipleConnections () throws SQLException, NotImplementedException;

    /*
     * Set the isolation level on the connection.
     */

    public void setIsolationLevel (Connection conn, int level) throws SQLException, NotImplementedException;
    
    /**
     * This method indicates whether the driver supports TMJOIN reliably. If isSameRM returns true but does
     * not support xares1.start(xid1,TMNOFLAGS); xares2.start(xid2, TMJOIN) then you need to make sure you 
     * set this to true to use a wrapped XAR that returns false for the driver.
     * 
     * See JBTM-2264 for more details.
     */
    public boolean requiresSameRMOverride();
    
}