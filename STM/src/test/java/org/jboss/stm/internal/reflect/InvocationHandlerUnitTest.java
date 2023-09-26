/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.reflect;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class InvocationHandlerUnitTest extends TestCase
{
    public static final int STORE_NEW = 0;
    public static final int STORE_FAIL = -1;
    public static final int STORE_INIT = 1;
    
    private int createStoreManager ()
    {
        int result = STORE_NEW;
        
        try
        {
            _storeManager = new StoreManager(null, new TwoPhaseVolatileStore(new ObjectStoreEnvironmentBean()), null);
        }              
        catch (final IllegalStateException ex)
        {
            // means store already initialised so check to see if compatible

            if (StoreManager.setupStore(null, ObjectModel.SINGLE).getClass().equals(TwoPhaseVolatileStore.class))
            {               
                // do nothing, as we are ok
                
                result = STORE_INIT;
            }
            else
            {
                ex.printStackTrace();
                
                result = STORE_FAIL;
            }
        }
        catch (final Throwable ex)
        {
            System.err.println("InvocationHandler could not initialise object store for optimistic concurrency control.");
            
            ex.printStackTrace();
            
            result = STORE_FAIL;
        }
    
        return result;
    }
    
    public void test ()
    {
        assertEquals(createStoreManager(), STORE_NEW);  // first time should always work!
        
        assertEquals(createStoreManager(), STORE_INIT);
    }
    
    private static StoreManager _storeManager;
}