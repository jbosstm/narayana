/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * $Id: TMClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.transactionserver;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;

import com.arjuna.ats.jts.common.jtsPropertyManager;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.junit.Test;
import static org.junit.Assert.*;

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

        int resolver = com.arjuna.orbportability.common.Configuration.bindDefault();
        String resolveService = jtsPropertyManager.getPropertyManager().getProperty(com.arjuna.orbportability.common.Environment.RESOLVE_SERVICE);

        if (resolveService != null)
        {
            if (resolveService.compareTo("NAME_SERVICE") == 0)
                resolver = com.arjuna.orbportability.Services.NAME_SERVICE;
            else
            {
                if (resolveService.compareTo("BIND_CONNECT") == 0)
                    resolver = com.arjuna.orbportability.Services.BIND_CONNECT;
                else
                {
                    if (resolveService.compareTo("FILE") == 0)
                        resolver = com.arjuna.orbportability.Services.FILE;
                    else
                    {
                        if (resolveService.compareTo("RESOLVE_INITIAL_REFERENCES") == 0)
                            resolver = com.arjuna.orbportability.Services.RESOLVE_INITIAL_REFERENCES;
                    }
                }
            }
        }

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
                System.out.println("master sleeping again.");

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
