/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleXAResource2 implements XAResource {

    private List<Xid> xids = new ArrayList<Xid>();
    
	private static boolean rollbackCalled;
	private static boolean commitCalled;
	private static boolean errorOnNextRollback;

	public SimpleXAResource2() {
	}

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
        xids.remove(xid);
		commitCalled = true;
	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
	}

	@Override
	public void forget(Xid xid) throws XAException {
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		return false;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
        xids.add(xid);
		return 0;
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
        return xids.toArray(new Xid[]{});
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		if (errorOnNextRollback) {
			errorOnNextRollback = false;
			throw new Error();
		}
        xids.remove(xid);
		rollbackCalled = true;
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		return false;
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {
	}

	public boolean commitCalled() {
		return commitCalled;
	}

	public boolean rollbackCalled() {
		return rollbackCalled;
	}

	public static void injectRollbackError() {
		errorOnNextRollback = true;
	}

}