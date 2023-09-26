/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.xa;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class XAResourceTest {
	@Before
	public void setup() {
//		recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryActivatorClassNames(Arrays.asList(new String[] {com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement.class.getName()}));
	}

	@After
	public void tearDown() {
//		recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryActivatorClassNames(null);
	}

	@Test
	public void testTwoResourcesReturnXA_RDONLY() throws Exception {

		ORB myORB = ORB.getInstance("test");
		RootOA myOA = OA.getRootOA(myORB);
		myORB.initORB(new String[] {}, null);
		myOA.initOA();
		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);

		RecoveryManager.manager();

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		assertTrue(theTransaction.enlistResource(new XA_READONLYXAResource()));
		assertTrue(theTransaction.enlistResource(new XA_READONLYXAResource()));
		assertTrue(theTransaction.enlistResource(new XA_READONLYXAResource()));

		tm.commit();

		RecoveryManager.manager().terminate();

		myOA.destroy();
		myORB.shutdown();
	}

	private class XA_READONLYXAResource implements XAResource {

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			System.out.println("commit");
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
			return XAResource.XA_RDONLY;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
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