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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyInitTest.java 2342 2006-03-30 13:06:17Z  $
 */
package com.hp.mwtests.orbportability.initialisation;

import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.ORB;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation;

import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyInitTest
{
    private static String   ORB_INSTANCE_NAME = "orb_instance_1";
    private static String   ORB_INSTANCE_NAME_2 = "orb_instance_2";

    @Test
    public void test()
    {
        Properties testProps = System.getProperties();

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

        System.setProperties(testProps);

        ORB orb = ORB.getInstance(ORB_INSTANCE_NAME);
        orb.initORB(new String[] {}, null);

        assertTrue(PreInitialisation._called);

        assertTrue(PostInitialisation._called);

        assertTrue(PreInitialisation2._called);

        assertTrue(PostInitialisation2._called);

        assertTrue(AllPreInitialisation._called);

        assertTrue(AllPostInitialisation._called);
        
        try
        {
            /**
             * Reset called flags on All ORB instance pre-initialisation
             */
            AllPreInitialisation._called = false;
            AllPostInitialisation._called = false;
            orb = ORB.getInstance(ORB_INSTANCE_NAME_2);
            System.out.println("Initialising Second ORB Instance");
            orb.initORB(new String[] {}, null);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail("ERROR - "+e);
        }

        assertTrue(PreInitialisation2._called);

        assertTrue(PostInitialisation2._called);

        assertTrue(AllPreInitialisation._called);

        assertTrue(AllPostInitialisation._called);

        try
        {
            orb.destroy();
        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
            e.printStackTrace(System.err);
        }
    }
}
