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

import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.ORB;
import com.hp.mwtests.orbportability.initialisation.preinit.PreInitialisationUsingInterface;
import com.hp.mwtests.orbportability.initialisation.postinit.PostInitialisationUsingInterface;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

public class PropertyInitTest4
{
    public final static String  ORB_INSTANCE_NAME = "testorb";
    public final static String  ORB_INSTANCE_NAME_2 = "testorb2";

    @Test
    public void test()
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
            System.out.println("Initialising ORB("+ORB_INSTANCE_NAME+")");
            orb.initORB(new String[] {}, null);

            assertEquals(orb, PreInitialisationUsingInterface.getObject());

            assertEquals(orb, PostInitialisationUsingInterface.getObject());

            orb2 = ORB.getInstance(ORB_INSTANCE_NAME_2);
            System.out.println("Initialising ORB("+ORB_INSTANCE_NAME_2+")");
            orb2.initORB(new String[] {}, null);

            assertEquals(orb2, PreInitialisationUsingInterface.getObject());

            assertEquals(orb2, PostInitialisationUsingInterface.getObject());

        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            orb.destroy();
            orb2.destroy();
        }
        catch (Exception e)
        {
            fail("ERROR - "+e);
            e.printStackTrace(System.err);
        }
    }
}
