/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.recovery;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SimpleResourceXA_RETRY implements XAResource {
	private Xid xid;
	private boolean firstAttemptToCommit = true;
	private boolean committed = false;
	private CrashRecoveryCommitReturnsXA_RETRY toWakeUp;

	public SimpleResourceXA_RETRY(CrashRecoveryCommitReturnsXA_RETRY toWakeUp) {
		this.toWakeUp = toWakeUp;
	}

	public void commit(Xid xid, boolean onePhase) throws XAException {
		System.out.println("SimpleResourceXA_RETRY commit called: " + xid);
		if (firstAttemptToCommit) {
			firstAttemptToCommit = false;
			System.out.println("Returning XA_RETRY first time");
			throw new XAException(XAException.XA_RETRY);
		}
		xid = null;
		committed = true;
		synchronized (toWakeUp) {
			toWakeUp.committed();
		}
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
