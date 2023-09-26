/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.ConnectableResource;
import org.jboss.tm.XAResourceWrapper;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;

public class JDBCConnectableResource implements ConnectableResource,
		LastResourceCommitOptimisation, XAResourceWrapper {

	private Connection connection;
	private Xid startedXid;

	public JDBCConnectableResource(Connection connection) throws SQLException {
		this.connection = connection;
	}

	@Override
	public Object getConnection() throws Throwable {
		return connection;
	}

	/**
	 * Test code - ignore
	 */
	@Override
	public void start(Xid arg0, int arg1) throws XAException {
		startedXid = arg0;
	}

	/**
	 * Test code - ignore
	 */
	Xid getStartedXid() {
		return startedXid;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		// Should not be called
		throw new XAException(XAException.XAER_PROTO);
	}

	@Override
	public void commit(Xid arg0, boolean arg1) throws XAException {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new XAException("Could not commit: " + e.getMessage());
		}
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new XAException("Could not commit: " + e.getMessage());
		}
	}

	@Override
	public void end(Xid arg0, int arg1) throws XAException {
		// Should not be called
		throw new XAException(XAException.XAER_PROTO);
	}

	@Override
	public void forget(Xid arg0) throws XAException {
		// Should not be called
		throw new XAException(XAException.XAER_PROTO);
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		// Should not be called
		throw new XAException(XAException.XAER_PROTO);
	}

	@Override
	public Xid[] recover(int arg0) throws XAException {
		// Should not be called
		throw new XAException(XAException.XAER_PROTO);
	}

	@Override
	public boolean setTransactionTimeout(int arg0) throws XAException {
		return false;
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		return arg0 == this;
	}

	@Override
	public XAResource getResource() {
		return null;
	}

	@Override
	public String getProductName() {
		return null;
	}

	@Override
	public String getProductVersion() {
		return null;
	}

	@Override
	public String getJndiName() {
		return "commitmarkableresource";
	}
}