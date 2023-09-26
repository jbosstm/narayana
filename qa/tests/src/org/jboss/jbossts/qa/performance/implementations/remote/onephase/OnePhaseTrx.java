/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.remote.onephase;

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.PerformanceFramework;
import org.jboss.jbossts.qa.performance.PerfTestInterfaceHelper;
import org.jboss.jbossts.qa.performance.PerfTestInterface;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.jts.OTSManager;

public class OnePhaseTrx extends PerformanceTest
{
    protected void work() throws Exception
    {
        try
        {
            ORB orb = ORB.getInstance( PerformanceFramework.ORB_INSTANCE_NAME );
            OA oa = OA.getRootOA(orb);

            String ref1 = getServiceConfig(0);

            org.omg.CORBA.Object obj = orb.orb().string_to_object(ref1);
            PerfTestInterface d = (PerfTestInterface) PerfTestInterfaceHelper.narrow(obj);

            OTSManager.get_current().begin();
            d.work();

            if (isParameterDefined("-commit"))
                OTSManager.get_current().commit(true);
            else
                OTSManager.get_current().rollback();
        }
        catch (Exception e)
        {
            System.err.println("Unexpected exception: "+e);
            e.printStackTrace(System.err);
        }

    }
}