/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: AtomicObject3.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.hammer;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;
import org.jboss.dtf.testframework.unittest.Test;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

import java.util.Random;

public class AtomicObject3 extends Test
{
    private final static int START_VALUE_1 = 10;
    private final static int START_VALUE_2 = 101;
    private final static int EXPECTED_VALUE = START_VALUE_1 + START_VALUE_2;

    public void run(String[] args)
    {
	ORB myORB = null;
	RootOA myOA = null;

	try
	{
	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);

	    myORB.initORB(args, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);
	}
	catch (Exception e)
	{
	    System.err.println("Initialisation failed: "+e);
            e.printStackTrace(System.err);
            assertFailure();
	}

	AtomicWorker3.init();

	AtomicWorker3.atomicObject_1 = new AtomicObject();
	AtomicWorker3.atomicObject_2 = new AtomicObject();

	System.out.println(AtomicWorker3.atomicObject_1.get_uid());
	System.out.println(AtomicWorker3.atomicObject_2.get_uid());

	if (!AtomicWorker3.atomicObject_1.set(START_VALUE_1))
        {
	    System.out.println("m set1 : failed");
            assertFailure();
        }

	if (!AtomicWorker3.atomicObject_2.set(START_VALUE_2))
        {
	    System.out.println("m set2 : failed");
            assertFailure();
        }

	Thread thr1 = new ThreadObject3b(1);
	Thread thr2 = new ThreadObject3b(2);

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
            assertFailure();
	}

	AtomicWorker3.get12(0, 0);
	AtomicWorker3.get21(0, 0);

        try
        {
            int value1 = AtomicWorker3.get1();
            int value2 = AtomicWorker3.get2();

            if ( ( value1 + value2 ) != EXPECTED_VALUE )
            {
                logInformation(value1+" "+value2+" incorrect values");
                assertFailure();
            }
            else
            {
                logInformation(value1+" "+value2+" correct values");
                assertSuccess();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            assertFailure();
        }

	myOA.destroy();
	myORB.shutdown();
    }

    public static void main(String[] args)
    {
	AtomicObject3 oa = new AtomicObject3();

	oa.run(args);
    }

}

