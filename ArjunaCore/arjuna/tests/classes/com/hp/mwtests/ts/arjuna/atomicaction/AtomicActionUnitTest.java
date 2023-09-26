/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
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
			return RecordType.USER_DEF_FIRST0;
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
			return TwoPhaseOutcome.FINISH_OK;
		}

		@Override
		public int nestedCommit() {
			return TwoPhaseOutcome.FINISH_OK;
		}

		@Override
		public int nestedPrepare() {
			return TwoPhaseOutcome.PREPARE_OK;
		}

		@Override
		public int topLevelAbort() {
			return TwoPhaseOutcome.FINISH_OK;
		}

		@Override
		public int topLevelCommit() {
			wasCommitted = true;
			return TwoPhaseOutcome.FINISH_OK;
		}

		@Override
		public int topLevelPrepare() {
			return TwoPhaseOutcome.PREPARE_OK;
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