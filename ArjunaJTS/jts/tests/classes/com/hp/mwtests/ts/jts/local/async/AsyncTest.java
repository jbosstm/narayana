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
 * $Id: AsyncTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.async;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CosTransactions.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class AsyncTest
{
    @Test
    public void test() throws Exception
    {
        boolean errorp = false;
        boolean errorc = false;

        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        try {

            Current current = OTSManager.get_current();

            DemoResource.printThread = true;

            current.begin();

            for (int j = 0; j < 100; j++)
            {
                if ((j == 10) && (errorp || errorc))
                {
                    boolean heuristicPrepare = errorp;
                    heuristic h = new heuristic(heuristicPrepare);

                    current.get_control().get_coordinator().register_resource(h.getReference());

                    h = null;
                }

                DemoResource r = new DemoResource();

                r.registerResource();

                r = null;
            }

            System.out.println("committing top-level transaction");
            current.commit(false);

            System.out.println("Test completed.");
        }
        catch (org.omg.CORBA.TRANSACTION_ROLLEDBACK e)
        {
            System.out.println("Caught exception: "+e);

            assertTrue(errorp || errorc);

        }

        myOA.destroy();
        myORB.shutdown();
        
    }
}