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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CurrentTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.current;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.TestModule.grid;
import com.hp.mwtests.ts.jts.TestModule.gridHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

import org.omg.CosTransactions.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class CurrentTest
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


        CurrentImple current = OTSImpleManager.current();
        Control myControl = null;
        String gridReference = "/tmp/grid.ref";
        String serverName = "Grid";
        grid gridVar = null;  // pointer the grid object that will be used.
        int h = -1, w = -1, v = -1;

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            gridReference = "C:\\temp\\grid.ref";
        }

        System.out.println("Beginning transaction.");

        try
        {
            current.begin();

            myControl = current.get_control();

            assertNotNull(myControl);
        }
        catch (Exception sysEx)
        {
            sysEx.printStackTrace(System.err);
            fail();
        }

        try
        {
            Services serv = new Services(myORB);

            gridVar = gridHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(gridReference)));
        }
        catch (Exception sysEx)
        {
            fail("failed to bind to grid: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        try
        {
            h = gridVar.height();
            w = gridVar.width();
        }
        catch (Exception sysEx)
        {
            fail("grid height/width failed: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        System.out.println("height is "+h);
        System.out.println("width  is "+w);

        try
        {
            gridVar.set(2, 4, 123, myControl);
            v = gridVar.get(2, 4, myControl);
        }
        catch (Exception sysEx)
        {
            fail("grid set/get failed: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        // no problem setting and getting the elememt:
        System.out.println("grid[2,4] is "+v);

        // sanity check: make sure we got the value 123 back:
        if (v != 123)
        {
            fail();
            System.err.println("something went seriously wrong");

            try
            {
                current.rollback();
            }
            catch (Exception e)
            {
                fail("rollback error: "+e);
                e.printStackTrace(System.err);
            }

            fail();
        }
        else
        {
            System.out.println("Committing transaction.");

            try
            {
                current.commit(true);
            }
            catch (Exception e)
            {
                fail("commit error: "+e);
                e.printStackTrace(System.err);
            }

            myOA.destroy();
            myORB.shutdown();

            System.out.println("Test completed successfully.");
        }
    }
}

