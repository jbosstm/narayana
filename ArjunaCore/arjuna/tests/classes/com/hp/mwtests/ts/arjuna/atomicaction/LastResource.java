/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;
import com.hp.mwtests.ts.arjuna.resources.OnePhase;
import com.hp.mwtests.ts.arjuna.resources.ShutdownRecord;

public class LastResource
{
    @Test
    public void run()
    {
        AtomicAction A = new AtomicAction();
        OnePhase opRes = new OnePhase();

        A.begin();
        A.add(new LastResourceRecord(opRes));
        A.add(new ShutdownRecord(ShutdownRecord.FAIL_IN_PREPARE));
        A.commit();
        
        assertEquals(OnePhase.ROLLEDBACK, opRes.status());

        A = new AtomicAction();
        opRes = new OnePhase();

        A.begin();
        A.add(new LastResourceRecord(opRes));
        A.add(new ShutdownRecord(ShutdownRecord.FAIL_IN_COMMIT));
        A.commit();
        
        assertEquals(OnePhase.COMMITTED, opRes.status());

        A = new AtomicAction();
        A.begin();
        A.add(new LastResourceRecord(new OnePhase()));
        
        assertEquals(AddOutcome.AR_DUPLICATE, A.add(new LastResourceRecord(new OnePhase())) );

        A.abort();
    }
}