/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.internal.jts.coordinator.CheckedActions;
import com.hp.mwtests.ts.jts.resources.TestBase;


public class CheckedActionsUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        CheckedAction ca = new CheckedAction();
        
        CheckedActions.set(ca);
        
        assertEquals(CheckedActions.get(), ca);
        
        CheckedActions.remove();
        
        assertEquals(CheckedActions.get(), null);
    }
}