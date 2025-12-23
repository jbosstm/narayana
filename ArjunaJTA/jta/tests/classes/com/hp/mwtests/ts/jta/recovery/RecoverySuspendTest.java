/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.hp.mwtests.ts.jta.common.BytemanControlledXAResource;
import com.hp.mwtests.ts.jta.common.BytemanControlledXAResourceRecovery;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMScripts;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.WithByteman;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@WithByteman
@BMUnitConfig(debug = true)
public class RecoverySuspendTest {

    private static RecoveryManager _manager;

    private final static List<String> modules = List.of(
            "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
            // the Recovery Module to test
            "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"
    );

    private static RecoveryEnvironmentBean _recoveryConfig;
    private static JTAEnvironmentBean _jtaEnvironmentBean;

    // fields read with byteman
    private final static int periodicRecoveryPeriod = 5;
    private final static int recoveryBackoffPeriod = 1;

    @BeforeAll
    public static void beforeClass() {
        _recoveryConfig = recoveryPropertyManager.getRecoveryEnvironmentBean();
        _jtaEnvironmentBean = jtaPropertyManager.getJTAEnvironmentBean();

        // Let's go quick on this
        _recoveryConfig.setRecoveryBackoffPeriod(recoveryBackoffPeriod);
        _recoveryConfig.setPeriodicRecoveryPeriod(periodicRecoveryPeriod);
        /*
         * Set this interval to be equal to periodicRecoveryPeriod.
         * The reason behind this choice is:
         * 1) Activate the Recovery Manager
         * 2) Create an XA transaction
         * 3) After periodicRecoveryPeriod seconds, the XA txn can be
         * processed by the XA Recovery Module
         */
        _jtaEnvironmentBean.setOrphanSafetyInterval(periodicRecoveryPeriod);

        _recoveryConfig.setRecoveryModuleClassNames(modules);

        _jtaEnvironmentBean.setXaResourceRecoveryClassNames(
                List.of(BytemanControlledXAResourceRecovery.class.getName()));

        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(List.of("*"));
    }

    @AfterEach
    public void afterEach() {
        _manager.terminate();

        // Let's clean
        ThreadActionData.purgeActions();

        if (!TxControl.isEnabled()) {
            // Re-activate transaction creation
            TxControl.enable();
        }
    }

