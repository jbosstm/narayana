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
 * (C) 2005-2010,
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.jta.utils;

import org.junit.Test;

import com.arjuna.ats.jta.utils.JNDIManager;

import static org.junit.Assert.*;

public class JNDIManagerUnitTest
{
    @Test
    public void testBind () throws Exception
    {
        try
        {
            JNDIManager.bindJTAImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTAImplementations(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTATransactionManagerImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTATransactionManagerImplementation(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTATransactionSynchronizationRegistryImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTATransactionSynchronizationRegistryImplementation(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTAUserTransactionImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.bindJTAUserTransactionImplementation(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
    }
    
    @Test
    public void testUnbind () throws Exception
    {
        try
        {
            JNDIManager.unbindJTATransactionManagerImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.unbindJTATransactionManagerImplementation(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.unbindJTATransactionSynchronizationRegistryImplementation();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            JNDIManager.unbindJTATransactionSynchronizationRegistryImplementation(null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
    }
}
