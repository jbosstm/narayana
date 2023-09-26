/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;

@RunWith(BMUnitRunner.class)
public class CrashRecovery2 {

    @Before
    public void setUp() {
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager
                .getRecoveryEnvironmentBean();
        recoveryEnvironmentBean
                .setRecoveryModuleClassNames(Arrays
                        .asList(new String[] {
                                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                                "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule" }));

        JTAEnvironmentBean jtaEnvironmentBean = jtaPropertyManager
                .getJTAEnvironmentBean();
        jtaEnvironmentBean
                .setXaResourceRecoveryClassNames(Arrays
                        .asList(new String[] { "com.hp.mwtests.ts.jta.recovery.TestXAResourceRecovery" }));
        jtaEnvironmentBean
                .setXaResourceOrphanFilterClassNames(Arrays
                        .asList(new String[] {
                                "com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter",
                                "com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter" }));
        jtaEnvironmentBean.setXaRecoveryNodes(Arrays
                .asList(new String[] { "1" }));
    }
    

    @BMScript("fail2pc")
	@Test
	public void test() throws NotSupportedException, SystemException,
			IllegalStateException, RollbackException, SecurityException,
			HeuristicMixedException, HeuristicRollbackException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
	    
	    recoveryPropertyManager.getRecoveryEnvironmentBean()
				.setRecoveryBackoffPeriod(1);

		// ok, now drive a TX to completion. the script should ensure that the
		// recovery

		TestXAResource firstResource = new TestXAResource();
		TestXAResource secondResource = new TestXAResource();

		jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		jakarta.transaction.Transaction theTransaction = tm.getTransaction();

		theTransaction.enlistResource(firstResource);
		theTransaction.enlistResource(secondResource);

		tm.commit();

		TestXAResourceRecovery.setResources(firstResource, secondResource);

		assertEquals(0, firstResource.commitCount());
		assertEquals(0, secondResource.commitCount());
		assertEquals(0, firstResource.rollbackCount());
		assertEquals(0, secondResource.rollbackCount());

		RecoveryManager manager = RecoveryManager
				.manager(RecoveryManager.DIRECT_MANAGEMENT);
		manager.initialize();

		manager.scan();

		assertEquals(0, firstResource.rollbackCount());
		assertEquals(0, secondResource.rollbackCount());
		assertEquals(1, firstResource.commitCount());
		assertEquals(1, secondResource.commitCount());
	}

    /**
     * Test of top-down recovery with serializable XAResource.
     */
    @Test
    public void testRmFailXAResourceSerializable() throws Exception {
        
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryBackoffPeriod(1);

        TestXAResource firstResource = new TestXAResourceRmFail().clearCounters();
        TestXAResource secondResource = new TestXAResource();

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        theTransaction.enlistResource(firstResource);
        theTransaction.enlistResource(secondResource);

        tm.commit();

        assertEquals("first resource failed with rmfail, no commit expected", 0, firstResource.commitCount());
        assertEquals("second resource should be committed", 1, secondResource.commitCount());
        assertEquals("first resource: no rollback expected on rmfail", 0, firstResource.rollbackCount());
        assertEquals("second resource: expected to be committed", 0, secondResource.rollbackCount());

        RecoveryManager manager = RecoveryManager
                .manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        manager.scan();

        assertEquals("no rollback expecte on first resource", 0, firstResource.rollbackCount());
        assertEquals("no rollback expecte on second resource", 0, secondResource.rollbackCount());
        assertEquals("serializable first resource should be committed on recovery", 1, firstResource.commitCount());
        assertEquals("second resource should be already committed even without recovery",
                1, secondResource.commitCount());
    }
}