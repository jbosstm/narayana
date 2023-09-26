/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionScanner;

public class LogMoveTest
{
    @Test
    public void test()
    {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        OutputObjectState fluff = new OutputObjectState();
        Uid kungfuTx = new Uid();
        boolean passed = false;
        final String tn = new AtomicAction().type();

        try {
            UidHelper.packInto(kungfuTx, fluff);

            System.err.println("Creating dummy log");

            recoveryStore.write_committed(kungfuTx, tn, fluff);

            if (recoveryStore.currentState(kungfuTx, tn) == StateStatus.OS_COMMITTED) {
                System.err.println("Wrote dummy transaction " + kungfuTx);

                // quicker to deal with scanner directly
                
                ExpiredTransactionScanner scanner = new ExpiredTransactionScanner(tn, "/StateManager/ExpiredEntries");
                
                scanner.scan();
                
                scanner.scan();

                if (recoveryStore.currentState(kungfuTx, tn) == StateStatus.OS_COMMITTED)
                    System.err.println("Transaction log not moved!");
                else {
                    System.err.println("Transaction log moved!");

                    passed = true;
                }
            } else
                System.err.println("State is not committed!");
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }

        assertTrue(passed);
    }
}