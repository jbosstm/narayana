/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.transaction.xa.XAResource;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

@RunWith(BMUnitRunner.class)
@BMScript("recovery")
public class CrashRecoveryCommitReturnsXA_RETRY {
	private volatile boolean committed;

	@Test
	public void test() throws Exception {
		// this test is supposed to leave a record around in the log store
		// during a commit long enough
		// that the periodic recovery thread runs and detects it. rather than
		// rely on delays to make
		// this happen (placing us at the mercy of the scheduler) we use a
		// byteman script to enforce
		// the thread sequence we need

		RecoveryEnvironmentBean recoveryEnvironmentBean = BeanPopulator
				.getDefaultInstance(RecoveryEnvironmentBean.class);
		recoveryEnvironmentBean.setRecoveryBackoffPeriod(1);
		recoveryEnvironmentBean.setPeriodicRecoveryPeriod(1);

		List<String> recoveryModuleClassNames = new ArrayList<String>();

		recoveryModuleClassNames
				.add("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule");
		recoveryModuleClassNames
				.add("com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");
		recoveryEnvironmentBean
				.setRecoveryModuleClassNames(recoveryModuleClassNames);
		List<String> expiryScannerClassNames = new ArrayList<String>();
		expiryScannerClassNames
				.add("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
		recoveryEnvironmentBean
				.setExpiryScannerClassNames(expiryScannerClassNames);
		recoveryEnvironmentBean.setRecoveryActivators(null);
		// start the recovery manager

		RecoveryManager.manager().initialize();

		XARecoveryModule xaRecoveryModule = null;
		for (RecoveryModule recoveryModule : ((Vector<RecoveryModule>) RecoveryManager
				.manager().getModules())) {
			if (recoveryModule instanceof XARecoveryModule) {
				xaRecoveryModule = (XARecoveryModule) recoveryModule;
				break;
			}
		}

		if (xaRecoveryModule == null) {
			throw new Exception("No XARM");
		}

		XAResource firstResource = new SimpleResource();
		final SimpleResourceXA_RETRY secondResource = new SimpleResourceXA_RETRY(
				this);

		xaRecoveryModule
				.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {

					@Override
					public boolean initialise(String p) throws Exception {
						// TODO Auto-generated method stub
						return true;
					}

					@Override
					public XAResource[] getXAResources() throws Exception {
						// TODO Auto-generated method stub
						return new XAResource[] { secondResource };
					}
				});

		// ok, now drive a TX to completion. the script should ensure that the
		// recovery

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		theTransaction.enlistResource(firstResource);
		theTransaction.enlistResource(secondResource);

		assertFalse(secondResource.wasCommitted());

		tm.commit();

		synchronized (this) {
			while (!committed) {
				wait();
			}
		}
		assertTrue(secondResource.wasCommitted());
	}

	public synchronized void committed() {
		committed = true;
		notify();
	}
}