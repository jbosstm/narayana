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

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.extensions.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

import org.omg.CosTransactions.*;

import org.omg.CORBA.IntHolder;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.INVALID_TRANSACTION;

public class SuspendResume extends Test
{

    public void run (String[] args)
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
            System.err.println("Initialisation failed: " + e);
            assertFailure();
        }

        CurrentImple current = OTSImpleManager.current();
        Control myControl = null;
        
        System.out.println("Beginning transaction.");

        try
        {
            current.begin();

            myControl = current.get_control();

            if (myControl == null)
            {
                System.err.println("Error - control is null!");
                assertFailure();
            }
        }
        catch (Exception sysEx)
        {
            sysEx.printStackTrace(System.err);
            assertFailure();
        }

        System.out.println("Committing transaction.");

        try
        {
            current.commit(true);

            current.resume(myControl);
            
            assertSuccess();
        }
        catch (Exception e)
        {
            System.err.println("commit error: " + e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Test completed successfully.");
    }

    public static void main (String[] args)
    {
        SuspendResume ct = new SuspendResume();
        ct.initialise(null, null, args, new LocalHarness());
        ct.runTest();
    }
}
