/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Stack;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.resources.XAResourceErrorHandler;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.resources.XAResourceMap;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyXA;

public class XAUtilsUnitTest
{
    class DummyXAResourceMap implements XAResourceMap
    {
        @Override
        public String getXAResourceName ()
        {
            return new DummyXA(false).getClass().getName();
        }

        @Override
        public boolean notAProblem (XAException ex, boolean commit)
        {
            return true;
        }
    }
    
    @Test
    public void test()
    {
        DummyXA xa = new DummyXA(false);
        
        assertFalse(XAUtils.mustEndSuspendedRMs(xa));
        assertTrue(XAUtils.canOptimizeDelist(xa));      
        assertEquals(XAUtils.getXANodeName(new XidImple(new Uid())), TxControl.getXANodeName());
    }
    
    @Test
    public void testXAResourceErrorHandler ()
    {
        Stack<XAResourceMap> list = new Stack<XAResourceMap>();
        DummyXAResourceMap map = new DummyXAResourceMap();
        
        list.push(map);
        
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceMaps(list);
        
        assertTrue(XAResourceErrorHandler.notAProblem(new DummyXA(false), new XAException(), true));
    }
}