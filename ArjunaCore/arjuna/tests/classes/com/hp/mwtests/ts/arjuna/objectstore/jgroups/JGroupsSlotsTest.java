package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsTestBase.REPLICATION_TIMEOUT_MS;
import static com.hp.mwtests.ts.arjuna.objectstore.jgroups.JGroupsTestBase.waitFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JGroupsSlotsTest {
    public static void setupStore() throws IOException, CoreEnvironmentBeanException {
        // common config for each slot store
        SlotStoreEnvironmentBean slotStoreConfig = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        JGroupsStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(JGroupsStoreEnvironmentBean.class);
        BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class).setNodeIdentifier("1");
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(SlotStoreAdaptor.class.getName());
        var slots = new JGroupsSlots(); // slot store backed by an infinispan cache

        slotStoreConfig.setBackingSlotsClassName(JGroupsSlots.class.getName());

        config.setNumberOfSlots(slotStoreConfig.getNumberOfSlots());
        config.setBytesPerSlot(slotStoreConfig.getBytesPerSlot());
        config.setStoreDir(slotStoreConfig.getStoreDir());
        config.setSyncWrites(true);
        config.setSyncDeletes(true);
        config.setNodeAddress("node1");
        config.setCacheName("replCache");
        config.setBackingSlots(slots);

        slots.init(config); // can throw IOException

        // tell the recovery manager that we are using the slot store (note beans can only be set once)
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());
        BeanPopulator.setBeanInstanceIfAbsent(JGroupsStoreEnvironmentBean.class.getName(), config);
    }

    @Test
    public void test() throws Exception {
        setupStore();

        SlotStoreEnvironmentBean slotStoreConfig = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        String backingSlotsClassName = slotStoreConfig.getBackingSlotsClassName();
        assertEquals(JGroupsSlots.class.getName(), backingSlotsClassName);

        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

        String DATA = "junit1";
        String TYPE_NAME = "/StateManager/junit1";
        OutputObjectState oos = new OutputObjectState();

        oos.packString(DATA);
        Uid uid = new Uid();

        try {
            assertTrue(recoveryStore.write_committed(uid, TYPE_NAME, oos));
            // write to the cluster of 1 and wait for it to propagate
            // (see JGroupsClusterTest.testTwoNodeReplication for a two node cluster test)
            waitFor(REPLICATION_TIMEOUT_MS, "write propagation",
                () -> recoveryStore.read_committed(uid, TYPE_NAME) != null);
            InputObjectState inputData = recoveryStore.read_committed(uid, TYPE_NAME);
            String tn = inputData.unpackString();
            assertEquals(DATA, tn);
        } catch (ObjectStoreException e) {
            fail(e);
        }
    }
}
