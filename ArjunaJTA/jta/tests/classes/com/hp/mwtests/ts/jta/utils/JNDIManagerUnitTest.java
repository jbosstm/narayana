/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.utils;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.jta.utils.JNDIManager;

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