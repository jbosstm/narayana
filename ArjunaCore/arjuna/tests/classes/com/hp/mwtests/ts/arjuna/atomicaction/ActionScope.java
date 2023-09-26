/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class ActionScope
{
    @Test
    public void test() throws Exception
    {
        AtomicAction atomicAction = new AtomicAction();

        atomicAction.begin();
        atomicAction.commit();

        assertEquals(ActionStatus.COMMITTED, atomicAction.status());
    }
}