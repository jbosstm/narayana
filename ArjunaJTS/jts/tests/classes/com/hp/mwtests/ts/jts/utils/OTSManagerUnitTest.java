/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.OTSManager;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class OTSManagerUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        assertTrue(OTSManager.factory() != null);
        assertTrue(OTSManager.get_factory() != null);      
        
        OTSImpleManager.current().begin();
        
        OTSManager.destroyControl(OTSImpleManager.current().get_control());
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        OTSManager.destroyControl(OTSImpleManager.current().suspendWrapper().getImple());
        
        OTSManager.setLocalSlotId(1234);
        
        assertEquals(OTSManager.getLocalSlotId(), 1234);
        
        OTSManager.setReceivedSlotId(5678);
        
        assertEquals(OTSManager.getReceivedSlotId(), 5678);
        
        OTSManager.setORB(null);
        OTSManager.setPOA(null);
    }
}