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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropertyInitTest4.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.orbportability.initialisation;

import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.ORB;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface;

import java.util.Properties;

public class PropertyInitTest4 extends Test
{
    public final static String  ORB_INSTANCE_NAME = "testorb";
    public final static String  ORB_INSTANCE_NAME_2 = "testorb2";

    /**
     * The main test method which must assert either a pass or a fail.
     */
    public void run(String[] args)
    {
        ORB orb = null,
            orb2 = null;
        Properties testProps = System.getProperties();

        testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                        "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface");
        testProps.setProperty(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb"),
                        "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface");
        testProps.setProperty(PreInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                        "com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface");
        testProps.setProperty(PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_INSTANCE_NAME_2),
                        "com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface");

        try
        {
            orb = ORB.getInstance(ORB_INSTANCE_NAME);
            logInformation("Initialising ORB("+ORB_INSTANCE_NAME+")");
            orb.initORB(args, null);

            if ( PreInitialisationUsingInterface.getObject() == orb )
            {
                logInformation("PreInitialisationUsingInterface returned ORB("+ORB_INSTANCE_NAME+")");
                assertSuccess();
            }
            else
            {
                logInformation("PreInitialisationUsingInterface failed to return ORB("+ORB_INSTANCE_NAME+")");
                assertFailure();
            }

            if ( PostInitialisationUsingInterface.getObject() == orb )
            {
                logInformation("PostInitialisationUsingInterface returned ORB("+ORB_INSTANCE_NAME+")");
                assertSuccess();
            }
            else
            {
                logInformation("PostInitialisationUsingInterface failed to return ORB("+ORB_INSTANCE_NAME+")");
                assertFailure();
            }

            orb2 = ORB.getInstance(ORB_INSTANCE_NAME_2);
            logInformation("Initialising ORB("+ORB_INSTANCE_NAME_2+")");
            orb2.initORB(args, null);

            if ( PreInitialisationUsingInterface.getObject() == orb2 )
            {
                logInformation("PreInitialisationUsingInterface returned ORB("+ORB_INSTANCE_NAME_2+")");
                assertSuccess();
            }
            else
            {
                logInformation("PreInitialisationUsingInterface failed to return ORB("+ORB_INSTANCE_NAME_2+")");
                assertFailure();
            }

            if ( PostInitialisationUsingInterface.getObject() == orb2 )
            {
                logInformation("PostInitialisationUsingInterface returned ORB("+ORB_INSTANCE_NAME_2+")");
                assertSuccess();
            }
            else
            {
                logInformation("PostInitialisationUsingInterface failed to return ORB("+ORB_INSTANCE_NAME_2+")");
                assertFailure();
            }
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }

        try
        {
            orb.destroy();
            orb2.destroy();
        }
        catch (Exception e)
        {
            logInformation("ERROR - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

    public static void main(String[] args)
    {
        PropertyInitTest4 test = new PropertyInitTest4();

        test.initialise( null, null, args, new LocalHarness() );
        test.runTest();
    }
}
