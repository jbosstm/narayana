/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.statemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

public class ExtendedUnitTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        ExtendedObject bo = new ExtendedObject();
        Uid u = bo.get_uid();
        
        A.begin();

        bo.set(2);
       
        bo.toggle();
        
        A.commit();
        
        bo.terminate();
        
        bo = new ExtendedObject(u);
        
        assertEquals(bo.status(), ObjectStatus.PASSIVE);
        assertTrue(bo.getStore() != null);
        assertTrue(bo.getStoreRoot() != null);
        
        assertEquals(bo.objectType(), ObjectType.ANDPERSISTENT);
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        bo.print(pw);
    }

    @Test
    public void testCadaver () throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setReadonlyOptimisation(false);
        
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        
        ExtendedObject bo = new ExtendedObject();
        Uid id = bo.get_uid();
        
        bo = new ExtendedObject(id);
        
        A.begin();
        B.begin();
        
        bo.set(2);
       
        bo.terminate();
        
        B.commit();
        A.commit();
    }
    
    @Test
    public void testTryLock () throws Exception
    {
        ExtendedObject bo = new ExtendedObject();
        
        assertTrue(bo.lock());
        assertTrue(bo.unlock());
    }
    
    @Test
    public void testFail () throws Exception
    {
        ExtendedObject bo = new ExtendedObject();
        AtomicAction A = new AtomicAction();
        
        A.begin();
        A.commit();
        
        ThreadActionData.pushAction(A);  // put it back on this thread.
        
        bo.deactivate();
        bo.set_status();
        
        assertEquals(bo.activate(), false);
        assertEquals(bo.destroy(), false);
    }
 
    @Test
    public void testRememberAction () throws Exception
    {
        ExtendedObject bo = new ExtendedObject();
        final Uid u = bo.get_uid();
        
        bo.activate();
        bo.deactivate();
        
        bo = new ExtendedObject(u);
        
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        assertTrue(bo.remember(A));
        
        A.commit();
    }
}