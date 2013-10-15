package org.jboss.jbossts.qa.CrashRecovery05Clients1;

import org.jboss.jbossts.qa.CrashRecovery05.BeforeCrashService;
import org.jboss.jbossts.qa.CrashRecovery05.BeforeCrashServiceHelper;
import org.jboss.jbossts.qa.CrashRecovery05.CrashBehavior;
import org.jboss.jbossts.qa.CrashRecovery05.ResourceBehavior;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class ClientBeforeCrash extends ClientCrashBase {
    protected BeforeCrashService service;
    protected ResourceBehavior[] resourceBehaviors;

    public ClientBeforeCrash(String id) {
        super(id);
    }

    public void initOrb(String[] args) throws Exception {
        super.initOrb(args);

        serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
        service = BeforeCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));
    }

    public void initCrashBehaviour(CrashBehavior ... behaviors) throws Exception {
        int i = 0;

        resourceBehaviors = new ResourceBehavior[behaviors.length];

        for (CrashBehavior behavior : behaviors) {
            resourceBehaviors[i] = new ResourceBehavior();
            resourceBehaviors[i].crash_behavior = behavior;
            i += 1;
        }

    }

    public void serviceSetup() throws Exception {
        OTS.current().begin();

        service.setup_oper(OTS.current().get_control(), resourceBehaviors);

        correct = service.is_correct();
    }
}
