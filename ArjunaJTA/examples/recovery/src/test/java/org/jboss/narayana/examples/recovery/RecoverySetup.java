package org.jboss.narayana.examples.recovery;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import junit.framework.Assert;
import org.jboss.narayana.examples.util.DummyXAResource;
import org.jboss.narayana.examples.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.*;

public class RecoverySetup {
    protected static RecoveryManager recoveryManager;

//    @BeforeClass
    public static void startRecovery() {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(Util.recoveryStoreDir);
        RecoveryManager.delayRecoveryManagerThread() ;
        BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class).setRecoveryBackoffPeriod(1);
        recoveryManager = RecoveryManager.manager();
    }

//    @AfterClass
    public static void stopRecovery() {
        recoveryManager.terminate();
    }

    protected void runRecoveryScan() {
        recoveryManager.scan();
    }
}
