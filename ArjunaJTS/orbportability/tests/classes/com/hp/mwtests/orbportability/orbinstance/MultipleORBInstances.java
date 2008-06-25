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
//
// Copyright (C) 2002,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: MultipleORBInstances.java 2342 2006-03-30 13:06:17Z  $

package com.hp.mwtests.orbportability.orbinstance;

import org.jboss.dtf.testframework.unittest.Test;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.RootOA;

import java.util.HashSet;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;

public class MultipleORBInstances extends Test
{
    public static final int NUMBER_OF_ORBOAS = 10;

    public boolean ensureAllORBReferencesAreUnique(ORB[] orbs, OA[] oas)
    {
        boolean success = true;

        HashSet orbSet = new HashSet();

        for (int count=0;count<orbs.length;count++)
        {
            if (!orbSet.contains(orbs[count].orb()))
            {
                orbSet.add(orbs[count].orb());
            }
            else
            {
                logInformation("Failure - unqiue ORB instance not created!");
                success = false;
            }
        }

        return(success);
    }

    public boolean initialiseORBandOA(String[] args, ORB orbInstance, OA oaInstance)
    {
        boolean returnValue = true;

        if (orbInstance == null)
        {
            logInformation("Could not create ORB instance");
            returnValue = false;
        }
        else
        {
            logInformation("Successfully created ORB instance");

            if (oaInstance == null)
            {
                logInformation("Could not create OA instance");
                returnValue = false;
            }
            else
            {
                logInformation("Successfully created OA instance");

                try
                {
                    orbInstance.initORB(args,null);
                    oaInstance.init();
                }
                catch (Exception e)
                {
                    logInformation("Unexpected Exception while initialising the orb instance - "+e);
                    e.printStackTrace(System.err);
                    returnValue = false;
                }
            }
        }

        return(returnValue);
    }

    public void run(String[] args)
    {
        ORB orbInstance[] = new ORB[NUMBER_OF_ORBOAS];
        RootOA oaInstance[] = new RootOA[NUMBER_OF_ORBOAS];

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            logInformation("Creating ORB and OA #"+count);
            orbInstance[count] = ORB.getInstance("orb_"+count);

            oaInstance[count] = RootOA.getRootOA(orbInstance[count]);

            if (!initialiseORBandOA(args,orbInstance[count],oaInstance[count]))
            {
                logInformation("Failed to create ORB and OA #"+count);
                assertFailure();
            }
        }

        if (!ensureAllORBReferencesAreUnique(orbInstance, oaInstance))
        {
            assertFailure();
        }

        logInformation("Retrieving all ORBs and OAs");

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            logInformation("Retrieving ORB and OA #"+count);
            ORB orb = ORB.getInstance("orb_"+count);

            if ( orb == null)
            {
                logInformation("Failed to retrieve ORB #"+count);
                assertFailure();
            }

            if ( OA.getRootOA(orb) == null )
            {
                logInformation("Failed to retrieve OA #"+count);
                assertFailure();
            }
        }

        logInformation("Destroying all ORBs and OAs");

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            orbInstance[count].destroy();
            oaInstance[count].destroy();
        }

        assertSuccess();
    }

    public static void main(String[] args)
    {
	MultipleORBInstances test = new MultipleORBInstances();

	test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());

	test.runTest();
    }

}
