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
package com.hp.mwtests.orbportability.orbspecific.orbinstance;

import org.jboss.dtf.testframework.unittest.Test;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class SimpleServer extends Test
{
    public void run(String[] args)
    {
        ORB orb = ORB.getInstance("main_orb");
        ORB orb2 = ORB.getInstance("main_orb_2");
        RootOA oa = RootOA.getRootOA(orb);
        RootOA oa2 = RootOA.getRootOA(orb2);

        try
        {
            orb.initORB(args,null);
            oa.initOA(args);

            orb2.initORB(args,null);
            oa2.initOA(args);
        }
        catch (Exception e)
        {
            logInformation("ERROR - During ORB and OA initialisation ("+e+")");
            e.printStackTrace(System.err);
            assertFailure();
        }

        try
        {
            SimpleObjectImpl obj = new SimpleObjectImpl();

            oa.objectIsReady(obj);

            if (oa.objectIsReady(obj))
            {
                logInformation("Manage to activate a servant on the same OA twice - this is incorrect");
                assertFailure();
            }
            else
            {
                logInformation("Didn't managed to activate a servant on the same OA twice - correct");
            }

            if (oa2.objectIsReady(obj))
            {
                logInformation("OA2 did not contain the servant registered on OA - correct");
                oa2.shutdownObject(obj);
            }
            else
            {
                logInformation("OA2 already contained the servant registered on OA - this is incorrect");
                assertFailure();
            }
            assertSuccess();
        }
        catch (Exception e)
        {
            logInformation("ERROR - During object initialisation ("+e+")");
            e.printStackTrace(System.err);
            assertFailure();
        }
        oa.destroy();
	orb.shutdown();
        oa2.destroy();
	orb2.shutdown();
    }

    public static void main(String[] args)
    {
        SimpleServer server = new SimpleServer();

        server.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());
        server.runTest();
    }
}
