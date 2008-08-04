package org.jboss.jbossts.qa.astests.crash;

import org.jboss.jbossts.qa.astests.recovery.ASFailureSpec;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;

public interface CrashRem extends EJBObject {
    // JNDI names of the fault injection beans
    String BMT_JNDI_NAME = "org.jboss.jbossts.qa.astests.crash.CrashBMTRem";
    String CMT_JNDI_NAME = "org.jboss.jbossts.qa.astests.crash.CrashCMTRem";

    String testXA(String ... args) throws RemoteException;
    String testXA(ASFailureSpec... specs) throws RemoteException;
}
