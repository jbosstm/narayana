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

package com.hp.mwtests.ts.jta.jts.subordinate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.TransactionImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

class DummyTransactionImple extends TransactionImple
{
    public DummyTransactionImple(AtomicTransaction imported)
    {
        super(imported);
    }

    public void commitAndDisassociate () throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, javax.transaction.SystemException, java.lang.IllegalStateException
    {
        super.commitAndDisassociate();
    }

    public void rollbackAndDisassociate () throws java.lang.IllegalStateException, java.lang.SecurityException, javax.transaction.SystemException
    {
        super.rollbackAndDisassociate();
    }
}


public class TransactionImpleUnitTest extends TestBase
{   
    @Test
    public void test () throws Exception
    {
        OTSImpleManager.current().begin();
        
        TransactionImple tx = new TransactionImple(new SubordinateAtomicTransaction(new ServerControlWrapper(OTSImpleManager.current().get_control())));
        
        assertFalse(tx.equals(null));
        assertTrue(tx.equals(tx));
        assertFalse(tx.equals(new TransactionImple(new AtomicTransaction())));
        assertFalse(tx.equals(new Object()));
        assertTrue(tx.toString() != null);
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
        
        try
        {
            tx.rollback();
            
            fail();
        }
        catch (final InvalidTerminationStateException ex)
        {
        }
        
        tx.doBeforeCompletion();
        
        tx.doPrepare();
        
        try
        {
            tx.doCommit();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        try
        {
            tx.doRollback();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        try
        {
            tx.doOnePhaseCommit();
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        tx.doForget();
        
        OTSImpleManager.current().rollback();
        
        DummyTransactionImple dummy = new DummyTransactionImple(new AtomicTransaction());
        
        try
        {
            dummy.commitAndDisassociate();
        }
        catch (final InactiveTransactionException ex)
        {
        }
        
        try
        {
            dummy.rollbackAndDisassociate();
        }
        catch (final InactiveTransactionException ex)
        {
        }
    }
}
