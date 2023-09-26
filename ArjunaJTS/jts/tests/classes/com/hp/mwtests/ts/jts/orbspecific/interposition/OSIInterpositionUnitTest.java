/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.resources.osi.OSIInterpositionCreator;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class OSIInterpositionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        OTSImpleManager.current().begin();
        
        PropagationContext ctx = OTSImpleManager.current().get_control().get_coordinator().get_txcontext();
        OSIInterpositionCreator creator = new OSIInterpositionCreator();
        Control cnt = creator.recreate(ctx);
        ControlImple impl = creator.recreateLocal(ctx);
        
        assertTrue(cnt != null);
        assertTrue(impl != null);
        
        OTSImpleManager.current().rollback();
    }
}