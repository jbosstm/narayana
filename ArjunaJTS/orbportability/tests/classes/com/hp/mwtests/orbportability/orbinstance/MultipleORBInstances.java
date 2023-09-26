/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.orbinstance;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class MultipleORBInstances
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
                System.out.println("Failure - unqiue ORB instance not created!");
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
            System.out.println("Could not create ORB instance");
            returnValue = false;
        }
        else
        {
            System.out.println("Successfully created ORB instance");

            if (oaInstance == null)
            {
                System.out.println("Could not create OA instance");
                returnValue = false;
            }
            else
            {
                System.out.println("Successfully created OA instance");

                try
                {
                    orbInstance.initORB(args,null);
                    oaInstance.init();
                }
                catch (Exception e)
                {
                    System.out.println("Unexpected Exception while initialising the orb instance - "+e);
                    e.printStackTrace(System.err);
                    returnValue = false;
                }
            }
        }

        return(returnValue);
    }

    @Test
    public void test()
    {
        ORB orbInstance[] = new ORB[NUMBER_OF_ORBOAS];
        RootOA oaInstance[] = new RootOA[NUMBER_OF_ORBOAS];

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            System.out.println("Creating ORB and OA #"+count);
            orbInstance[count] = ORB.getInstance("orb_"+count);

            oaInstance[count] = RootOA.getRootOA(orbInstance[count]);

            assertTrue( initialiseORBandOA(new String[] {},orbInstance[count],oaInstance[count]) );
        }

        assertTrue( ensureAllORBReferencesAreUnique(orbInstance, oaInstance) );

        System.out.println("Retrieving all ORBs and OAs");

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            System.out.println("Retrieving ORB and OA #"+count);
            ORB orb = ORB.getInstance("orb_"+count);

            assertNotNull(orb);

            assertNotNull( OA.getRootOA(orb) );            
        }

        System.out.println("Destroying all ORBs and OAs");

        for (int count=0;count<NUMBER_OF_ORBOAS;count++)
        {
            orbInstance[count].destroy();
            oaInstance[count].destroy();
        }
    }
}