    @Test
    @BMScripts(scripts = {
            @BMScript("RecoverySuspendTest/recoverySuspendTest_BytemanControlledXAResource"),
            @BMScript("RecoverySuspendTest/recoverySusptenTest_BytemanControlledXAResource_commitFailure"),
            @BMScript("RecoverySuspendTest/recoverySusptenTest_BytemanControlledXAResource_recoverFailure"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_FailTest")
    })
    public void testSuspensionWhenThereIsXATransactionsToRecover() {

        // Make sure that the test environment is ready
        resetCounters();
        BytemanControlledXAResource.setCommitReturn(XAException.XAER_RMFAIL);

        /*
         * BytemanControlledXAResource.getCommitCallCounter() should be 2 as:
         * - One invocation from the normal commit procedure should fail
         * - One invocation from the recovery process should pass
         * (i.e. when processing AtomicActionRecoveryModule)
         * BytemanControlledXAResource.getRecoverCallCounter() should be 1 as:
         * - One invocation comes from the recovery cycle that recovers the transaction
         */
        startTest(2, 0, 1, 0);
    }

    @Test
    @BMScripts(scripts = {
            @BMScript("RecoverySuspendTest/recoverySuspendTest_BytemanControlledXAResource"),
            @BMScript("RecoverySuspendTest/recoverySusptenTest_BytemanControlledXAResource_commitFailure"),
            @BMScript("RecoverySuspendTest/recoverySusptenTest_BytemanControlledXAResource_rollbackFailure"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_SkipAtomicActionRecoveryModule"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_FailTest"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_Rendezvous")
    })
    public void testSuspensionWhenThereIsXAOrphanToRecover() {

        /*
         * AtomicActionRecoveryModule (AARM) is used only at the end of the test to clean the environment.
         * In other words, instead of completing/deleting the transaction manually, the Recovery Manager
         * will commit the transaction and, as a consequence, won't get stuck during suspension.
         * NOTE: this is an artificial scenario as the transaction that the AARM eventually commits
         * has already been rolled back by BytemanControlledXAResource (locally to itself).
         * Thus, the transaction should have resulted in a heuristic outcome.
         * The outcome achieved through the use of AARM isn't specs compliant,
         * but it is good enough to clean the environment and test XARecoveryModule in isolation.
         */

        // Set up the orphan filter
        XAResourceOrphanFilter xaResourceOrphanFilter = new NodeNameXAResourceOrphanFilter();
        _jtaEnvironmentBean.setXaResourceOrphanFilters(List.of(xaResourceOrphanFilter));

        // Make sure that the test environment is ready
        resetCounters();
        BytemanControlledXAResource.setCommitReturn(XAException.XAER_RMFAIL);

        /*
         * BytemanControlledXAResource.getCommitCallCounter() should be 2 as:
         * - One invocation from the normal commit procedure should fail
         * - One invocation from the recovery process should pass
         * (i.e. when processing AtomicActionRecoveryModule)
         * BytemanControlledXAResource.getRollbackCallCounter() should be 2 as:
         * - One invocation from the recovery process should fail
         * - One invocation from the recovery process should pass
         * BytemanControlledXAResource.getRecoverCallCounter() should be 3
         * as there is a total of 3 recovery process's passes
         */
        startTest(2, 2, 3, 0);

        // Reset orphan filters setup
        _jtaEnvironmentBean.setXaResourceOrphanFilters(null);
    }

    private void startTest(int numberOfCommits, int numberOfRollbacks, int numberOfRecovers, int timeout) {

        createXATransaction(new BytemanControlledXAResource(), timeout);

        // The Transaction System needs to be disabled, i.e. no new transactions can be created
        TxControl.disable();

        // Makes sure that the transaction reaper completed all transactions
        TransactionReaper.transactionReaper().waitForAllTxnsToTerminate();

        // Makes sure that all transactions without a timeout have passed the `prepare` phase
        waitForTransactionsWithoutTimeout();

        // obtain a new RecoveryManager with the above config:
        _manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);

        // Synchronization is not needed (i.e. async = true) as isWaitForWorkLeftToDo() == true
        _manager.suspend(true, true);

        Assertions.assertEquals(numberOfCommits, BytemanControlledXAResource.getCommitCallCounter(),
                String.format("BytemanControlledXAResource's getCommitCallCounter is %d but it should have been %d",
                        BytemanControlledXAResource.getCommitCallCounter(), numberOfCommits));

        Assertions.assertEquals(numberOfRollbacks, BytemanControlledXAResource.getRollbackCallCounter(),
                String.format("BytemanControlledXAResource's getRollbackCallCounter is %d but it should have been %d",
                        BytemanControlledXAResource.getRollbackCallCounter(), numberOfRollbacks));

        Assertions.assertEquals(numberOfRecovers, BytemanControlledXAResource.getRecoverCallCounter(),
                String.format("BytemanControlledXAResource's getRecoverCallCounter is %d but it should have been %d",
                        BytemanControlledXAResource.getRecoverCallCounter(), numberOfRecovers));
    }

    private void createXATransaction(XAResource xaResource, int timeout) {
        TransactionImple tx = new TransactionImple(timeout);

        try {
            tx.enlistResource(new TestXAResource());
            tx.enlistResource(xaResource);
        } catch (SystemException | RollbackException e) {
            fail();
        }

        try {
            tx.commit();
        } catch (HeuristicRollbackException | SystemException | HeuristicMixedException | RollbackException e) {
            // it is fine if the transaction fails
        }
    }

    private void waitForTransactionsWithoutTimeout() {
        while (ActionManager.manager().getNumberOfInflightTransactions() > 0) {
            try {
                // Let's re-check in 1 second
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void resetCounters() {
        BytemanControlledXAResource.resetCommitCallCounter();
        BytemanControlledXAResource.resetRollbackCallCounter();
        BytemanControlledXAResource.resetRecoverCallCounter();
        BytemanControlledXAResource.resetGreenFlag();
    }
}
