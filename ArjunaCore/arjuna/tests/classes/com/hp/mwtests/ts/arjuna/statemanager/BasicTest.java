/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.statemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

public class BasicTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        BasicObject bo = new BasicObject();

        A.begin();

        bo.set(2);

        A.commit();
        
        assertTrue(bo.getStore() != null);
        assertTrue(bo.getStoreRoot() != null);
        
        assertEquals(bo.getObjectModel(), ObjectModel.SINGLE);
    }

    @Test
    public void testNested () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();      
        BasicObject bo = new BasicObject();
        Uid u = bo.get_uid();
        
        A.begin();
        B.begin();
        
        bo.set(2);

        B.commit();
        A.commit();

        bo = new BasicObject(u);
        
        A = new AtomicAction();
        
        A.begin();
        
        assertEquals(bo.get(), 2);
        
        A.commit();
    }
}