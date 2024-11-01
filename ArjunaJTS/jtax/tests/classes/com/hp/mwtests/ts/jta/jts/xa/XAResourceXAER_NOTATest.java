/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.xa;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import jakarta.transaction.RollbackException;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.ORBPackage.InvalidName;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(BMUnitRunner.class)
public class XAResourceXAER_NOTATest {
	private ORB myORB;
	private RootOA myOA;
	private boolean commitOnePhase;

	@Before
	public void setup() throws InvalidName {
		commitOnePhase = arjPropertyManager.getCoordinatorEnvironmentBean().isCommitOnePhase();
		arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
		myORB = ORB.getInstance("test");
		myOA = OA.getRootOA(myORB);
		myORB.initORB(new String[] {}, null);
		myOA.initOA();
		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);

		RecoveryManager.manager();
	}

	@After
	public void tearDown() {

		RecoveryManager.manager().terminate();

		myOA.destroy();
		myORB.shutdown();
		arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(commitOnePhase);
	}

	@Test
	@BMRule(name = "Fail if logging statement doesn't execute", targetClass = "com.arjuna.ats.jts.logging.jtsI18NLogger_$logger", targetMethod = "warn_resources_errgenerr", targetLocation = "AT ENTRY", action = "System.setProperty(\"Called\", \"true\")")

	public void testPrepareFails() throws Exception {

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();
		assertTrue(theTransaction.enlistResource(new XAResource() {

			@Override
			public int prepare(Xid xid) throws XAException {
				throw new XAException(XAException.XAER_RMFAIL);
			}

			@Override
			public void rollback(Xid xid) throws XAException {
				throw new XAException(XAException.XAER_NOTA);
			}

			@Override
			public void commit(Xid xid, boolean onePhase) throws XAException {

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
			public Xid[] recover(int flag) throws XAException {
				return new Xid[0];
			}

			@Override
			public boolean setTransactionTimeout(int seconds) throws XAException {
				return false;
			}

			@Override
			public void start(Xid xid, int flags) throws XAException {

			}
		}));

		try {
			tm.commit();
			fail("Should not have been able to rollback");
		} catch (RollbackException rbe) {
			// This is expected
		}
		assertEquals(System.getProperty("Called"), "true");
	}
}