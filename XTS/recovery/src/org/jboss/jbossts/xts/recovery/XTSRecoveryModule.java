package org.jboss.jbossts.xts.recovery;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;

/**
 * extension of JBossTS recovery module interface adding install and uninstall lifecycle methods
 */
public interface XTSRecoveryModule extends RecoveryModule {
    public void install();
    public void uninstall();
}
