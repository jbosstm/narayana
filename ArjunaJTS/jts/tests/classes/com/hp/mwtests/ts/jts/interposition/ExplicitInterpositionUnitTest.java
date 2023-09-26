/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.interposition;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.ExplicitInterposition;
import com.hp.mwtests.ts.jts.orbspecific.resources.ExplicitStackImple;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ExplicitInterpositionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ExplicitStackImple stack = new ExplicitStackImple();
        OTSImpleManager.current().begin();
        
        assertEquals(stack.push(10, OTSImpleManager.current().get_control()), 0);
        
        OTSImpleManager.current().rollback();
        
        OTSImpleManager.current().begin();
        
        ExplicitInterposition inter = new ExplicitInterposition(OTSImpleManager.current().get_control().get_coordinator().get_txcontext(), true);
        
        inter.unregisterTransaction();
        
        OTSImpleManager.current().rollback();
    }
}