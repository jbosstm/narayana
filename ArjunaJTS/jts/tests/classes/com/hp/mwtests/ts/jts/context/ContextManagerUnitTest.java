/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.context;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ArjunaOTS.ActionControl;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.context.ContextManager;
import com.arjuna.ats.internal.jts.context.ContextPropagationManager;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ContextManagerUnitTest extends TestBase
{
    @Test
    public void testContextPropagationManager () throws Exception
    {
        ContextPropagationManager manager = new ContextPropagationManager();
    }
    
    @Test
    public void testContextManager () throws Exception
    {
        ContextManager manager = new ContextManager();
        
        assertEquals(manager.current(Thread.currentThread().getName()), null);
        assertEquals(manager.current(), null);
        
        OTSImpleManager.current().begin();
        
        manager.associate();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        Control ct = OTSImpleManager.current().suspend();
        
        manager.addRemoteHierarchy(ct);

        manager.popAction();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        ActionControl cont = (ActionControl) OTSImpleManager.current().getControlWrapper().getImple().getControl();
        
        manager.addActionControlHierarchy(cont);
        
        manager.purgeActions();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        manager.addControlImpleHierarchy(OTSImpleManager.current().getControlWrapper().getImple());
        
        manager.purgeActions();
        
        OTSImpleManager.current().suspend();
    }
}