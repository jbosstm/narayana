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
 * $Id: GridClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.grid;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;
import com.hp.mwtests.ts.jts.TestModule.grid;
import com.hp.mwtests.ts.jts.TestModule.gridHelper;

import org.omg.CosTransactions.*;

import org.junit.Test;
import static org.junit.Assert.*;

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

