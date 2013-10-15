package org.jboss.jbossts.qa.CrashRecovery05Clients1;

import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

public class ClientCrashBase {
    protected boolean correct = true;
    protected String serviceIOR = null;
    protected String id;

    public ClientCrashBase(String id) {
        this.id = id;
    }

    public void initOrb(String[] args) throws Exception {
        ORBInterface.initORB(args, null);
        OAInterface.initOA();
    }

    public void shutdownOrb() {
        try
        {
            OAInterface.shutdownOA();
            ORBInterface.shutdownORB();
        }
        catch (Exception exception)
        {
            System.err.printf("%s.main: ORB shutdown problem: %s%n", id, exception);
            exception.printStackTrace(System.err);
        }
    }

    public void reportStatus() {
        if (correct)
            System.out.println("Passed");
        else
            System.out.println("Failed");
    }

    public void reportException(Exception exception) {
        System.out.println("Failed");
        System.err.printf("%s.main: %s%n", id, exception);
        exception.printStackTrace(System.err);
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
