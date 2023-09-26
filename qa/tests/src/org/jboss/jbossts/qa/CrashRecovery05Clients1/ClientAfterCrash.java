/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.qa.CrashRecovery05Clients1;

import org.jboss.jbossts.qa.CrashRecovery05.AfterCrashService;
import org.jboss.jbossts.qa.CrashRecovery05.AfterCrashServiceHelper;
import org.jboss.jbossts.qa.CrashRecovery05.CheckBehavior;
import org.jboss.jbossts.qa.CrashRecovery05.ResourceTrace;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class ClientAfterCrash extends ClientCrashBase {
    protected AfterCrashService service;

    public ClientAfterCrash(String id) {
        super(id);
    }

    public void initOrb(String[] args) throws Exception {
        super.initOrb(args);

        serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
        service = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));
    }

    public void serviceSetup(CheckBehavior... behaviors) {

        service.setup_oper(1);

        correct = service.check_oper(behaviors);

        if (!correct) {
            System.out.println("Gonna fail1");
        } else {
            correct = service.is_correct();
            if (!correct)
                System.out.println("Gonna fail2");
        }
    }

    public void checkResourceTrace(ResourceTrace... traces) {
        if (!correct)
            return;

        ResourceTrace resourceTrace = service.get_resource_trace(0);

        for (ResourceTrace trace : traces) {
            if (resourceTrace == trace)
                return;
        }

        System.out.printf("Gonna fail3: resourceTrace=%s%n", resourceTrace.toString());

        correct = false;
    }

    public void waitForRecovery() throws InterruptedException {
        CrashRecoveryDelays.awaitRecoveryCR05(); // awaitReplayCompletionCR05();
    }
}
