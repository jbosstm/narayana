/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.xa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;

public class JTSSynchronizationTest {
	protected boolean failed;

	@Test
	public void test() throws Exception {

		Map<String, String> map = new HashMap<String, String>();
		map.put("com.arjuna.orbportability.orb.PreInit1",
				"com.arjuna.ats.internal.jts.context.ContextPropagationManager");
		map.put("com.arjuna.orbportability.orb.PostInit",
				"com.arjuna.ats.jts.utils.ORBSetup");
		map.put("com.arjuna.orbportability.orb.PostInit2",
				"com.arjuna.ats.internal.jts.recovery.RecoveryInit");
		map.put("com.arjuna.orbportability.orb.PostSet1",
				"com.arjuna.ats.jts.utils.ORBSetup");
		opPropertyManager.getOrbPortabilityEnvironmentBean()
				.setOrbInitializationProperties(map);

		ORB myORB = ORB.getInstance("test");
		RootOA myOA = OA.getRootOA(myORB);
		final Properties initORBProperties = new Properties();
		initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
		initORBProperties.setProperty(
				"com.sun.CORBA.POA.ORBPersistentServerPort", ""
						+ jtsPropertyManager.getJTSEnvironmentBean()
								.getRecoveryManagerPort());
		myORB.initORB(new String[] {}, initORBProperties);
		myOA.initOA();
		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);

		recoveryPropertyManager
				.getRecoveryEnvironmentBean()
				.setRecoveryActivatorClassNames(
						Arrays.asList(new String[] { "com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement" }));
		final TransactionSynchronizationRegistry tsr = new com.arjuna.ats.internal.jta.transaction.jts.TransactionSynchronizationRegistryImple();

		new RecoveryManagerImple(false);
		final jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.getStatus();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
		tsr.registerInterposedSynchronization(new Synchronization() {
			@Override
			public void beforeCompletion() {
			}

			@Override
			public void afterCompletion(int arg0) {
				try {
					Transaction transaction = tm.getTransaction();
					Transaction suspend = tm.suspend();
					assertTrue(tm.getStatus() == jakarta.transaction.Status.STATUS_NO_TRANSACTION);
					Transaction suspend2 = tm.suspend();
					assertTrue(suspend2 == null);
					tm.begin();
					assertTrue(tm.getStatus() == jakarta.transaction.Status.STATUS_ACTIVE);
					tm.commit();
					assertTrue(tm.getStatus() == jakarta.transaction.Status.STATUS_NO_TRANSACTION);
					tm.resume(suspend);
					assertTrue(tm.getStatus() == arg0);
					Transaction transaction2 = tm.getTransaction();
					assertTrue(transaction == transaction2);
				} catch (SystemException | IllegalStateException
						| SecurityException | InvalidTransactionException
						| NotSupportedException | RollbackException
						| HeuristicMixedException | HeuristicRollbackException e) {
					failed = true;
					e.printStackTrace();
				}

			}
		});
		tm.commit();

		myOA.destroy();
		myORB.shutdown();

		if (failed) {
			fail("Issues");
		}
	}

	private class XARMERRXAResource implements XAResource {

		private boolean returnRMERROutOfEnd;

		public XARMERRXAResource(boolean returnRMERROutOfEnd) {
			this.returnRMERROutOfEnd = returnRMERROutOfEnd;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			if (returnRMERROutOfEnd) {
				throw new XAException(XAException.XAER_RMERR);
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
			return 0;
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