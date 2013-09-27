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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.context.ContextManager;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;
import com.hp.mwtests.ts.jta.jts.common.Synchronization;


public class TransactionImpleUnitTest
{
    @Test
    public void test () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();
        
        TransactionImple tx = new TransactionImple();

        DummyXA res = new DummyXA(false);
        
        tx.enlistResource(res);
        
        tx.delistResource(res, XAResource.TMSUSPEND);
        
        assertTrue(tx.getResources() != null);      
        assertTrue(tx.getTimeout() != -1);
        
        tx.commit();
        
        assertTrue(TransactionImple.getTransactions() != null);
        
        assertEquals(TransactionImple.getTransactions().size(), 0);
        
        try
        {
            tx = (TransactionImple) TransactionManager.transactionManager(new InitialContext());
        
            fail();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testSynchronization () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();
        
        TransactionImple tx = new TransactionImple();

        tx.registerSynchronization(new Synchronization());
        
        assertTrue(tx.getSynchronizations().size() == 1);
        
        tx.setRollbackOnly();
        
        try
        {
            tx.registerSynchronization(new Synchronization());
            
            fail();
        }
        catch (final RollbackException ex)
        {
        }
        
        tx.rollback();
    }
    
    @Test
    public void testEnlist () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();
        
        TransactionImple tx = new TransactionImple();
        
        tx.setRollbackOnly();
        
        try
        {
            tx.enlistResource(null);
            
            fail();
        }
        catch (final SystemException ex)
        {
        }
        
        try
        {
            tx.enlistResource(new DummyXA(false));
            
            fail();
        }
        catch (final RollbackException ex)
        {
        }
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final RollbackException ex)
        {
        }
        
        try
        {
            tx.enlistResource(new DummyXA(false));
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        Control suspend = OTSImpleManager.current().suspend();
        
        tx = new TransactionImple();
        
        DummyXA res = new DummyXA(false);
        
        tx.enlistResource(res);
        
        tx.delistResource(res, XAResource.TMSUSPEND);
        tx.enlistResource(res);
        
        tx.commit();
        
    }
    
    @Test
    public void testDelist () throws Exception
    {
        ThreadActionData.purgeActions();
        OTSImpleManager.current().contextManager().purgeActions();
        
        
        TransactionImple tx = new TransactionImple();

        try
        {
            tx.delistResource(null, XAResource.TMSUCCESS);
            
            fail();
        }
        catch (final SystemException ex)
        {
        }

        DummyXA xares = new DummyXA(false);
        
        try
        {
            assertFalse(tx.delistResource(xares, XAResource.TMSUCCESS));
        }
        catch (final Throwable ex)
        {
            fail();
        }

        tx.enlistResource(xares);
        
        assertTrue(tx.delistResource(xares, XAResource.TMSUCCESS));
        
        tx.commit();
        
        try
        {
            tx.delistResource(xares, XAResource.TMSUCCESS);
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
    }
    
    @Test
    public void testFailure () throws Exception
    {
        ThreadActionData.purgeActions();
        OTSImpleManager.current().contextManager().purgeActions();
        
        TransactionImple tx = new TransactionImple();
        
        assertFalse(tx.equals(null));
        assertTrue(tx.equals(tx));
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit));
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final HeuristicMixedException ex)
        {
        }
        
        assertEquals(tx.getStatus(), Status.STATUS_COMMITTED);
        
        try
        {
            tx.registerSynchronization(null);
            
            fail();
        }
        catch (final SystemException ex)
        {
        }
        
        try
        {
            tx.commit();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
    }
    
    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
}
