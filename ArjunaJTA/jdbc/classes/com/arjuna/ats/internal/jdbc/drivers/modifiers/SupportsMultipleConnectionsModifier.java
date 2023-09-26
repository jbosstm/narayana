/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.xa.XAModifier;

import javax.sql.XAConnection;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.SQLException;

public class SupportsMultipleConnectionsModifier implements XAModifier, ConnectionModifier {

    @Override
    public String initialise(String dbName) {
        return dbName;
    }

    @Override
    public Xid createXid(Xid xid) throws SQLException, NotImplementedException {
        return xid;
    }

    @Override
    public XAConnection getConnection(XAConnection conn) throws SQLException,
            NotImplementedException {
        throw new NotImplementedException(); // NEVER CALLED
    }

    @Override
    public boolean supportsMultipleConnections() throws SQLException,
            NotImplementedException {
        return true; // This ensures connection close is delayed
    }

    @Override
    public void setIsolationLevel(Connection conn, int level)
            throws SQLException, NotImplementedException {
        conn.setTransactionIsolation(level);
    }

    @Override
    public int xaStartParameters(int level) throws SQLException,
            NotImplementedException {
        return level;
    }

    @Override
    public boolean requiresSameRMOverride() {
        return false;
    }
}