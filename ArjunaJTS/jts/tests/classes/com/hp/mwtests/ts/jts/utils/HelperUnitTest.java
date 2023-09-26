/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class HelperUnitTest extends TestBase
{
    @Test
    public void test() throws Exception
    {
        Helper help = new Helper();
        TransactionFactoryImple factory = new TransactionFactoryImple("test");
        ControlImple tx = factory.createLocal(1000);
        
        assertTrue(Helper.localAction(tx.getControl()) != null);
        assertEquals(Helper.localControl(tx.getControl()), tx);
        
        assertTrue(Helper.getUidCoordinator(tx.get_coordinator()) != null);
        assertTrue(Helper.getUidCoordinator(tx.getControl()) != null);
        
        assertTrue(Helper.isUidCoordinator(tx.get_coordinator()));
    }
}