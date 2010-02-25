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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.subordinate;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.SubordinateAtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;

import static org.junit.Assert.*;

class DummyTransactionImple extends TransactionImple
{
    public DummyTransactionImple()
    {
        super(0);
    }
    
    protected void commitAndDisassociate () throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, javax.transaction.SystemException, java.lang.IllegalStateException
    {
        super.commitAndDisassociate();
    }

    protected void rollbackAndDisassociate () throws java.lang.IllegalStateException, java.lang.SecurityException, javax.transaction.SystemException
    {
        super.rollbackAndDisassociate();
    }
}

public class SubordinateTxUnitTest
{
    @Test
    public void testTransactionImple () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        TransactionImple dummy = new TransactionImple(0);
        
        assertFalse(tx.equals(dummy));
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            tx.rollback();
            
            fail();
        }
        catch (InvalidTerminationStateException ex)
        {
        }
        
        assertEquals(tx.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        
        tx.doCommit();
        
        dummy.doRollback();
        
        tx = new TransactionImple(10);
        
        tx.doOnePhaseCommit();
        tx.doForget();
        
        tx.doBeforeCompletion();
        
        assertTrue(tx.toString() != null);
        assertTrue(tx.activated());
    }
    
    @Test
    public void testAtomicAction () throws Exception
    {
        SubordinateAtomicAction saa = new SubordinateAtomicAction();
        AtomicAction A = new AtomicAction();
        
        assertEquals(saa.commit(), ActionStatus.INVALID);
        assertEquals(saa.abort(), ActionStatus.INVALID);
        
        assertTrue(saa.type() != A.type());
        
        assertTrue(saa.activated());
        
        saa.doForget();
    }
    
    @Test
    public void testError () throws Exception
    {
        DummyTransactionImple dti = new DummyTransactionImple();
        
        try
        {
            dti.commitAndDisassociate();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {   
        }
        
        try
        {
            dti.rollbackAndDisassociate();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
    }
}
