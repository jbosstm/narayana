package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;

public class XARecoveryModuleUnitTest2 {

	@Test
	public void testDelayedPhase2() throws Exception {
		XAResource dummy = new XAResource() {

			@Override
			public void commit(Xid xid, boolean onePhase) throws XAException {
				// TODO Auto-generated method stub

			}

			@Override
			public void end(Xid xid, int flags) throws XAException {
				// TODO Auto-generated method stub

			}

			@Override
			public void forget(Xid xid) throws XAException {
				// TODO Auto-generated method stub

			}

			@Override
			public int getTransactionTimeout() throws XAException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean isSameRM(XAResource xares) throws XAException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public int prepare(Xid xid) throws XAException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Xid[] recover(int flag) throws XAException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void rollback(Xid xid) throws XAException {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean setTransactionTimeout(int seconds)
					throws XAException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void start(Xid xid, int flags) throws XAException {
				// TODO Auto-generated method stub

			}

		};
		// Ensure that we can recover the test xar
		ArrayList<String> r = new ArrayList<String>();
		r.add(XARXARRMSyncerXARecovery.class.getName());
		jtaPropertyManager.getJTAEnvironmentBean()
				.setXaResourceRecoveryClassNames(r);

		// Ensure we can detect orphans
		ArrayList<String> filters = new ArrayList<String>();
		filters.add(JTATransactionLogXAResourceOrphanFilter.class.getName());
		filters.add(JTANodeNameXAResourceOrphanFilter.class.getName());
		jtaPropertyManager.getJTAEnvironmentBean()
				.setXaResourceOrphanFilterClassNames(filters);

		// Ensure we will handle orphans
		jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(
				Arrays.asList(new String[] { "*" }));

		// Create the recovery module
		XARecoveryModule xarm = new XARecoveryModule();

		// Begin a transaction
		com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple tx = new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple(
				0);

		// Enlist two resources so we get XA behaviour
		assertTrue(tx.enlistResource(dummy));
		// Make sure the syncing resource has a handle on the XARM
		assertTrue(tx.enlistResource(new XARXARMSyncer(xarm)));
		tx.commit();

		// Wait till the safety interval has expired
		Field safetyIntervalMillis = RecoveryXids.class
				.getDeclaredField("safetyIntervalMillis");
		safetyIntervalMillis.setAccessible(true);
		int safetyInterval = (Integer) safetyIntervalMillis.get(null);
		Thread.sleep(safetyInterval);
		// Trigger phase two recovery and make sure the orphan is detected
		xarm.periodicWorkSecondPass();
		assertTrue(XARXARMSyncer.isOrphanDetected());
	}
}
