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

import java.util.Hashtable;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import org.junit.Test;
import static org.junit.Assert.*;


public class AtomicActionUnitTest
{
    @Test
    public void testBasic () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        assertEquals(A.hierarchyDepth(), 1);
        
        assertEquals(A.topLevelActionUid(), A.get_uid());
        
        A.end(true);
        
        assertEquals(A.status(), ActionStatus.COMMITTED);
        assertEquals(A.getTimeout(), AtomicAction.NO_TIMEOUT);
        assertTrue(BasicAction.Current() != null);
        
        ThreadActionData.purgeActions();
        
        assertEquals(BasicAction.Current(), null);
        
        assertTrue(A.type() != null);
        assertTrue(BasicAction.maintainHeuristics());
        
        assertTrue(A.destroy());
    }
    
    @Test
    public void testThreading () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();      
        
        assertEquals(A.status(), ActionStatus.RUNNING);

        assertTrue(A.addChildThread());
        
        A.addThread();
        A.addThread(new Thread());
        
        assertEquals(A.activeThreads(), 1);
        
        A.removeChildThread();
        
        assertEquals(A.activeThreads(), 0);
        
        A.commit(true);
        
        assertEquals(A.status(), ActionStatus.COMMITTED);
        
        ThreadActionData.purgeActions();
    }
    
    @Test
    public void testPreventCommit () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        A.preventCommit();
        
        A.commit();
        
        assertEquals(A.status(), ActionStatus.ABORTED);
    }
    
    @Test
    public void testNested () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        
        A.begin();
        B.begin();
        
        assertTrue(A.childTransactions().length == 1);
        
        B.commit();
        A.abort();
        
        assertEquals(A.deactivate(), true);
    }
    
    @Test
    public void testActivateDeactivate () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        assertEquals(A.activate(), false);
        
        A.abort();
        
        assertEquals(A.deactivate(), true);
    }
}
