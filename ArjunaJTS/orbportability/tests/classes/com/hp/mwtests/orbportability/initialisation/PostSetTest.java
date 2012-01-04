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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PostSetTest.java 2342 2006-03-30 13:06:17Z  $
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
