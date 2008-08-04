package org.jboss.jbossts.qa.astests.crash;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface CrashRemHome extends EJBHome {
    CrashRem create() throws CreateException, RemoteException;
}
