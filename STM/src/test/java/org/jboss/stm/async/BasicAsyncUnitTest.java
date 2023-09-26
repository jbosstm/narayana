/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.async;

import java.io.IOException;
import java.util.concurrent.Future;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

public class BasicAsyncUnitTest extends TestCase
{
    
    public void testBeginCommit () throws Exception
    {
        Transaction tx = new Transaction();
        Future<Integer> beginResult = tx.begin();
        
        while (!beginResult.isDone())
        {
            System.out.println("Waiting for transaction begin to complete");
            Thread.sleep(1);
        }
        
        assertTrue(beginResult.get().intValue() == ActionStatus.RUNNING);
        
        Future<Integer> commitResult = tx.commit();
        
        while (!commitResult.isDone())
        {
            System.out.println("Waiting for transaction commit to complete");
            Thread.sleep(1);
        }
        
        assertTrue(commitResult.get().intValue() == ActionStatus.COMMITTED);
    }
    
    public void testBeginAbort () throws Exception
    {
        Transaction tx = new Transaction();
        Future<Integer> beginResult = tx.begin();
        
        while (!beginResult.isDone())
        {
            System.out.println("Waiting for transaction begin to complete");
            Thread.sleep(1);
        }
        
        assertTrue(beginResult.get().intValue() == ActionStatus.RUNNING);
        
        Future<Integer> abortResult = tx.abort();
        
        while (!abortResult.isDone())
        {
            System.out.println("Waiting for transaction abort to complete");
            Thread.sleep(1);
        }
        
        assertTrue(abortResult.get().intValue() == ActionStatus.ABORTED);
    }
}