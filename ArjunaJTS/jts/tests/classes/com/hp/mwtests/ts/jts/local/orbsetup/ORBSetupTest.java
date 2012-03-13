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
 * $Id: ORBSetupTest.java 2342 2006-03-30 13:06:17Z  $
 */
package com.hp.mwtests.ts.jts.local.orbsetup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.internal.utils.PostInitLoader;

public class ORBSetupTest
{
    private final static String ORB_NAME = "testorb";

    @Test
    public void test()
    {
        boolean staticSet = false;
        ORB myORB = null;
        RootOA myOA = null;

        Map<String, String> properties = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();

        properties.put( PostInitLoader.generateORBPropertyName("com.arjuna.orbportability.orb", ORB_NAME), "com.arjuna.ats.jts.utils.ORBSetup");

        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(properties);

        try
        {
            myORB = ORB.getInstance(ORB_NAME);
            myOA = OA.getRootOA(myORB);

            if (staticSet)
            {
                ORBManager.setORB(myORB);
            }

            try
            {
                myORB.initORB(new String[] {}, null);
                myOA.initOA();

                assertEquals(myORB, ORBManager.getORB());
            }
            catch (FatalError e)
            {
                if (staticSet)
                {
                    System.out.println("FatalError thrown as expected");
                }
                else
                {
                    fail("Error: "+e);
                    e.printStackTrace(System.err);
                }
            }

            myOA.destroy();
            myORB.destroy();
        }
        catch (Throwable e)
        {
            fail("Error: "+e);
            e.printStackTrace(System.err);
        }
    }
}

