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
package com.hp.mwtests.orbportability.orbinfo;

import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORBInfo;

public class ORBInfoTest extends Test
{
    private final static String ORB_NAME = "test.orb";

    /**
     * The main test method which must assert either a pass or a fail.
     */
    public void run(String[] args)
    {
        try
        {
            ORB orb = ORB.getInstance( ORB_NAME );
            OA oa = OA.getRootOA( orb );

            orb.initORB(args, null);
            oa.initOA(args);

            logInformation("          ORBInfo.getOrbName: "+ORBInfo.getOrbName());
            logInformation("  ORBInfo.getOrbMajorVersion: "+ORBInfo.getOrbMajorVersion());
            logInformation("  ORBInfo.getOrbMinorVersion: "+ORBInfo.getOrbMinorVersion());
            logInformation("ORBInfo.getCorbaMajorVersion: "+ORBInfo.getCorbaMajorVersion());
            logInformation("ORBInfo.getCorbaMinorVersion: "+ORBInfo.getCorbaMinorVersion());
            logInformation("     ORBInfo.getOrbEnumValue: "+ORBInfo.getOrbEnumValue());

            oa.destroy();
            orb.destroy();

            assertSuccess();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

    public static void main(String[] args)
    {
        ORBInfoTest test = new ORBInfoTest();
        test.initialise(null,null,args,new LocalHarness());
        test.runTest();
    }
}
