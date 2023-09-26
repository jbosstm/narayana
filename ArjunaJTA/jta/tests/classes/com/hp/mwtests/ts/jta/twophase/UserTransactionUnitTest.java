/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.jta.UserTransaction;


public class UserTransactionUnitTest
{
    @Test
    public void test () throws Exception
    {
        ThreadActionData.purgeActions();
        
        UserTransactionImple ut = new UserTransactionImple();
        
        assertEquals(ut.getTimeout(), 0);
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
    public void testSubordinate () throws Exception
    {
        ThreadActionData.purgeActions();
        
        UserTransactionImple ut = new UserTransactionImple();
        
        assertTrue(ut.createSubordinate() != null);
        
        ut.begin();
        
        try
        {
            ut.createSubordinate();
            
            fail();
        }
        catch (final NotSupportedException ex)
        {
        }
        
        ut.commit();
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
}