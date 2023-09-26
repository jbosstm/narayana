/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.local.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.Terminator;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.grid_i;
import com.hp.mwtests.ts.jts.resources.TransactionalThread;

public class GridTest
{
    @Test
    public void test()
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            TransactionFactoryImple theOTS = new TransactionFactoryImple();
            Control myControl;
            grid_i localGrid = new grid_i(100, 100);
            int h, w, v;

            myControl = theOTS.create(0);

            assertNotNull( myControl );
            
            h = localGrid.height();
            w = localGrid.width();

            localGrid.set( 2, 4, 123, myControl);
            v = localGrid.get(2, 4, myControl);

            // no problem setting and getting the elememt:

            System.out.println("grid[2,4] is "+v);

            assertEquals(123, v);

                Terminator handle = myControl.get_terminator();

                try
                {
                    if (handle != null)
                    {
                        handle.commit(false);
                    }
                    else
                        System.err.println("Error - no transaction terminator!");
                }
                catch (Exception ex)
                {
                    System.out.println("Test error! Caught: "+ex);
                }


            ORBManager.getPOA().shutdownObject(theOTS);
            ORBManager.getPOA().shutdownObject(localGrid);
        }
        catch (UserException e)
        {
            fail("Caught UserException: "+e);

            e.printStackTrace();
        }
        catch (SystemException e)
        {
            fail("Caught SystemException: "+e);

            e.printStackTrace();
        }

        System.out.println("\nWill now try different thread terminating transaction.\n");

        try
        {
            org.omg.CosTransactions.Current current = OTSManager.get_current();

            System.out.println("Starting new transaction.");

            current.begin();

            Control tc = current.get_control();

            if (tc != null)
            {
                System.out.println("Creating new thread.");

                TransactionalThread tranThread = new TransactionalThread(tc);

                System.out.println("Waiting for thread to terminate transaction.\n");

                tranThread.start();

                while (!tranThread.finished())
                    Thread.yield();

                System.out.println("\nCreator will now attempt to rollback transaction. Should fail.");

                try
                {
                    current.rollback();

                    fail("Error - managed to rollback transaction!");
                }
                catch (NoTransaction e1)
                {
                    System.out.println("Correct termination - caught: "+e1);
                }
                catch (INVALID_TRANSACTION e2)
                {
                    System.out.println("Correct termination - caught: "+e2);
                }
                catch (Exception e3)
                {
                    fail("Wrong termination - caught unexpected exception: "+e3);
                    e3.printStackTrace();
                }

                System.out.println("Test completed successfully.");
            }
            else
                System.err.println("Error - null transaction control!");
        }
        catch (Exception e)
        {
            System.out.println("Caught unexpected exception: "+e);
        }

        myOA.destroy();
        myORB.shutdown();
    }
}