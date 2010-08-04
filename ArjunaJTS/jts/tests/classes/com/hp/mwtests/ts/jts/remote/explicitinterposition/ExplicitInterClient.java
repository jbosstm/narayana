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
 * $Id: ExplicitInterClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.explicitinterposition;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.TestModule.SetGet;
import com.hp.mwtests.ts.jts.TestModule.SetGetHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

import org.omg.CosTransactions.*;

public class ExplicitInterClient
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


        CurrentImple current = OTSImpleManager.current();
        Control theControl = null;

        String objectReference = args[0];

        SetGet SetGetVar = null;
        short h = 0;

        try
        {
            current.begin();
            current.begin();
            current.begin();
        }
        catch (Exception e)
        {
            TestUtility.fail("Caught exception during begin: "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            Services serv = new Services(myORB);

            SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(objectReference)));
        }
        catch (Exception ex)
        {
            TestUtility.fail("Failed to bind to setget server: "+ex);
            ex.printStackTrace(System.err);
        }

        try
        {
            theControl = current.get_control();

            SetGetVar.set((short) 2, theControl);
            //	    SetGetVar.set((short) 2, theControl);

            theControl = null;

            System.out.println("Set value.");
        }
        catch (Exception ex1)
        {
            TestUtility.fail("Unexpected system exception during set: "+ex1);
            ex1.printStackTrace(System.err);
        }

        try
        {
            System.out.println("committing first nested action");

            current.commit(true);

            //	    SetGetVar.set((short) 4, current.get_control());

            System.out.println("committing second nested action");

            current.commit(true);
        }
        catch (Exception sysEx)
        {
            TestUtility.fail("Caught unexpected exception during commit: "+sysEx);
            sysEx.printStackTrace(System.err);
        }

        try
        {
            theControl = current.get_control();

            h = SetGetVar.get(theControl);

            theControl = null;

            System.out.println("Got value.");
        }
        catch (Exception ex2)
        {
            TestUtility.fail("Unexpected system exception during get: "+ex2);
            ex2.printStackTrace(System.err);
        }

        try
        {
            current.commit(true);

            System.out.println("committed top-level action");
        }
        catch (Exception ep)
        {
            TestUtility.fail("Caught commit exception for top-level action: "+ep);
            ep.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}
