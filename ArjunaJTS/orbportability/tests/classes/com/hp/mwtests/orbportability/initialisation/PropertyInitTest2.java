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
 * $Id: PropertyInitTest2.java 2342 2006-03-30 13:06:17Z  $
 */
package com.hp.mwtests.orbportability.initialisation;

import org.jboss.dtf.testframework.unittest.Test;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.AllPreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation2;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisation;
import com.hp.mwtests.orbportability.initialisation.postinit.AllPostInitialisation;

import java.util.Properties;

import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.CORBA.Policy;

public class PropertyInitTest2 extends Test
{
    private static String   ORB_INSTANCE_NAME = "orb_instance_";
    private static String   OA_INSTANCE_NAME  = "oa_instance_";

    public void run(String[] args)
    {
        int numberOfORBs = 1;
        int numberOfOAsPerORB = 3;

        Properties testProps = System.getProperties();

        /**
         * Setup pre-initialisation classes for all ORBs and all OAs
         */
        for (int orbCount=0;orbCount<numberOfORBs;orbCount++)
        {
            logInformation("Registering pre-initialisation property '"+PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME+orbCount)+"'");
            testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME+orbCount, "preinitmyorb"),
                                  "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");

            for (int oaCount=0;oaCount<numberOfOAsPerORB;oaCount++)
            {
                logInformation("Registering pre-initialisation property '"+PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount)+"'");
                logInformation("Registering pre-initialisation property '"+PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount, "mypoainit")+"'");

                testProps.setProperty(PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount),
                                      "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");
                testProps.setProperty(PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount, "mypoainit"),
                                      "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2");
            }
        }

        System.setProperties(testProps);

        try
        {
            long preInitExpectedValue = 0;
            long myPoaExceptedValue = 0;

            for (int orbCount=0;orbCount<numberOfORBs;orbCount++)
            {
                String orbId = ORB_INSTANCE_NAME+orbCount;
                ORB orb = ORB.getInstance(orbId);
                logInformation("Initialising ORB Instance '"+orbId+"'");
                orb.initORB(args, null);
                RootOA rootOA = RootOA.getRootOA(orb);
                rootOA.initPOA(args);
                preInitExpectedValue++;

                if (PreInitialisation._count != preInitExpectedValue)
                {
                    logInformation("Checking: Failed, Pre-initialisation class not called as expected");
                    assertFailure();
                }
                else
                {
                    logInformation("Checking: Correct ("+preInitExpectedValue+")");
                }

                for (int oaCount=0;oaCount<numberOfOAsPerORB;oaCount++)
                {
                    String oaId = OA_INSTANCE_NAME+oaCount;
                    logInformation("Initialising OA instance '"+oaId+"' for ORB Instance '"+orbId+"'");

                    Policy p[] = new Policy[1];
                    p[0] = rootOA.rootPoa().create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
                    OA oa = rootOA.createPOA(oaId, p);
                    preInitExpectedValue++;
                    myPoaExceptedValue++;
                }

                if (PreInitialisation2._count != myPoaExceptedValue)
                {
                    logInformation("Checking: Failed, Pre-initialisation of mypoa class not called as expected");
                    assertFailure();
                }
                else
                {
                    logInformation("Checking: myPOA Correct ("+myPoaExceptedValue+")");
                }


                if (PreInitialisation._count != preInitExpectedValue)
                {
                    logInformation("Checking: Failed, Pre-initialisation class not called as expected");
                    assertFailure();
                }
                else
                {
                    logInformation("Checking: Correct ("+preInitExpectedValue+")");
                }
            }
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        for (int orbCount=0;orbCount<numberOfORBs;orbCount++)
        {
            try
            {
                String orbId = ORB_INSTANCE_NAME+orbCount;
                ORB orb = ORB.getInstance(orbId);

                RootOA oa = RootOA.getRootOA(orb);
                oa.destroy();
                orb.destroy();
            }
            catch (Exception e)
            {
                logInformation("ERROR - While destroying ORB instance '"+ORB_INSTANCE_NAME+orbCount+"' ("+e+")");
                e.printStackTrace(System.err);
                assertFailure();
            }
        }

        assertSuccess();
    }

    public static void main(String[] args)
    {
	PropertyInitTest2 test = new PropertyInitTest2();

	test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());

	test.runTest();
    }
}
