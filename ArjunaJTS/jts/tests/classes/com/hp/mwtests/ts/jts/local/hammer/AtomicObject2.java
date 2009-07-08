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
 * $Id: AtomicObject2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.hammer;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

import org.junit.Test;
import static org.junit.Assert.*;

public class AtomicObject2
{
    private final static int START_VALUE_1 = 10;
    private final static int START_VALUE_2 = 101;
    private final static int EXPECTED_VALUE = START_VALUE_1 + START_VALUE_2;

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

        AtomicWorker2.init();

        AtomicWorker2.atomicObject_1 = new AtomicObject();
        AtomicWorker2.atomicObject_2 = new AtomicObject();

        System.out.println(AtomicWorker2.atomicObject_1.get_uid());
        System.out.println(AtomicWorker2.atomicObject_2.get_uid());

        assertTrue( AtomicWorker2.atomicObject_1.set(START_VALUE_1) );

        assertTrue( AtomicWorker2.atomicObject_2.set(START_VALUE_2) );

        ThreadObject2 thr1 = new ThreadObject2('1');
        ThreadObject2 thr2 = new ThreadObject2('2');

        thr1.start();
        thr2.start();

        try
        {
            thr1.join();
            thr2.join();
        }
        catch (InterruptedException e)
        {
            System.err.println(e);
        }

        AtomicWorker2.get12('m', 0);
        AtomicWorker2.get21('m', 0);

        try
        {
            int value1 = AtomicWorker2.get1();
            int value2 = AtomicWorker2.get2();

            assertEquals(EXPECTED_VALUE, (value1 + value2) );
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}
