/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
