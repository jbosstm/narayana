package org.jboss.jbossts.qa.astests.crash;

import org.jboss.jbossts.qa.astests.recovery.ASFailureSpec;

public interface CrashLocal extends javax.ejb.EJBLocalObject {
    String testXA(String ... args);
    String testXA(ASFailureSpec... specs);
}
