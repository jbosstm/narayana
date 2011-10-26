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

package com.hp.mwtests.ts.jta.twophase;

import java.lang.reflect.Method;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.utils.JTAHelper;
import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.RecoveryXAResource;
import com.hp.mwtests.ts.jta.common.Synchronization;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;

import static org.junit.Assert.*;

class TxImpleOverride extends TransactionImple
{
    public TxImpleOverride ()
    {
        super();
    }
    
    public static void put (TransactionImple tx)
    {
        TransactionImple.putTransaction(tx);
    }
    
    public static void remove (TransactionImple tx)
    {
        TransactionImple.removeTransaction(tx);
    }
}

class DummyXAModifier implements XAModifier
{
    @Override
    public Xid createXid (Xid xid) throws SQLException, NotImplementedException
    {
        return xid;
    }

    @Override
    public int xaStartParameters (int level) throws SQLException,
            NotImplementedException
    {
        return 0;
    }
    
}

public class TransactionImpleUnitTest
{
    @Test
    public void test () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        
        TxImpleOverride.put(tx);
        
        assertEquals(tx, TransactionImple.getTransaction(tx.get_uid()));
        
        DummyXA res = new DummyXA(false);
        
        tx.enlistResource(res);
        
        tx.delistResource(res, XAResource.TMSUSPEND);
        
        assertTrue(tx.isAlive());
        
        tx.commit();  
        
        assertTrue(tx.getRemainingTimeoutMills() != -1);
        assertTrue(tx.getTimeout() != -1);
        assertEquals(tx.getSynchronizations().size(), 0);
        assertEquals(tx.getResources().size(), 1);
        
        TxImpleOverride.remove(tx);
        
        assertTrue(TransactionImple.getTransactions() != null);
        
        assertEquals(TransactionImple.getTransaction(tx.get_uid()), null);
        
        try
        {
            tx = (TransactionImple) TransactionManager.transactionManager(new InitialContext());
        
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        assertNull(TransactionImple.getTransaction(null));
    }
    
    @Test
    public void testThreadIsActive () throws Exception
    {
        ThreadActionData.purgeActions();
        
        Class[] parameterTypes = new Class[1];
        TransactionImple tx = new TransactionImple(0);

        tx.enlistResource(new RecoveryXAResource());
        
        parameterTypes[0] = XAResource.class;
        
        Method m = tx.getClass().getDeclaredMethod("threadIsActive", parameterTypes);
        m.setAccessible(true);
        
        Object[] parameters = new Object[1];
        parameters[0] = new RecoveryXAResource();
        
        Boolean res = (Boolean) m.invoke(tx, parameters);
        
        assertFalse(res.booleanValue());
        
        tx.rollback();
        ThreadActionData.purgeActions();
    }
    
    @Test
    public void testXidCreation () throws Exception
    {
        Class[] parameterTypes = new Class[3];
        TransactionImple tx = new TransactionImple(0);
        
        parameterTypes[0] = boolean.class;
        parameterTypes[1] = XAModifier.class;
        parameterTypes[2] = XAResource.class;
        
        Method m = tx.getClass().getDeclaredMethod("createXid", parameterTypes);
        m.setAccessible(true);
        
        Object[] parameters = new Object[3];
        
        parameters[0] = false;
        parameters[1] = new DummyXAModifier();
        parameters[2] = new DummyXA(false);
        
        Xid res = (Xid) m.invoke(tx, parameters);
        
        assertTrue(res != null);
        
        tx.rollback();
    }
    
    @Test
    public void testEnlist () throws Exception
    {
        ThreadActionData.purgeActions();
        
        TransactionImple tx = new TransactionImple(0);
        
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
    }
    
    @Test
    public void testDelist () throws Exception
    {
        ThreadActionData.purgeActions();
        
        TransactionImple tx = new TransactionImple(0);

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
        
        TransactionImple tx = new TransactionImple(0);
        
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
    
    @Test
    public void testInvalid () throws Exception
    {
        ThreadActionData.purgeActions();
        
        TxImpleOverride tx = new TxImpleOverride();
        
        assertEquals(tx.hashCode(), -1);
        assertEquals(tx.getStatus(), Status.STATUS_NO_TRANSACTION);
        
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
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            tx.setRollbackOnly();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            tx.registerSynchronization(new Synchronization());
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        tx.toString();
        
        assertFalse(tx.isAlive());
    }
}
