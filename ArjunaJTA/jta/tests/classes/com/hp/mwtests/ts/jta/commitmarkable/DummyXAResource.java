/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.commitmarkable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource {

	@Override
	public void commit(Xid arg0, boolean arg1) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(Xid arg0, int arg1) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public void forget(Xid arg0) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTransactionTimeout() throws XAException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Xid[] recover(int arg0) throws XAException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setTransactionTimeout(int arg0) throws XAException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start(Xid arg0, int arg1) throws XAException {
		// TODO Auto-generated method stub

	}

}