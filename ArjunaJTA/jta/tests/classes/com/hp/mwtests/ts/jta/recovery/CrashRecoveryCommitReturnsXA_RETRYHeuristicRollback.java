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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CrashRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.transaction.xa.XAResource;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

@RunWith(BMUnitRunner.class)
@BMScript("recovery")
public class CrashRecoveryCommitReturnsXA_RETRYHeuristicRollback {
	@Test
	public void testHeuristicRollback() throws Exception {
		// this test is supposed to leave a record around in the log store
		// during a commit long enough
		// that the periodic recovery thread runs and detects it. rather than
		// rely on delays to make
		// this happen (placing us at the mercy of the scheduler) we use a
		// byteman script to enforce
		// the thread sequence we need

		RecoveryEnvironmentBean recoveryEnvironmentBean = BeanPopulator
				.getDefaultInstance(RecoveryEnvironmentBean.class);
		// JBTM-1354 we need to make sure that a full scan has gone off
		recoveryEnvironmentBean.setRecoveryBackoffPeriod(1);
		recoveryEnvironmentBean.setPeriodicRecoveryPeriod(Integer.MAX_VALUE);

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

		// JBTM-1354 Run a scan to make sure that the recovery thread has completed a full run before starting the test
		// The important thing is that replayCompletion is allowed to do a scan of the transactions 
		RecoveryManager.manager().scan();

		XAResource firstResource = new SimpleResource();
		Object toWakeUp = new Object();
		final SimpleResourceXA_RETRYHeuristicRollback secondResource = new SimpleResourceXA_RETRYHeuristicRollback();

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
		Uid txUid = ((TransactionImple) theTransaction).get_uid();

		theTransaction.enlistResource(firstResource);
		theTransaction.enlistResource(secondResource);

		assertFalse(secondResource.wasCommitted());

		tm.commit();

		InputObjectState uids = new InputObjectState();
		String type = new AtomicAction().type();
		StoreManager.getRecoveryStore().allObjUids(type, uids);
		boolean moreUids = true;

		boolean found = false;
		while (moreUids) {
			Uid theUid = UidHelper.unpackFrom(uids);
			if (theUid.equals(txUid)) {
				found = true;
				Field heuristicListField = BasicAction.class
						.getDeclaredField("heuristicList");
				heuristicListField.setAccessible(true);
				ActionStatusService ass = new ActionStatusService();

				{
					int theStatus = ass.getTransactionStatus(type,
							theUid.stringForm());
					assertTrue(theStatus == ActionStatus.COMMITTED);
					RecoverAtomicAction rcvAtomicAction = new RecoverAtomicAction(
							theUid, theStatus);
					theStatus = rcvAtomicAction.status();
					rcvAtomicAction.replayPhase2();
					assertTrue(theStatus == ActionStatus.COMMITTED);
					assertTrue(secondResource.wasCommitted());
					RecordList heuristicList = (RecordList) heuristicListField
							.get(rcvAtomicAction);
					assertTrue(
							"Expected 1 heuristics: " + heuristicList.size(),
							heuristicList.size() == 1);
				}
				{
					int theStatus = ass.getTransactionStatus(type,
							theUid.stringForm());
					assertTrue(theStatus == ActionStatus.COMMITTED);
					RecoverAtomicAction rcvAtomicAction = new RecoverAtomicAction(
							theUid, theStatus);
					theStatus = rcvAtomicAction.status();
					assertTrue(theStatus == ActionStatus.COMMITTED);
					RecordList heuristicList = (RecordList) heuristicListField
							.get(rcvAtomicAction);
					assertTrue(
							"Expected 1 heuristics: " + heuristicList.size(),
							heuristicList.size() == 1);
					assertTrue(secondResource.wasCommitted());
				}
			} else if (theUid.equals(Uid.nullUid())) {
				moreUids = false;
			}
		}

		if (!found) {
			throw new Exception("Could not locate the Uid");
		}
	}
}
