/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.tools.RecoveryMonitor;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.logging.jtaLogger;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Test the RecoveryMonitor is verbose mode
 */
public class RecoveryMonitorTest {

    @Test
    public void testRecoveryMonitorWithFailure() throws Exception {
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();

        // Ensure that test XAR is recoverable by adding the test XAResourceRecovery
        ArrayList<String> rcvClassNames = new ArrayList<>();

        rcvClassNames.add(XATestResourceXARecovery.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(rcvClassNames);
        XATestResourceXARecovery.setUseFaultyResources(true);

        TransactionImple tx = new TransactionImple(0); // begin a transaction

        // enlist a resource that behaves correctly
        assertTrue(tx.enlistResource(new XATestResource(XATestResource.OK_JNDI_NAME, false)));
        // enlist a resource that throws an exception from recover
        assertTrue(tx.enlistResource(new XATestResource(XATestResource.FAULTY_JNDI_NAME, true)));

        tx.commit();

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        manager.addModule(xaRecoveryModule); // we only need to test the XARecoveryModule

        try {
            manager.startRecoveryManagerThread(); // start periodic recovery

            String host = recoveryEnvironmentBean.getRecoveryAddress(); // the recovery listener host
            String rcPort = String.valueOf(recoveryEnvironmentBean.getRecoveryPort()); // the recovery listener port

            // trigger a recovery scan with verbose output
            RecoveryMonitor.main(new String[] {"-verbose", "-port", rcPort, "-host", host});

            // check the output of the scan
            assertEquals("ERROR", RecoveryMonitor.getResponse());
            assertEquals("ERROR", RecoveryMonitor.getSystemOutput());
            assertFalse(xaRecoveryModule.isPeriodicWorkSuccessful());
        } finally {
            manager.terminate();
        }
    }
}