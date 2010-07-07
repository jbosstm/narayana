/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.atomicaction;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TxControl;

import static org.junit.Assert.*;

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
        
        String nodeName = "NodeName";
        
        TxControl.setXANodeName(nodeName.getBytes());
        
        assertEquals(new String(TxControl.getXANodeName()), nodeName);
    }
}
