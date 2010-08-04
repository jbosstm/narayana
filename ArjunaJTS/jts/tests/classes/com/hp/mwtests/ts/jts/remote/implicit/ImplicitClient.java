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
 * $Id: ImplicitClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.implicit;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.TestModule.TranGrid;
import com.hp.mwtests.ts.jts.TestModule.TranGridHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class ImplicitClient
{
    public static void main(String[] args) throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        String refFile = args[0];

        CurrentImple current = OTSImpleManager.current();

        TranGrid TranGridVar = null;   // pointer the grid object that will be used.
        short h = 0, w = 0, v = 0;

        try
        {
            current.begin();

            Services serv = new Services(myORB);
            TranGridVar = TranGridHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(refFile)));

            try
            {
                h = TranGridVar.height();
                w = TranGridVar.width();
            }
            catch (Exception e)
            {
                TestUtility.fail("Invocation failed: "+e);
            }

            System.out.println("height is "+h);
            System.out.println("width  is "+w);

            try
            {
                System.out.println("calling set");

                TranGridVar.set((short) 2, (short) 4, (short) 123);

                System.out.println("calling get");

                v = TranGridVar.get((short) 2, (short) 4);
            }
            catch (Exception sysEx)
            {
                TestUtility.fail("Grid set/get failed: "+sysEx);
                sysEx.printStackTrace(System.err);
            }

            // no problem setting and getting the element:

            System.out.println("trangrid[2,4] is "+v);

            // sanity check: make sure we got the value 123 back:

            if (v != 123)
            {
                // oops - we didn't:

                current.rollback();
                TestUtility.fail("Result not as expected");
            }
            else
            {
                current.commit(true);
            }
        }
        catch (Exception e)
        {
            TestUtility.fail("Caught exception: "+e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}

