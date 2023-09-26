/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PostSetLoader;
import com.hp.mwtests.orbportability.initialisation.postset.AllPostSet;
import com.hp.mwtests.orbportability.initialisation.postset.SinglePostSetUsingInterface;

public class PostSetTest
{
    private final static String ORB_INSTANCE_NAME = "PostSetTestORB";
    private final static String ORB_INSTANCE_NAME2 = "PostSetTestORB2";

    @Test
    public void test()
    {
        Map<String, String> testProps = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        testProps.put(PostSetLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                "com.hp.mwtests.orbportability.initialisation.postset.AllPostSet");
        testProps.put(PostSetLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME2),
                "com.hp.mwtests.orbportability.initialisation.postset.SinglePostSetUsingInterface");

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(testProps);

        ORB orb = null;
        ORB orb2 = null;

        orb = ORB.getInstance(ORB_INSTANCE_NAME);
        orb.initORB(new String[] {}, null);

        orb2 = ORB.getInstance(ORB_INSTANCE_NAME2);

        orb2.setOrb(orb.orb());

        assertTrue( AllPostSet._called );

        assertTrue( SinglePostSetUsingInterface._called );

        assertEquals(orb2, SinglePostSetUsingInterface._passedObj );

        orb.destroy();
    }
}