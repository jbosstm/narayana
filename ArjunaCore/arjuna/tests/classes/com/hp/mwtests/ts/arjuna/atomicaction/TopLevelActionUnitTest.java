/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class TopLevelActionUnitTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        TopLevelAction tl = new TopLevelAction();
        
        A.begin();  // top level
        B.begin();  // nested
        
        tl.begin(); // nested top level
        
        A.abort();  // not recommended in practice!
        
        assertEquals(A.status(), ActionStatus.ABORTED);
        assertEquals(B.status(), ActionStatus.ABORTED);
        
        assertEquals(tl.status(), ActionStatus.RUNNING);
        
        tl.abort();
    }
}