/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface;

public class PropertyInitTest4
{
    public final static String  ORB_INSTANCE_NAME = "testorb";
    public final static String  ORB_INSTANCE_NAME_2 = "testorb2";

    @Test
    public void test()
    {
        ORB orb = null,
            orb2 = null;
        Map<String, String> testProps = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                        "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface");
        testProps.put(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                        "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface");
        testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                        "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface");
        testProps.put(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                        "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface");

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(testProps);

        try
        {
            orb = ORB.getInstance(ORB_INSTANCE_NAME);
            System.out.println("Initialising ORB("+ORB_INSTANCE_NAME+")");
            orb.initORB(new String[] {}, null);

            assertEquals(orb, PreInitialisationUsingInterface.getObject());

            assertEquals(orb, PostInitialisationUsingInterface.getObject());

            orb2 = ORB.getInstance(ORB_INSTANCE_NAME_2);
            System.out.println("Initialising ORB("+ORB_INSTANCE_NAME_2+")");
            orb2.initORB(new String[] {}, null);

            assertEquals(orb2, PreInitialisationUsingInterface.getObject());

            assertEquals(orb2, PostInitialisationUsingInterface.getObject());

        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            orb.destroy();
            orb2.destroy();
        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
            e.printStackTrace(System.err);
        }
    }
}