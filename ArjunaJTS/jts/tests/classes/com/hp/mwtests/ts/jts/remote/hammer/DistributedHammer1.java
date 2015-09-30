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
 * $Id: DistributedHammer1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.hammer;

import org.omg.CORBA.IntHolder;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.HammerHelper;
import com.hp.mwtests.ts.jts.orbspecific.resources.DistributedHammerWorker1;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class DistributedHammer1
{
    private final static int   START_VALUE_1 = 10;
    private final static int   START_VALUE_2 = 101;

    private final static int   EXPECTED_RESULT = START_VALUE_1 + START_VALUE_2;

    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String server1 = args[0];
        String server2 = args[1];

        try
        {
            Services serv = new Services(myORB);

            DistributedHammerWorker1.hammerObject_1 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server1)));
            DistributedHammerWorker1.hammerObject_2 = HammerHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(server2)));

            TestUtility.assertTrue( DistributedHammerWorker1.hammerObject_1.set(START_VALUE_1, null) );

            TestUtility.assertTrue( DistributedHammerWorker1.hammerObject_2.set(START_VALUE_2, null) );

            DistributedHammerWorker1.get12('m', 0);
            DistributedHammerWorker1.get21('m', 0);
        }
        catch (Exception e)
        {
            TestUtility.fail("DistributedHammer1: "+e);
            e.printStackTrace(System.err);
        }

        for (int i = 0; i < 100; i++)
            DistributedHammerWorker1.randomOperation('1', 0);

        DistributedHammerWorker1.get12('m', 0);
        DistributedHammerWorker1.get21('m', 0);

        IntHolder value1 = new IntHolder(0);
        IntHolder value2 = new IntHolder(0);

        TestUtility.assertTrue( DistributedHammerWorker1.get1(value1) | DistributedHammerWorker1.get2(value2) );

        TestUtility.assertEquals(EXPECTED_RESULT, (value1.value + value2.value) );

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}

