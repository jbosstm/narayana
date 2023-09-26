/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery.contact;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.arjuna.ArjunaOTS.ArjunaFactory;
import com.arjuna.ArjunaOTS.ArjunaFactoryHelper;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class FactoryContactItemUnitTest extends TestBase
{
    @Test
    public void testNull () throws Exception
    {
        Uid dummyState = new Uid();
        FactoryContactItem item = FactoryContactItem.recreate(dummyState);
        
        assertTrue(item == null);
    }
    
    @Test
    public void test () throws Exception
    {
        TransactionFactoryImple factory = new TransactionFactoryImple("test");
        
        assertTrue(FactoryContactItem.createAndSave(ArjunaFactoryHelper.narrow(factory.getReference())));
        
        FactoryContactItem item = FactoryContactItem.recreate(com.arjuna.ats.arjuna.utils.Utility.getProcessUid());
        
        assertTrue(item != null);       
        assertTrue(item.getFactory() != null);
        
        item.markAsAlive();
        assertTrue(item.getAliveTime() != null);
        
        item.markAsDead();
        assertTrue(item.getCreationTime() != null);
        
        assertTrue(item.getUid() != Uid.nullUid());
    }
}