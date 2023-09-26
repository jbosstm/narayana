/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.perftestwork;

import org.jboss.jbossts.qa.performance.PerfTestInterfacePOA;
import org.jboss.jbossts.qa.performance.PerfTestException;
import org.jboss.jbossts.qa.performance.PerformanceFramework;
import org.jboss.jbossts.qa.performance.resources.DemoSubTranResource;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import org.omg.CosTransactions.SubtransactionAwareResourceHelper;
import org.omg.CosTransactions.SubtransactionAwareResource;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;

public class SingleSubtranAwareResourceRegistration extends PerfTestInterfacePOA
{
    public void work() throws PerfTestException
    {
        try
        {
            /**
             * Retrieve ORB and OA references
             */
            ORB orb = ORB.getInstance(PerformanceFramework.ORB_INSTANCE_NAME);
            OA oa = OA.getRootOA(orb);

            Control control = OTSManager.get_current().get_control();
            Coordinator coordinator = control.get_coordinator();

            DemoSubTranResource resource = new DemoSubTranResource();

            oa.objectIsReady(resource);
            SubtransactionAwareResource res = SubtransactionAwareResourceHelper.narrow(oa.corbaReference(resource));
            coordinator.register_resource(res);
        }
        catch (Exception e)
        {
            throw new PerfTestException();
        }
    }
}