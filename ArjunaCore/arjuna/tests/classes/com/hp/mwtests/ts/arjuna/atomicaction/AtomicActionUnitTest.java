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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;


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
    
    @Test
    public void testCanCommitSuspendedTransaction() throws Exception
    {
    	AtomicAction aa = new AtomicAction();
    	aa.begin();
    	assertTrue(aa.Current() != null);
    	aa.suspend();
    	assertTrue(aa.Current() == null);
    	SimpleAbstractRecord simpleAbstractRecord = new SimpleAbstractRecord();
    	aa.add(simpleAbstractRecord);
    	aa.commit();
    	assertTrue(simpleAbstractRecord.wasCommitted());
    }
    
    private class SimpleAbstractRecord extends AbstractRecord {

		private boolean wasCommitted;

		@Override
		public int typeIs() {
			return 0;
		}

		public boolean wasCommitted() {
			return wasCommitted;
		}

		@Override
		public Object value() {
			return null;
		}

		@Override
		public void setValue(Object o) {
		}

		@Override
		public int nestedAbort() {
			return 0;
		}

		@Override
		public int nestedCommit() {
			return 0;
		}

		@Override
		public int nestedPrepare() {
			return 0;
		}

		@Override
		public int topLevelAbort() {
			return 0;
		}

		@Override
		public int topLevelCommit() {
			wasCommitted = true;
			return 0;
		}

		@Override
		public int topLevelPrepare() {
			return 0;
		}

		@Override
		public void merge(AbstractRecord a) {
		}

		@Override
		public void alter(AbstractRecord a) {
		}

		@Override
		public boolean shouldAdd(AbstractRecord a) {
			return false;
		}

		@Override
		public boolean shouldAlter(AbstractRecord a) {
			return false;
		}

		@Override
		public boolean shouldMerge(AbstractRecord a) {
			return false;
		}

		@Override
		public boolean shouldReplace(AbstractRecord a) {
			return false;
		}
    }
}
