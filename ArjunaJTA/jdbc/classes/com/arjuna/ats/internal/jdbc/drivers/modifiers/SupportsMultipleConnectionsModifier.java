/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as 2016 by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2016,
 * @author JBoss Inc.
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
        // Non-modifier path does not call this
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
