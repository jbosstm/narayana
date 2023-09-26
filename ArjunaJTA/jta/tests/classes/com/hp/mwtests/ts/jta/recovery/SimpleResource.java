/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleResource implements XAResource {

	public void commit(Xid xid, boolean onePhase) throws XAException {
		System.out.println("SimpleResource commit called: " + xid);
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
		System.out.println("SimpleResource prepare called: " + xid);
		return XA_OK;
	}

	public Xid[] recover(int flag) throws XAException {
		return null;
	}

	public void rollback(Xid xid) throws XAException {
		System.out.println("SimpleResource ROLLBACK called: " + xid);
		throw new XAException("WASN't EXPECTING THAT!");
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

}