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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.RollbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;

public class JTATestXARMERR {

	@Test
	public void test() throws Exception {

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();
		tm.setTransactionTimeout(0);
		tm.begin();
		javax.transaction.Transaction theTransaction = tm.getTransaction();
		TransactionImple txImple =((TransactionImple) theTransaction); 
		Uid get_uid = txImple.get_uid();
		XARMERRXAResource xar1 = new XARMERRXAResource(true);
		assertTrue(theTransaction.enlistResource(xar1));
		XARMERRXAResource xar2 = new XARMERRXAResource(false);
		assertTrue(theTransaction.enlistResource(xar2));

		try {
			tm.commit();
			fail("Expected RollbackException");
		} catch (RollbackException e) {
			// Expected
			assertTrue(xar2.getRollbackCalled());
			assertTrue(xar1.getForgetCalled());
			assertTrue(StoreManager.getRecoveryStore().currentState(get_uid, new AtomicAction().type()) == StateStatus.OS_UNKNOWN);
		}
	}

	private class XARMERRXAResource implements XAResource {

		private boolean returnRMERR;
		private boolean rollbackCalled;
		private boolean forgetCalled;

		public XARMERRXAResource(boolean returnRMERR) {
			this.returnRMERR = returnRMERR;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			if (returnRMERR) {
				throw new XAException(XAException.XAER_RMERR);
			}
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			rollbackCalled = true;
		}

		public boolean getRollbackCalled() {
			return rollbackCalled;
		}

		@Override
		public void forget(Xid xid) throws XAException {
			forgetCalled = true;
		}

		public boolean getForgetCalled() {
			return forgetCalled;
		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
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
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			return null;
		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			return false;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
		}
	}
}
