/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.remote.nested;

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.PerformanceFramework;
import org.jboss.jbossts.qa.performance.PerfTestInterface;
import org.jboss.jbossts.qa.performance.PerfTestInterfaceHelper;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.jts.OTSManager;

public class NestedTrx extends PerformanceTest
{
    protected void work() throws Exception
    {
        try
        {
            ORB orb = ORB.getInstance( PerformanceFramework.ORB_INSTANCE_NAME );
            OA oa = OA.getRootOA(orb);

            String ref1 = getServiceConfig(0);
            String ref2 = getServiceConfig(1);

            org.omg.CORBA.Object obj1 = orb.orb().string_to_object(ref1);
            PerfTestInterface d1 = (PerfTestInterface) PerfTestInterfaceHelper.narrow(obj1);

            OTSManager.get_current().begin();
            d1.work();

            org.omg.CORBA.Object obj2 = orb.orb().string_to_object(ref2);
            PerfTestInterface d2 = (PerfTestInterface) PerfTestInterfaceHelper.narrow(obj2);

            OTSManager.get_current().begin();
            d2.work();

            // nested commit
            OTSManager.get_current().commit(true);

            if (isParameterDefined("-commit"))
            {
                // top level commit
                OTSManager.get_current().commit(true);
            }
            else
            {
                // top level rollback
                OTSManager.get_current().rollback();
            }
        }
        catch (Exception e)
        {
            System.err.println("Unexpected exception: "+e);

            e.printStackTrace();
        }

    }
}