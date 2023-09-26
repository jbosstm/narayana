/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.transactionserver;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;
import com.hp.mwtests.ts.jts.orbspecific.resources.DistributedHammerWorker1;

public class TMClient
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
        Control topLevelControl = null;
        Control nestedControl = null;
        String server = "/tmp/hammer1.ref";
        boolean slave = false;

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            server = "C:\\temp\\hammer1.ref";
        }

        Services serv = new Services(myORB);

        int resolver = Services.getResolver();

        try
        {
            String[] params = new String[1];

            params[0] = Services.otsKind;

            org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params, resolver);

            params = null;
            theOTS = TransactionFactoryHelper.narrow(obj);
        }
        catch (Exception e)
        {
            fail("Unexpected bind exception: "+e);
            e.printStackTrace(System.err);
        }

        System.out.println("Creating transaction.");

        try
        {
            topLevelControl = theOTS.create(0);
        }
        catch (Exception e)
        {
            fail("Create call failed: "+e);
            e.printStackTrace(System.err);
        }

        System.out.println("Creating subtransaction.");

        try
        {
            nestedControl = topLevelControl.get_coordinator().create_subtransaction();
        }
        catch (Exception e)
        {
            System.err.println("Subtransaction create call failed: "+e);

            try
            {
                topLevelControl.get_terminator().rollback();
            }
            catch (Exception ex)
            {
            }

            e.printStackTrace(System.err);
            fail();
        }

        try
        {
            DistributedHammerWorker1.hammerObject_1 = HammerHelper.narrow(serv.getService(server, null, Services.FILE));

            if (!DistributedHammerWorker1.hammerObject_1.incr(1, nestedControl))
                System.out.println("Could not increment!");
            else
                System.out.println("incremented.");

            System.out.println("sleeping.");

            Thread.sleep(20000);

            nestedControl.get_terminator().rollback();

            if (!slave)
            {
                System.out.println("controller sleeping again.");

                Thread.sleep(20000);
            }

            IntHolder value = new IntHolder(0);

            org.omg.CosTransactions.PropagationContext ctx = topLevelControl.get_coordinator().get_txcontext();

            assertTrue( DistributedHammerWorker1.hammerObject_1.get(value, topLevelControl) );

            topLevelControl.get_terminator().rollback();
        }
        catch (Exception e)
        {
            fail("TMClient: "+e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();
    }
}