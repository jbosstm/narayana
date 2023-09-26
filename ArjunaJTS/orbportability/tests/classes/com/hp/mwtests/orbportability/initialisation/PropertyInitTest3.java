/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2;
import com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2;

/**
 * This test tests the use of the pre/post-initialisation properties when
 * passed to the ORB initialisation routine.
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class PropertyInitTest3
{
    private static String   ORB_INSTANCE_NAME = "orb_instance_1";
    private static String   ORB_INSTANCE_NAME_2 = "orb_instance_2";

    @Test
    public void test()
    {
        Properties testProps = new Properties();

        testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                "com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation");
        testProps.setProperty(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                "com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation");
        testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME),
                "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");
        testProps.setProperty(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME),
                "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation");
        testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME_2),
                "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2");
        testProps.setProperty(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME_2),
                "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2");

        ORB orb = ORB.getInstance(ORB_INSTANCE_NAME);
            System.out.println("Initialising First ORB Instance");
            orb.initORB(new String[] {}, testProps);

        assertTrue( PreInitialisation._called );

        assertTrue( PostInitialisation._called );

        assertTrue( PreInitialisation2._called );

        assertTrue( PostInitialisation2._called );

        assertTrue( AllPreInitialisation._called );

        assertTrue( AllPostInitialisation._called );

        try
        {
            /**
             * Reset called flags on All ORB instance pre-initialisation
             */
            AllPreInitialisation._called = false;
            AllPostInitialisation._called = false;
            orb = ORB.getInstance(ORB_INSTANCE_NAME_2);
            System.out.println("Initialising Second ORB Instance");
            orb.initORB(new String[] {}, testProps);
        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
        }

        assertTrue( PreInitialisation2._called );

        assertTrue( PostInitialisation2._called );

        assertTrue( AllPreInitialisation._called );

        assertTrue( AllPostInitialisation._called );

        try
        {
            orb.destroy();
        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
        }
    }
}