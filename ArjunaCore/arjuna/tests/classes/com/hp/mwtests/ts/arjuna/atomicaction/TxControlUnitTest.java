/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TxControl;

public class TxControlUnitTest
{
    @Test
    public void testDisable () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        TxControl.disable();
        
        A.begin();
        A.commit();
        
        assertEquals(A.status(), ActionStatus.ABORTED);
        
        TxControl.enable();
        
        A = new AtomicAction();
        
        A.begin();
        A.commit();
        
        assertEquals(A.status(), ActionStatus.COMMITTED);
    }
    
    @Test
    public void testGetSet () throws Exception
    {
        TxControl.setDefaultTimeout(1000);
        
        assertEquals(TxControl.getDefaultTimeout(), 1000);
        
        assertEquals(TxControl.getAsyncPrepare(), arjPropertyManager.getCoordinatorEnvironmentBean().isAsyncPrepare());
        
        assertEquals(TxControl.getMaintainHeuristics(), arjPropertyManager.getCoordinatorEnvironmentBean().isMaintainHeuristics());
        
        String nodeName = "1";
        
        TxControl.setXANodeName(nodeName);
        
        assertTrue(TxControl.getXANodeName().equals(nodeName));
    }
}