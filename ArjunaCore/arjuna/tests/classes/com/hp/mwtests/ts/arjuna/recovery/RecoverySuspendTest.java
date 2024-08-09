/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.BytemanControlledRecord;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMScripts;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.WithByteman;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@WithByteman
@BMUnitConfig(debug=true)
public class RecoverySuspendTest {

    private static RecoveryManager _manager;
    private static RecordTypeMap _recordTypeMap;

    // fields read with byteman
    private final static int periodicRecoveryPeriod = 5;
    private final static int recoveryBackoffPeriod = 1;

    @BeforeAll
    public static void beforeClass() {

        RecoveryEnvironmentBean recoveryConfig = recoveryPropertyManager.getRecoveryEnvironmentBean();
        String[] modules = {
                // the module to test
                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule"
        };

        // Let's go quick on this
        recoveryConfig.setRecoveryBackoffPeriod(recoveryBackoffPeriod);
        recoveryConfig.setPeriodicRecoveryPeriod(periodicRecoveryPeriod);
        // don't sign off until the store is empty
        recoveryConfig.setWaitForRecovery(true);

        // the test set of modules
        recoveryConfig.setRecoveryModuleClassNames(Arrays.asList(modules));

        // obtain a new RecoveryManager with the above config:
        _manager = RecoveryManager.manager(RecoveryManager.INDIRECT_MANAGEMENT);
        // recovery can start
        _manager.startRecoveryManagerThread();

        _recordTypeMap = new RecordTypeMap() {
            @SuppressWarnings("unchecked")
            public Class getRecordClass ()
            {
                return BytemanControlledRecord.class;
            }

            public int getType ()
            {
                return RecordType.USER_DEF_FIRST0;
            }
        };

        RecordTypeManager.manager().add(_recordTypeMap);
    }

    @BeforeEach
    public void beforeEach() {
        // In case the Recovery Manager was suspended, this will resume it
        _manager.resume();
    }

    @AfterAll
    public static void afterClass() {
        RecordTypeManager.manager().remove(_recordTypeMap);
    }

    @AfterEach
    public void afterEach() {
        if (!TxControl.isEnabled()) {
            // Re-activate transaction creation
            TxControl.enable();
        }
    }

    @Test
    @BMScripts(scripts = {
                    @BMScript("RecoverySuspendTest/recoverySuspendTest_BytemanControlledRecord"),
                    @BMScript("RecoverySuspendTest/recoverySuspendTest_PeriodicRecovery")
            })
    public void testSuspensionWhenThereArentTransactionsToRecover() {

        // Make sure that the test environment is ready
        BytemanControlledRecord.resetCommitCallCounter();
        // The following instruction makes the BytemanControlledRecord commit without failures.
        BytemanControlledRecord.setGreenFlag();

        createAtomicAction(new BytemanControlledRecord(true));

        // The Transaction System needs to be disabled, i.e. no new transactions can be created
        TxControl.disable();

        // Makes sure that the transaction reaper completed all transactions
        TransactionReaper.transactionReaper().waitForAllTxnsToTerminate();

        // Synchronization is not needed (i.e. async = true) as isWaitForRecovery() == true
        _manager.suspend(true);

        // BytemanControlledRecord.getCommitCallCounter() should be 1 as:
        // - One invocation from the normal commit procedure should pass
        Assertions.assertEquals(1, BytemanControlledRecord.getCommitCallCounter(),
                String.format("BytemanControlledRecord's getCommitCallCounter is %d but it should have been 1",
                        BytemanControlledRecord.getCommitCallCounter()));
    }

    @Test
    @BMScripts(scripts = {
            @BMScript("RecoverySuspendTest/recoverySuspendTest_BytemanControlledRecord"),
            @BMScript("RecoverySuspendTest/recoverySuspendTest_FailTest")
    })
    public void testSuspensionWhenThereIsAtomicActionToRecover() {

        // Make sure that the test environment is ready
        BytemanControlledRecord.resetCommitCallCounter();
        BytemanControlledRecord.resetGreenFlag();
        
        createAtomicAction(new BytemanControlledRecord(true));

        // The Transaction System needs to be disabled, i.e. no new transactions can be created
        TxControl.disable();

        // Makes sure that the transaction reaper completed all transactions
        TransactionReaper.transactionReaper().waitForAllTxnsToTerminate();

        // Synchronization is not needed (i.e. async = true) as isWaitForRecovery() == true
        _manager.suspend(true);

        // BytemanControlledRecord.getCommitCallCounter() should be 3 as:
        // - One invocation from the normal commit procedure should fail
        // - One invocation from the recovery process should fail
        // - One invocation from the recovery process should pass
        Assertions.assertEquals(3, BytemanControlledRecord.getCommitCallCounter(), 
                String.format("BytemanControlledRecord's getCommitCallCounter is %d but it should have been 3",
                        BytemanControlledRecord.getCommitCallCounter()));
    }

    private void createAtomicAction(AbstractRecord secondRecord) {

        AtomicAction A = new AtomicAction();

        BasicRecord basicRecordOne = new BasicRecord();

        A.begin();

        A.add(basicRecordOne);
        A.add(secondRecord);

        A.commit();
    }
}
