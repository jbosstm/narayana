/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.interposition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerControlWrapperUnitTest extends TestBase
{
    @Test
    public void testEquality () throws Exception
    {
        TransactionFactoryImple imple = new TransactionFactoryImple("test");
        ControlImple tx = imple.createLocal(1000);
        
        ServerControlWrapper wrap1 = new ServerControlWrapper(tx);
        ServerControlWrapper wrap2 = new ServerControlWrapper(tx.getControl());
        
        assertTrue(wrap1.get_uid().equals(wrap2.get_uid()));
        
        wrap1 = new ServerControlWrapper(tx.getControl(), tx);
        wrap2 = new ServerControlWrapper(tx.getControl(), tx.get_uid());
        
        assertTrue(wrap1.get_uid().equals(wrap2.get_uid()));
    }
    
    @Test
    public void testNested () throws Exception
    {
        TransactionFactoryImple imple = new TransactionFactoryImple("test");
        ControlImple tx = imple.createLocal(1000);
        
        ServerControlWrapper wrap = new ServerControlWrapper(tx);
        ControlWrapper sub = wrap.create_subtransaction();
        
        assertTrue(sub != null);
        
        assertEquals(sub.cancel(), ActionStatus.ABORTED);
    }
}