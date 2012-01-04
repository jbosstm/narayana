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
 * $Id: PropertyInitTest2.java 2342 2006-03-30 13:06:17Z  $
 */
package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2;

public class PropertyInitTest2
{
    private static String   ORB_INSTANCE_NAME = "orb_instance_";
    private static String   OA_INSTANCE_NAME  = "oa_instance_";

    @Test
    public void test()
    {
        int numberOfORBs = 1;
        int numberOfOAsPerORB = 3;

        Map<String, String> testProps = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        /**
         * Setup pre-initialisation classes for all ORBs and all OAs
         */
        for (int orbCount=0;orbCount<numberOfORBs;orbCount++)
        {
            System.out.println("Registering pre-initialisation property '"+PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME+orbCount)+"'");
            testProps.put(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb",ORB_INSTANCE_NAME+orbCount, "preinitmyorb"),
                                  "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");

            for (int oaCount=0;oaCount<numberOfOAsPerORB;oaCount++)
            {
                System.out.println("Registering pre-initialisation property '"+PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount)+"'");
                System.out.println("Registering pre-initialisation property '"+PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount, "mypoainit")+"'");

                testProps.put(PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount),
                                      "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation");
                testProps.put(PreInitLoader.generateOAPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME+orbCount, OA_INSTANCE_NAME+oaCount, "mypoainit"),
                                      "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisation2");
            }
        }

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(testProps);

        try
        {
            long preInitExpectedValue = 0;
            long myPoaExceptedValue = 0;

            for (int orbCount=0;orbCount<numberOfORBs;orbCount++)
            {
                String orbId = ORB_INSTANCE_NAME+orbCount;
                ORB orb = ORB.getInstance(orbId);
                System.out.println("Initialising ORB Instance '"+orbId+"'");
                orb.initORB(new String[] {}, null);
                RootOA rootOA = RootOA.getRootOA(orb);
                rootOA.initPOA(new String[] {});
                preInitExpectedValue++;

                assertEquals(preInitExpectedValue,  PreInitialisation._count);

                for (int oaCount=0;oaCount<numberOfOAsPerORB;oaCount++)
                {
                    String oaId = OA_INSTANCE_NAME+oaCount;
                    System.out.println("Initialising OA instance '"+oaId+"' for ORB Instance '"+orbId+"'");

                    Policy p[] = new Policy[1];
                    p[0] = rootOA.rootPoa().create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);
                    OA oa = rootOA.createPOA(oaId, p);
                    preInitExpectedValue++;
                    myPoaExceptedValue++;
                }

                assertEquals(myPoaExceptedValue, PreInitialisation2._count);

                assertEquals(preInitExpectedValue, PreInitialisation._count);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail("ERROR - "+e);
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
                fail("ERROR - While destroying ORB instance '"+ORB_INSTANCE_NAME+orbCount+"' ("+e+")");
                e.printStackTrace(System.err);
            }
        }
    }
}
