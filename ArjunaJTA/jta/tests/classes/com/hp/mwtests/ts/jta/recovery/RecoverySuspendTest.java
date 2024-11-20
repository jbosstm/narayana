package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;
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
import javax.transaction.xa.Xid;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@WithByteman
@BMUnitConfig(debug = true)
public class RecoverySuspendTest {

    private static RecoveryManager _manager;

    private final static String[] modules = {
            // the Recovery Module to test
            "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"
    };

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
        // don't sign off until the store is empty
        _recoveryConfig.setWaitForWorkLeftToDo(true);

        // the orphan filter
        XAResourceOrphanFilter xaResourceOrphanFilter = new NodeNameXAResourceOrphanFilter();
        _jtaEnvironmentBean.setXaResourceOrphanFilters(List.of(xaResourceOrphanFilter));
        // the test set of modules
        _recoveryConfig.setRecoveryModuleClassNames(Arrays.asList(modules));

        _jtaEnvironmentBean.setXaResourceRecoveryClassNames(
                List.of(BytemanControlledXAResourceRecovery.class.getName()));

        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(List.of("*"));
    }

    @AfterEach
    public void afterEach() {
        if (_manager.mode() == PeriodicRecovery.Mode.SUSPENDED.ordinal()) {
            // In case the Recovery Manager was suspended, this will resume it
            _manager.resume();
        }

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
            @BMScript("RecoverySuspendTest/recoverySuspendTest_PeriodicRecovery")
    })
    public void testSuspensionWhenThereArentTransactionsToRecover() {

        // Make sure that the test environment is ready
        BytemanControlledXAResource.resetCommitCallCounter();
        BytemanControlledXAResource.resetRollbackCallCounter();
        // The following instruction makes the BytemanControlledXAResource commit without failures.
        BytemanControlledXAResource.setGreenFlag();
        BytemanControlledXAResource.setCommitReturn(XAResource.XA_OK);

        // BytemanControlledXAResource.getCommitCallCounter() should be 1 as:
        // - One invocation from the normal commit procedure should pass
        startTest(1,0, 0);
    }

    @Test
    @BMScripts(scripts = {
            @BMScript("RecoverySuspendTest/recoverySuspendTest_BytemanControlledXAResource"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_FailTest")
    })
    public void testSuspensionWhenThereIsXATxnToRecover() {

        // Make sure that the test environment is ready
        BytemanControlledXAResource.resetCommitCallCounter();
        BytemanControlledXAResource.resetRollbackCallCounter();
        BytemanControlledXAResource.resetGreenFlag();
        BytemanControlledXAResource.setCommitReturn(XAException.XAER_RMFAIL);

        // BytemanControlledRecord.getCommitCallCounter() should be 3 as:
        // - One invocation from the normal commit procedure should fail
        // - One invocation from the recovery process should fail
        // - One invocation from the recovery process should pass
        startTest(1, 2, 0);
    }

    private void startTest(int numberOfCommits, int numberOfRollbacks, int timeout) {

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
        _manager.suspend(true);

        Assertions.assertEquals(numberOfCommits, BytemanControlledXAResource.getCommitCallCounter(),
                String.format("BytemanControlledXAResource's getCommitCallCounter is %d but it should have been %d",
                        BytemanControlledXAResource.getCommitCallCounter(), numberOfCommits));

        Assertions.assertEquals(numberOfRollbacks, BytemanControlledXAResource.getRollbackCallCounter(),
                String.format("BytemanControlledXAResource's getRollbackCallCounter is %d but it should have been %d",
                        BytemanControlledXAResource.getRollbackCallCounter(), numberOfRollbacks));
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
}
