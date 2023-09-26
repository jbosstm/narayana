/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.concurrencycontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.txoj.lockstore.BasicPersistentLockStore;
import com.arjuna.ats.txoj.LockResult;
import com.arjuna.ats.txoj.common.txojPropertyManager;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObjectLockStore;

public class LockStoreUnitTest
{
    @Test
    public void testBasic () throws Throwable
    {
        txojPropertyManager.getTxojEnvironmentBean().setLockStoreType(BasicPersistentLockStore.class.getName());
        
        AtomicObjectLockStore obj = new AtomicObjectLockStore(ObjectModel.MULTIPLE);
        
        obj.set(0);
        obj.incr(1);
        
        assertEquals(obj.get(), 1);

        assertTrue(obj.getLockStore().getClass().getName().equals(BasicPersistentLockStore.class.getName()));
    }
    
    /*
     * This is not meant to be driven by the normal unit test process.
     * 
     * To run, fire up two shells and run the first one with this test and with no parameters. Take the Uid
     * that is printed out and use it in the second shell with the same test and the -uid option.
     * 
     * Optionally provide a value through the -val parameter on one or both of the shells.
     * 
     * If you are fast enough then you should see something like ...
     * 
     * Error recreating object 0:ffffc0a8001f:ec14:51571c30:0
     *   com.hp.mwtests.ts.txoj.common.exceptions.TestException: Write lock error.
     *   at com.hp.mwtests.ts.txoj.common.resources.AtomicObject.set(AtomicObject.java:182)
     *   at com.hp.mwtests.ts.txoj.concurrencycontrol.LockStoreUnitTest.main(LockStoreUnitTest.java:141)
     *   
     * This shows concurrency control being applied across two different instances of the same object running
     * in different address spaces.
     */
    
    public static void main (String[] args)
    {
        Uid u = null;
        AtomicObjectLockStore obj = null;
        int value = 10;
        
        txojPropertyManager.getTxojEnvironmentBean().setLockStoreType(BasicPersistentLockStore.class.getName());
        
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-help"))
            {
                System.out.println("LockStoreUnitTest [-help] [-uid <uid>] [-val <integer>]");
                
                return;
            }
            
            if (args[i].equals("-uid"))
            {
                try
                {
                    u = new Uid(args[i+1]);
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                    
                    return;
                }
            }
            
            if (args[i].equals("-val"))
            {
                try
                {
                    value = Integer.parseInt(args[i+1]);
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                    
                    return;
                }
            }
        }
        
        if (u == null)
        {
            obj = new AtomicObjectLockStore(ObjectModel.MULTIPLE);
            
            System.out.println("Object created: "+obj.get_uid());
            
            /*
             * Now sleep and give a chance to create another shell instance.
             */
            
            try
            {
                Thread.sleep(10000);
            }
            catch (final Exception ex)
            {
            }
        }
        else
            obj = new AtomicObjectLockStore(u, ObjectModel.MULTIPLE);
        
        try
        {
            AtomicAction A = new AtomicAction();
            
            A.begin();
            
            obj.set(value);
            
            try
            {
                Thread.sleep(10000);
            }
            catch (final Exception ex)
            {
            }
            
            A.commit();
            
            System.out.println("Value: "+obj.get());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
}