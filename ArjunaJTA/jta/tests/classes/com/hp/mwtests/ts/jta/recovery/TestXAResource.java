/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class TestXAResource implements XAResource {
	private Xid xid;
	private int commitCount;
	private int rollbackCount;
	private int recoveryCount;

	public TestXAResource(Xid xid) {
		this.xid = xid;
	}

	public TestXAResource() {
	}

	public Xid[] recover(int flag) throws XAException {
		recoveryCount++;
		return new Xid[] { xid };
	}

	public boolean isSameRM(XAResource xares) throws XAException {
		return false;
	}

	public int prepare(Xid xid) throws XAException {
		this.xid = xid;
		return XA_OK;
	}

	public void commit(Xid id, boolean onePhase) throws XAException {
		System.out.println("committed");
		xid = null;
		commitCount++;
	}

	public void rollback(Xid xid) throws XAException {
		System.out.println("rolled back");
		xid = null;
		rollbackCount++;
	}

	public void forget(Xid xid) throws XAException {
	}

	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

	public void end(Xid xid, int flags) throws XAException {
	}

	public Xid getXid() {
		return xid;
	}

	public int commitCount() {
		return commitCount;
	}

	public int rollbackCount() {
		return rollbackCount;
	}

	public int recoveryCount() {
		return recoveryCount;
	}

}