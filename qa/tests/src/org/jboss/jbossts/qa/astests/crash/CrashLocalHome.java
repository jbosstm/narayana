package org.jboss.jbossts.qa.astests.crash;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

public interface CrashLocalHome extends EJBLocalHome {
    CrashLocal create() throws CreateException;
}
