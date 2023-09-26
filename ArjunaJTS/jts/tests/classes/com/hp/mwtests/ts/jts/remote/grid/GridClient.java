/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.grid;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.grid;
import com.hp.mwtests.ts.jts.TestModule.gridHelper;

public class GridClient
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);


        TransactionFactory theOTS = null;
        Control myControl = null;
        grid gridVar = null;
        int h = -1, w = -1, v = -1;
        String gridReference = "/tmp/grid.ref";
        String serverName = "Grid";

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            gridReference = "C:\\temp\\grid.ref";
        }

        Services serv = new Services(myORB);

        try
        {
            String[] params = new String[1];

            params[0] = Services.otsKind;

            org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params);

            params = null;
            theOTS =  TransactionFactoryHelper.narrow(obj);
        }
        catch (Exception e)
        {
            fail("Unexpected bind exception: "+e);
        }

        System.out.println("Creating transaction.");

        try
        {
            myControl = theOTS.create(0);
        }
        catch (Exception e)
        {
            fail("Create call failed: "+e);
            e.printStackTrace();
        }

        try
        {
            gridVar = gridHelper.narrow(serv.getService(gridReference, null, Services.FILE));
        }
        catch (Exception e)
        {
            fail("Grid bind failed: "+e);
        }

        try
        {
            h = gridVar.height();
            w = gridVar.width();
        }
        catch (Exception e)
        {
            fail("Grid invocation failed: "+e);
        }

        System.out.println("height is "+h);
        System.out.println("width  is "+w);

        try
        {
            System.out.println("calling set");

            gridVar.set(2, 4, 123, myControl);

            System.out.println("calling get");

            v = gridVar.get(2, 4, myControl);
        }
        catch (Exception sysEx)
        {
            fail("Grid set/get failed: "+sysEx);
        }

        // no problem setting and getting the elememt:
        System.out.println("grid[2,4] is "+v);

        // sanity check: make sure we got the value 123 back:
        if (v != 123)
        {
            // oops - we didn't:
            fail("something went seriously wrong");

            try
            {
                myControl.get_terminator().rollback();
            }
            catch (Exception e)
            {
            }
        }
        else
        {
            System.out.println("Committing transaction.");

            try
            {
                Terminator handle = myControl.get_terminator();

                handle.commit(true);
            }
            catch (Exception sysEx)
            {
                fail("Transaction commit error: "+sysEx);
            }
        }

        /*
       * OTSArjuna specific call to tell the system
       * that we are finished with this transaction.
       */

        try
        {
            OTSManager.destroyControl(myControl);
        }
        catch (Exception e)
        {
            fail("Caught destroy exception: "+e);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Test completed successfully.");
    }
}