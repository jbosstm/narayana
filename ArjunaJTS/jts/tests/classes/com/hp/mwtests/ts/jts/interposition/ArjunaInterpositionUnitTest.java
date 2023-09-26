/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.interposition;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.InterpositionCreator;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ArjunaInterpositionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        InterpositionCreator creator = new InterpositionCreator();
        
        OTSImpleManager.current().begin();
        OTSImpleManager.current().begin();
        
        PropagationContext ctx = OTSImpleManager.current().get_control().get_coordinator().get_txcontext();      
        
        assertTrue(creator.recreateLocal(ctx) != null);       
        assertTrue(creator.recreate(ctx) != null);
        
        OTSImpleManager.current().rollback();
        OTSImpleManager.current().rollback();
    }
}