/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleResourceXA_RETRYHeuristicRollback implements XAResource {
	private Xid xid;
	private boolean firstAttemptToCommit = true;
	private boolean committed = false;

	public void commit(Xid xid, boolean onePhase) throws XAException {
		System.out.println("SimpleResourceXA_RETRY commit called: " + xid);
		if (firstAttemptToCommit) {
			firstAttemptToCommit = false;
			System.out.println("Returning XA_RETRY first time");
			throw new XAException(XAException.XA_RETRY);
		}
		xid = null;
		committed = true;
		throw new XAException(XAException.XA_HEURRB);
	}

	public void end(Xid xid, int flags) throws XAException {
	}

	public void forget(Xid xid) throws XAException {
	}

	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	public boolean isSameRM(XAResource xares) throws XAException {
		return false;
	}

	public int prepare(Xid xid) throws XAException {
		System.out.println("SimpleResourceXA_RETRY prepare called: " + xid);
		this.xid = xid;
		return XA_OK;
	}

	public Xid[] recover(int flag) throws XAException {
		if (xid != null) {
			return new Xid[] { xid };
		}
		return null;
	}

	public void rollback(Xid xid) throws XAException {
		System.out.println("SimpleResourceXA_RETRY ROLLBACK called: " + xid);
		throw new XAException("SimpleResourceXA_RETRY WASN't EXPECTING THAT!");
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

	public boolean wasCommitted() {
		// TODO Auto-generated method stub
		return committed;
	}

}