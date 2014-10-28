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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionSynchronizationRegistry;
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
		final javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.getStatus();

		tm.begin();

		javax.transaction.Transaction theTransaction = tm.getTransaction();

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
					assertTrue(tm.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION);
					Transaction suspend2 = tm.suspend();
					assertTrue(suspend2 == null);
					tm.begin();
					assertTrue(tm.getStatus() == javax.transaction.Status.STATUS_ACTIVE);
					tm.commit();
					assertTrue(tm.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION);
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
