/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.hp.mwtests.performance.servers;

import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.ats.jts.OTSManager;

import com.hp.mwtests.performance.*;

import com.hp.mwtests.performance.implementations.perftestwork.SingleResourceRegistration;
import com.hp.mwtests.performance.implementations.perftestwork.DummyPerfTestImplementation;

public class NestedOnePhaseServer extends Test
{
    /**
     * The main test method which must assert either a pass or a fail.
     */
    public void run(String[] args)
    {
        try
        {
            String serviceName = args[ args.length - 2 ];
            String serviceName2 = args[ args.length - 1 ];

            /**
             * Retrieve ORB and OA references, intialise them
             * and then set the OTSManager ORB and OA properties
             */
            ORB orb = ORB.getInstance( PerformanceFramework.ORB_INSTANCE_NAME );
            OA oa = OA.getRootOA( orb );

            orb.initORB(args, null);
            oa.initOA(args);

            OTSManager.setORB(orb);
            OTSManager.setPOA(oa);

            SingleResourceRegistration obj1 = new SingleResourceRegistration();
            oa.objectIsReady(obj1);
            PerfTestInterface objRef1 = PerfTestInterfaceHelper.narrow(oa.corbaReference(obj1));

            registerService( serviceName, orb.orb().object_to_string(objRef1) );

            DummyPerfTestImplementation obj2 = new DummyPerfTestImplementation();
            oa.objectIsReady(obj2);
            PerfTestInterface objRef2 = PerfTestInterfaceHelper.narrow(oa.corbaReference(obj2));

            registerService( serviceName2, orb.orb().object_to_string(objRef2) );

            assertSuccess();
            assertReady();

            orb.orb().run();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

    public static void main(String[] args)
    {
        NestedOnePhaseServer server = new NestedOnePhaseServer();

        server.initialise( null, null, args, new LocalHarness() );
        server.runTest();
    }
}
