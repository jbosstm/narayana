/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;

import com.hp.mwtests.ts.arjuna.resources.DummyHeuristic;
import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.HeuristicRecord;

public class HeuristicNotificationUnitTest
{
    @Test
    public void test () throws Exception
    {
        AtomicAction A = new AtomicAction();
        DummyHeuristic dh = new DummyHeuristic();
        
        A.begin();
        
        A.add(new BasicRecord());
        A.add(new BasicRecord());
        A.add(new HeuristicRecord());
        
        A.addSynchronization(dh);
        
        A.commit(false);
        
        assertEquals(TwoPhaseOutcome.HEURISTIC_MIXED, dh.getStatus());
    }
}