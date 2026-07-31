package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsTestBase.removeDirectory;
import static com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsTestBase.resetAtomicActionRecoveryModule;
import static com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsTestBase.startRecoveryStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JGroupsSlotsTest {
    private JGroupsSlots slots;
    private JGroupsStoreEnvironmentBean config;

    public void setupStore() throws Throwable {
        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier("1");

        config = new JGroupsStoreEnvironmentBean();
        slots = new JGroupsSlots();

        config.setBackingSlotsClassName(JGroupsSlots.class.getName());
        config.setWalSyncWrites(true);
        config.setWalSyncDeletes(true);
        config.setNodeAddress("node1");
        config.setCacheName("replCache");
        config.setBackingSlots(slots);

        removeDirectory(config.getStoreDir());

        resetAtomicActionRecoveryModule();
        startRecoveryStore(config);
    }

    @AfterEach
    public void tearDown() {
        try {
            if (slots != null) {
                slots.stop();
                slots = null;
            }
        } catch (Exception ignore) {
        }
        StoreManager.shutdown();
    }

    @Test
    public void test() throws Throwable {
        setupStore();

        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

        String DATA = "junit1";
        String TYPE_NAME = "/StateManager/junit1";
        OutputObjectState oos = new OutputObjectState();

        oos.packString(DATA);
        Uid uid = new Uid();

        try {
            CountDownLatch writeLatch = new CountDownLatch(1);
            config.getCache().addChangeListener(writeLatch::countDown);

            assertTrue(recoveryStore.write_committed(uid, TYPE_NAME, oos));
            assertTrue(writeLatch.await(10, TimeUnit.SECONDS), "write propagation");

            InputObjectState inputData = recoveryStore.read_committed(uid, TYPE_NAME);
            String tn = inputData.unpackString();
            assertEquals(DATA, tn);
        } catch (ObjectStoreException e) {
            fail(e);
        }
    }
}
