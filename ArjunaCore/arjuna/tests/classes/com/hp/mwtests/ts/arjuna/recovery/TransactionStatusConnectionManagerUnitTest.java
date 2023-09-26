/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;

/*
 * This class does get tested mostly through other tests that we run. This test
 * suite is simply to cover the edge cases.
 */

public class TransactionStatusConnectionManagerUnitTest
{
    @Test
    public void testPresumedAbort ()
    {
        Uid tx = new Uid();
        TransactionStatusConnectionManager tscm = new TransactionStatusConnectionManager();
        
        assertEquals(tscm.getTransactionStatus(tx), ActionStatus.ABORTED);
    }
}