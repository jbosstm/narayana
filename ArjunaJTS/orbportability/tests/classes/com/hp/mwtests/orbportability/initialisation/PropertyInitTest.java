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

import com.arjuna.mwlabs.testframework.unittest.Test;
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

public class PropertyInitTest extends Test
{
    private static String   ORB_INSTANCE_NAME = "orb_instance_1";
    private static String   ORB_INSTANCE_NAME_2 = "orb_instance_2";

    public void run(String[] args)
    {
        Properties testProps = System.getProperties();

        logInformation("Registering All ORB instance pre-initialisation object");
        logInformation("Registering All ORB instance post-initialisation object");
        logInformation("Registering First ORB instance pre-initialisation object");
        logInformation("Registering First ORB instance post-initialisation object");
        logInformation("Registering Second ORB instance pre-initialisation object");
        logInformation("Registering Second ORB instance post-initialisation object");

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

        ORB orb = null;

        try
        {
            orb = ORB.getInstance(ORB_INSTANCE_NAME);
            logInformation("Initialising First ORB Instance");
            orb.initORB(args, null);
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        if (!PreInitialisation._called)
        {
            logInformation("FAILED: First ORB instance pre-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("First ORB instance pre-initialisation called");
        }

        if (!PostInitialisation._called)
        {
            logInformation("FAILED: First ORB instance post-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("First ORB instance post-initialisation called");
        }

        if (!PreInitialisation2._called)
        {
            logInformation("Second ORB instance pre-initialisation not called");
        }
        else
        {
            logInformation("FAILED: Second ORB instance pre-initialisation called");
            assertFailure();
        }

        if (!PostInitialisation2._called)
        {
            logInformation("Second ORB instance post-initialisation not called");
        }
        else
        {
            logInformation("FAILED: Second ORB instance post-initialisation called");
            assertFailure();
        }

        if (!AllPreInitialisation._called)
        {
            logInformation("FAILED: All ORB instances pre-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("All ORB instances pre-initialisation called");
        }

        if (!AllPostInitialisation._called)
        {
            logInformation("FAILED: All ORB instances post-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("All ORB instances post-initialisation called");
        }

        try
        {
            /**
             * Reset called flags on All ORB instance pre-initialisation
             */
            AllPreInitialisation._called = false;
            AllPostInitialisation._called = false;
            orb = ORB.getInstance(ORB_INSTANCE_NAME_2);
            logInformation("Initialising Second ORB Instance");
            orb.initORB(args, null);
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        if (!PreInitialisation2._called)
        {
            logInformation("FAILED: Second ORB instance pre-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("Second ORB instance pre-initialisation called");
        }

        if (!PostInitialisation2._called)
        {
            logInformation("FAILED: Second ORB instance post-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("Second ORB instance post-initialisation called");
        }

        if (!AllPreInitialisation._called)
        {
            logInformation("FAILED: All ORB instances pre-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("All ORB instances pre-initialisation called");
        }

        if (!AllPostInitialisation._called)
        {
            logInformation("FAILED: All ORB instances post-initialisation not called");
            assertFailure();
        }
        else
        {
            logInformation("All ORB instances post-initialisation called");
        }

        try
        {
            orb.destroy();
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        assertSuccess();
    }

    public static void main(String[] args)
    {
	PropertyInitTest test = new PropertyInitTest();

	test.initialise(null, null, args, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());

	test.runTest();
    }
}
