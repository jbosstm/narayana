/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;

public class LogStoreTest
{
    @Test
    public void test()
    {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());

        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        final int numberOfTransactions = 1000;
        final Uid[] ids = new Uid[numberOfTransactions];
        final int fakeData = 0xdeedbaaf;
        final String type = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/Test";

        for (int i = 0; i < numberOfTransactions; i++) {
            OutputObjectState dummyState = new OutputObjectState();

            try {
                dummyState.packInt(fakeData);
                ids[i] = new Uid();
                recoveryStore.write_committed(ids[i], type, dummyState);
            }
            catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        InputObjectState ios = new InputObjectState();
        boolean passed = false;

        try {
            if (recoveryStore.allObjUids(type, ios, StateStatus.OS_UNKNOWN)) {
                Uid id = new Uid(Uid.nullUid());
                int numberOfEntries = 0;

                do {
                    try {
                        id = UidHelper.unpackFrom(ios);
                    }
                    catch (Exception ex) {
                        id = Uid.nullUid();
                    }

                    if (id.notEquals(Uid.nullUid())) {
                        passed = true;

                        System.err.println("Located transaction log " + id + " in object store.");

                        numberOfEntries++;

                        boolean found = false;

                        for (int i = 0; i < ids.length; i++) {
                            if (id.equals(ids[i]))
                                found = true;
                        }

                        if (passed && !found) {
                            passed = false;

                            System.err.println("Found unexpected transaction!");
                        }
                    }
                }
                while (id.notEquals(Uid.nullUid()));

                if ((numberOfEntries != ids.length) && passed) {
                    passed = false;

                    System.err.println("Expected " + ids.length + " and got " + numberOfEntries);
                }
            }
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }

        assertTrue(passed);
    }
}