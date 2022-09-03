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

package com.hp.mwtests.ts.jta.jts.xa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.RollbackException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class XAResourceCheckForSingleEndCall {
	@Test
	public void test() throws Exception {

		ORB myORB = ORB.getInstance("test");
		RootOA myOA = OA.getRootOA(myORB);
		myORB.initORB(new String[] {}, null);
		myOA.initOA();

		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);
		jtaPropertyManager
				.getJTAEnvironmentBean()
				.setTransactionManagerClassName(
						com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class
								.getName());
		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		XARMFAILXAResource xarmfailxaResource = new XARMFAILXAResource(true);
		XARMFAILXAResource xarmfailxaResource2 = new XARMFAILXAResource(false);
		assertTrue(theTransaction.enlistResource(xarmfailxaResource));
		assertTrue(theTransaction.enlistResource(xarmfailxaResource2));

		try {
			tm.commit();
			fail("Should have failed");
		} catch (RollbackException e) {
			// Expected
		}

		assertTrue(xarmfailxaResource.rollbackCalled = true);
		assertTrue(xarmfailxaResource2.rollbackCalled = true);

		assertTrue(xarmfailxaResource.endCount == 1);
		assertTrue(xarmfailxaResource2.endCount == 1);

		myOA.destroy();
		myORB.shutdown();
	}

	private class XARMFAILXAResource implements XAResource {

		private boolean error;

		private boolean rollbackCalled;

		private int endCount;

		public XARMFAILXAResource(boolean error) {
			this.error = error;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			endCount++;
			if (error && endCount > 1) {
				throw new XAException();
			}
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
			if (error) {
				throw new XAException(XAException.XAER_RMFAIL);
			}
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			rollbackCalled = true;
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
