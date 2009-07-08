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
 * $Id: DistributedHammer2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.hammer;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CORBA.IntHolder;

import org.junit.Test;
import static org.junit.Assert.*;

public class DistributedHammer2
{
    private final static int   START_VALUE_1 = 10;
    private final static int   START_VALUE_2 = 101;

    private final static int   EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

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


        String server1 = "/tmp/hammer1.ref";
        String server2 = "/tmp/hammer2.ref";

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            server1 = "C:\\temp\\hammer1.ref";
            server2 = "C:\\temp\\hammer2.ref";
        }

        try
        {
            Services serv = new Services(myORB);

            DistributedHammerWorker2.hammerObject_1 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server1)));
            DistributedHammerWorker2.hammerObject_2 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server2)));

            assertTrue( DistributedHammerWorker2.hammerObject_1.set(START_VALUE_1, null) );

            assertTrue( DistributedHammerWorker2.hammerObject_2.set(START_VALUE_2, null) );

            DistributedHammerWorker2.get12('m', 0);
            DistributedHammerWorker2.get21('m', 0);
        }
        catch (Exception e)
        {
            fail("DistributedHammer2: "+e);
            e.printStackTrace(System.err);
        }

        DHThreadObject2 thr1 = new DHThreadObject2('1');
        DHThreadObject2 thr2 = new DHThreadObject2('2');

        thr1.start();
        thr2.start();

        try
        {
            thr1.join();
            thr2.join();
        }
        catch (InterruptedException e)
        {
            fail("DistributedHammer2: "+e);
            e.printStackTrace(System.err);
        }

        DistributedHammerWorker2.get12('m', 0);
        DistributedHammerWorker2.get21('m', 0);

        IntHolder value1 = new IntHolder(0);
        IntHolder value2 = new IntHolder(0);

        assertTrue( DistributedHammerWorker2.get1(value1) | DistributedHammerWorker2.get2(value2) );

        assertEquals(EXPECTED_RESULT, (value1.value + value2.value) );

        myOA.destroy();
        myORB.shutdown();
    }
}

