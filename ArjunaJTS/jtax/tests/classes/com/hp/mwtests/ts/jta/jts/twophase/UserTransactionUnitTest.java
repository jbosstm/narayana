/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import jakarta.transaction.RollbackException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.UserTransaction;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;


public class UserTransactionUnitTest
{
    @Test
    public void test () throws Exception
    {
        ThreadActionData.purgeActions();
        
        UserTransactionImple ut = new UserTransactionImple();
        
        assertTrue(ut.getTimeout() >= 0);
        assertTrue(ut.toString() != null);
        
        assertEquals(ut.getObjectInstance(null, null, null, null), ut);
        
        try
        {
            ut = (UserTransactionImple) UserTransaction.userTransaction(new InitialContext());
        
            fail();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testRollbackOnly () throws Exception
    {
        ThreadActionData.purgeActions();
        
        UserTransactionImple ut = new UserTransactionImple();
        
        ut.begin();
        
        ut.setRollbackOnly();
        
        try
        {
            ut.commit();
            
            fail();
        }
        catch (final RollbackException ex)
        {
        }
    }
    
    @Test
    public void testNull () throws Exception
    {
        ThreadActionData.purgeActions();
        
        UserTransactionImple ut = new UserTransactionImple();
        
        try
        {
            ut.commit();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            ut.rollback();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        try
        {
            ut.setRollbackOnly();
            
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