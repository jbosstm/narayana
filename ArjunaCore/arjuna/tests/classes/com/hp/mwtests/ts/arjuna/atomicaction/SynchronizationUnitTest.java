/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.hp.mwtests.ts.arjuna.resources.SyncRecord;

public class SynchronizationUnitTest
{
    @Test
    public void test () throws Exception
    {
        AtomicAction A = new AtomicAction();
        SyncRecord sr = new SyncRecord();
        
        A.begin();
        
        assertEquals(A.addSynchronization(sr), AddOutcome.AR_ADDED);
        assertEquals(A.getSynchronizations().size(), 1);
        
        A.commit();
        
        assertTrue(sr.called());
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        assertEquals(A.addSynchronization(null), AddOutcome.AR_REJECTED);
        
        A.abort();
    }
}