/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.xa.XAModifier;

/*
 * This is a stateless class to allow us to get round
 * problems in Oracle. For example, they can't work with
 * an arbitrary implementation of Xid - it has to be their
 * own implementation!
 */

public class IsSameRMModifier implements XAModifier, ConnectionModifier {

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
		return true;
	}
}