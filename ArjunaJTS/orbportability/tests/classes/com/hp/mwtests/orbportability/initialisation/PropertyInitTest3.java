/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2;
import com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2;

/**
 * Tests that ORB pre/post-initialisation properties registered via the
 * ORB portability environment bean are correctly invoked per ORB instance.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class PropertyInitTest3
{
    private static final String ORB_INSTANCE_NAME   = "orb_instance_prop3a";
    private static final String ORB_INSTANCE_NAME_2 = "orb_instance_prop3b";

    @Before
    public void resetCalledFlags()
    {
        PreInitialisation._called  = false;
        PreInitialisation2._called = false;
        AllPreInitialisation._called  = false;
        PostInitialisation._called  = false;
        PostInitialisation2._called = false;
        AllPostInitialisation._called = false;
    }

    @Test
    public void test()
    {
        Map<String, String> testProps = new HashMap<>();

        testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                "com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation");
        testProps.put(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                "com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation");
        testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME),
                "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");
        testProps.put(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME),
                "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation");
        testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2");
        testProps.put(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2");

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(testProps);

        // Initialise first ORB instance — should fire AllPreInit and PreInitialisation only
        ORB orb = ORB.getInstance(ORB_INSTANCE_NAME);
        System.out.println("Initialising First ORB Instance");
        orb.initORB(new String[] {}, null);

        assertTrue("AllPreInitialisation should fire for every ORB init", AllPreInitialisation._called);
        assertTrue("AllPostInitialisation should fire for every ORB init", AllPostInitialisation._called);
        assertTrue("PreInitialisation should fire for its specific ORB", PreInitialisation._called);
        assertTrue("PostInitialisation should fire for its specific ORB", PostInitialisation._called);
        assertFalse("PreInitialisation2 should NOT fire until its ORB is initialised", PreInitialisation2._called);
        assertFalse("PostInitialisation2 should NOT fire until its ORB is initialised", PostInitialisation2._called);

        // Reset global flags before initialising second ORB
        AllPreInitialisation._called  = false;
        AllPostInitialisation._called = false;

        // Initialise second ORB instance — should fire AllPreInit and PreInitialisation2 only
        try
        {
            orb = ORB.getInstance(ORB_INSTANCE_NAME_2);
            System.out.println("Initialising Second ORB Instance");
            orb.initORB(new String[] {}, null);
        }
        catch (Exception e)
        {
            fail("ERROR - " + e);
        }

        assertTrue("AllPreInitialisation should fire again for second ORB", AllPreInitialisation._called);
        assertTrue("AllPostInitialisation should fire again for second ORB", AllPostInitialisation._called);
        assertTrue("PreInitialisation2 should fire for its specific ORB", PreInitialisation2._called);
        assertTrue("PostInitialisation2 should fire for its specific ORB", PostInitialisation2._called);

        try
        {
            orb.destroy();
        }
        catch (Exception e)
        {
            fail("ERROR - " + e);
        }
    }
}
