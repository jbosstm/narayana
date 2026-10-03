package com.hp.mwtests.ts.arjuna.objectstore.infinispan;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public class InfinispanRecoveryTestBase extends InfinispanTestBase {

    static RecoveryManager manager;
    static Store store;
    static RecoveryStore recoveryStore;

    @BeforeAll
    public static void beforeAll() {
        // the recovery system needs to know how to create instantiates of RecoverableCrashRecord
        RecordTypeManager.manager().add(new InfinispanRecoveryGroupScanTest.RecoverableCrashRecordTypeMap());
    }

    static void startRecoverySystem(Store store) {
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();

        recoveryStore = startRecoveryStore(store.config());

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        try {
            resetAtomicActionRecoveryModule();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        AtomicActionRecoveryModule aaRecoveryModule = new AtomicActionRecoveryModule();
        manager.addModule(aaRecoveryModule); // we only need to test the XARecoveryModule
    }

    @AfterAll
    static void stopRecoveryStore() {
        if (recoveryStore != null) {
            recoveryStore.stop();
            recoveryStore = null;
        }
        if (store != null) {
            store.stop();
            store = null;
        }
        if (manager != null) {
            manager.terminate();
            manager = null;
        }
    }

    public static class RecoverableCrashRecord extends CrashRecord {

        // needs a default constructor which gets called during recovery via AbstractRecord.create
        public RecoverableCrashRecord() {
            super();
        }

        public RecoverableCrashRecord(CrashLocation crashLocation, CrashType crashType) {
            super(crashLocation, crashType);
        }

        public boolean shouldAdd(AbstractRecord a)  {
            return true;
        }
    }

    /**
     * RecordTypeMap is the arjuna mechanism that facilitates {@link AbstractRecord#create(int)}
     * to create specific record types during recovery
     */
    static class RecoverableCrashRecordTypeMap implements RecordTypeMap {
        public Class<RecoverableCrashRecord> getRecordClass ()
        {
            return RecoverableCrashRecord.class;
        }

        public int getType() {
            // the type must be registered in the RecordType registry which it will be if RecoverableCrashRecord
            // uses RecordType.USER_DEF_FIRST0 which it does
            int recordType = new RecoverableCrashRecord().typeIs();

            Assertions.assertEquals(RecordType.USER_DEF_FIRST0, recordType);

            return recordType;
        }
    }
}
