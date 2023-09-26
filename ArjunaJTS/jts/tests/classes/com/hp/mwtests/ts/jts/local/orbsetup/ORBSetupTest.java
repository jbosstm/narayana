/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jts.local.orbsetup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PostInitLoader;

public class ORBSetupTest
{
    private final static String ORB_NAME = "testorb";

    @Test
    public void test()
    {
        boolean staticSet = false;
        ORB myORB = null;
        RootOA myOA = null;

        Map<String, String> properties = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        properties.put( PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_NAME), "com.arjuna.ats.jts.utils.ORBSetup");

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(properties);

        try
        {
            myORB = ORB.getInstance(ORB_NAME);
            myOA = OA.getRootOA(myORB);

            if (staticSet)
            {
                ORBManager.setORB(myORB);
            }

            try
            {
                myORB.initORB(new String[] {}, null);
                myOA.initOA();

                assertEquals(myORB, ORBManager.getORB());
            }
            catch (FatalError e)
            {
                if (staticSet)
                {
                    System.out.println("FatalError thrown as expected");
                }
                else
                {
                    fail("Error: "+e);
                    e.printStackTrace(System.err);
                }
            }

            myOA.destroy();
            myORB.destroy();
        }
        catch (Throwable e)
        {
            fail("Error: "+e);
            e.printStackTrace(System.err);
        }
    }
}