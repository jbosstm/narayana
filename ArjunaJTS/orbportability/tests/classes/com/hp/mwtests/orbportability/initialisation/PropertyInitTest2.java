/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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