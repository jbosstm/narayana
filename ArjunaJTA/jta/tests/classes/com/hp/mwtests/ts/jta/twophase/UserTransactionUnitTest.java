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

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;

import static org.junit.Assert.*;


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
