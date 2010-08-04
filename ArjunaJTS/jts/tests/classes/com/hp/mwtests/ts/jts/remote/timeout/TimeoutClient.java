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
 * $Id: TimeoutClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.timeout;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.TestModule.SetGet;
import com.hp.mwtests.ts.jts.TestModule.SetGetHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

import org.omg.CosTransactions.*;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class TimeoutClient
{
    public static void main(String[] args) throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            CurrentImple current = OTSImpleManager.current();
            Control theControl = null;

            String objectReference = args[0];

            SetGet SetGetVar = null;

            System.out.println("Setting transaction timeout to 2 seconds.");

            current.set_timeout(2);

            current.begin();
            current.begin();

            try
            {
                Services serv = new Services(myORB);

                SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(objectReference)));
            }
            catch (Exception e)
            {
                TestUtility.fail("Bind to object failed: "+e);
                e.printStackTrace(System.err);
            }

            try
            {
                theControl = current.get_control();

                SetGetVar.set((short) 2, theControl);

                theControl = null;

                System.out.println("Set value.");
            }
            catch (Exception e)
            {
                TestUtility.fail("Call to set or get failed: "+e);
                e.printStackTrace(System.err);
            }

            try
            {
                System.out.println("Now sleeping for 5 seconds.");

                Thread.sleep(5000);
            }
            catch (Exception e)
            {
            }

            System.out.println("\ncommitting nested action.");

            try
            {
                current.commit(true);
                TestUtility.fail("commit worked");
            }
            catch (TRANSACTION_ROLLEDBACK  e1)
            {
                System.out.println("Caught TransactionRolledBack");
            }
            catch (INVALID_TRANSACTION  e1)	/* For JacORB */
            {
                System.out.println("Caught InvalidTransaction");
            }

            System.out.println("\ncommitting top-level action");

            try
            {
                current.commit(true);
                TestUtility.fail("commit worked");
            }
            catch (TRANSACTION_ROLLEDBACK  e2)
            {
                System.out.println("Caught TransactionRolledBack");
            }
            catch (INVALID_TRANSACTION  e3)
            {
                System.out.println("Caught InvalidTransaction");
            }
            catch (Exception e)
            {
                TestUtility.fail("Caught other exception: "+e);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            TestUtility.fail(e.toString());
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}
