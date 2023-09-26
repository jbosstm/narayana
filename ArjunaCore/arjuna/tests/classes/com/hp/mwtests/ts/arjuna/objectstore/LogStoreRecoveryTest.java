/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.TestBase;

@RunWith(BMUnitRunner.class)
@BMScript("objectstore")
public class LogStoreRecoveryTest extends TestBase
{
    @Before
    public void setUp()
        {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());
        
        super.setUp();
        }

    @Test
    public void test()
    {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

        final int numberOfTransactions = 1000;
        final Uid[] ids = new Uid[numberOfTransactions];
        final int fakeData = 0xdeedbaaf;
        final String type = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/LogStoreRecoveryTest";

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

        /*
           * Remove 50% of the entries, simulating a crash during
           * normal execution.
           *
           * Q: why not just write 50% in the first place?
           * A: because we will extend this test to allow the recovery
           *    system to run in between writing and removing.
           */

        for (int i = 0; i < numberOfTransactions / 2; i++) {
            try {
                recoveryStore.remove_committed(ids[i], type);
            }
            catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
        /*
        try {
        */
            /*
                * Give the purger thread a chance to run and delete
                * the entries we've "removed" (really only marked as
                * being removable.)
                */
       /*
            Thread.sleep(12000);
        }
        catch (final Exception ex) {
        }
        */

        /*
           * Now get a list of entries to work on.
           */

        InputObjectState ios = new InputObjectState();
        boolean passed = true;

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

                if ((numberOfEntries == numberOfTransactions / 2) && passed) {
                    System.err.println("Would attempt recovery on " + numberOfEntries + " dead transactions.");
                } else {
                    passed = false;

                    System.err.println("Expected " + (numberOfTransactions / 2) + " and got " + numberOfEntries);
                }
            }
        }
        catch (final Exception ex) {
            ex.printStackTrace();
        }

        assertTrue(passed);
    }
}