/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransactionReplayer;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.resources.TestBase;

import org.omg.CosTransactions.Status;


public class RecoveredTransactionReplayerUnitTest extends TestBase
{
    @Test
    public void testCommitted () throws Exception
    {
        /*
         * Deliberately choose a Uid that doesn't represent a state
         * on disk.
         * 
         * Use un-threaded.
         */
        
        Uid dummyUid = new Uid();
        String dummyState = "/StateManager/DummyState";
        RecoveredTransactionReplayer replayer = new RecoveredTransactionReplayer(dummyUid, dummyState);
        DemoResource res = new DemoResource();

        assertTrue(RecoveredTransactionReplayer.isPresent(dummyUid) != null);

        replayer.swapResource(dummyUid, res.getResource());
        
        replayer.run();  

        assertTrue(RecoveredTransactionReplayer.isPresent(dummyUid) == null);
    }
}