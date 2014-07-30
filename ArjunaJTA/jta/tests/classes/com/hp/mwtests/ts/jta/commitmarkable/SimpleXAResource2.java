/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2013
 * @author JBoss Inc.
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
