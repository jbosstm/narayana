/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.xa;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * XA connection implementations must provide an implementation
 * of this class.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: RecoverableXAConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

// check

public interface RecoverableXAConnection
{

    public static final int AUTO_RECOVERY = 0;
    public static final int OBJECT_RECOVERY = 1;

    public boolean packInto (OutputObjectState os);
    public boolean unpackFrom (InputObjectState os);

    public XAResource getResource () throws SQLException;
    public XAConnection getConnection () throws SQLException;
    public XAConnection getCurrentConnection () throws SQLException;
    public XADataSource getDataSource () throws SQLException;
    void closeCloseCurrentConnection() throws SQLException;

    /**
     * @return true if the connection is being used within a transaction,
     * false otherwise.
     */

    public boolean inuse ();

    /**
     * @since JTS 2.2.
     */
	   
    public void reset ();  // reset the connection for new useage

    /**
     * @since JTS 2.2.
     */

    public void close ();  // close the connection and make available for gc
 
    /**
     * Remember the transaction we are being used within.
     *
     * @since JTS 2.2.
     */

    public boolean setTransaction (jakarta.transaction.Transaction tx);

    /**
     * Is this the same transaction?
     *
     * @return true if the connection can be used by this
     * transaction, false otherwise.
     * @since JTS 2.2.
     */

    public boolean validTransaction (jakarta.transaction.Transaction tx);
 
}