/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.hp.mwtests.ts.arjuna.objectstore.jgroups;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups.JGroupsStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jgroups.Receiver;
import org.jgroups.View;

import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.Role;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JGroupsTestBase {
    static final String JGROUPS_CONFIG_FILE = "jgroups.xml";
    static final String CLUSTER_NAME = "clusteredObjectStore"; // a name for the cluster of shared stores
    // location of the file system store (with surefire it will be the build directory)
    static final String STORE_DIR = System.getProperty("user.dir") + "/jgroups-caches";
    public static final long RECOVERY_TIMEOUT_MS = 10_000;

    @FunctionalInterface
    public interface ThrowingBooleanSupplier {
        boolean getAsBoolean() throws Exception;
    }

    public static void waitFor(long timeoutMs, String description, ThrowingBooleanSupplier condition) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!condition.getAsBoolean()) { // this line can throw the Exception
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("Timed out waiting for: " + description);
            }
            assertDoesNotThrow(() -> TimeUnit.MILLISECONDS.sleep(50), "interrupted");
        }
    }

    /**
     * Wait for Raft leader election using a role change listener instead of polling.
     * A role change to Leader or Follower implies a leader has been elected.
     */
    static void awaitLeaderElection(RAFT raft, long timeout, TimeUnit unit) throws InterruptedException {
        if (raft.leader() != null) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        RAFT.RoleChange listener = role -> {
            if (role == Role.Leader || role == Role.Follower) {
                latch.countDown();
            }
        };
        raft.addRoleListener(listener);
        try {
            if (raft.leader() != null) {
                return;
            }
            assertTrue(latch.await(timeout, unit), "Leader election timed out");
        } finally {
            raft.remRoleListener(listener);
        }
    }

    // recursively delete the contents of dir, depth first, and then dir itself
    protected static void removeDirectory(String dir) {
        try {
            Path storePath = Paths.get(dir);
            if (Files.exists(storePath)) {
                Files.walk(storePath)
                        .sorted(Comparator.reverseOrder()) // depth first
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignore) {
                            }
                        });
            }
        } catch (IOException ignore) {
        }
    }

    /**
     * Use view change notifiers to avoid indeterminate waits.
     */
    static class ViewChangeListener implements Receiver {
        private final CountDownLatch latch;
        private final int expectedSize;

        ViewChangeListener(int expectedSize) {
            this.latch = new CountDownLatch(1);
            this.expectedSize = expectedSize;
        }

        @Override
        public void viewAccepted(View view) {
            if (view.size() >= expectedSize) {
                latch.countDown();
            }
        }

        boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }
    }

    // record bringing together various data related to a slot store instance
    record Store(String clusterName,
                 String nodeName, // name of a cluster node
                 JGroupsStoreEnvironmentBean config, // config for the slot store on this node
                 JGroupsSlots slots, // slot store
                 Path path) { // filesystem path where the persistent cache is located

        public Store(String configFile, String clusterName, String nodeName, String storeDir) throws Exception {
            this(
                    clusterName,
                    nodeName,
                    new JGroupsStoreEnvironmentBean(),
                    new JGroupsSlots(),
                    Paths.get(storeDir + "/" + nodeName)
            );

            config.setNodeAddress(nodeName);
            config.setClusterName(clusterName);
            config.setStoreDir(path.toString());
            config.setCacheName(clusterName); // Use clusterName so all stores join the same cluster
            config.setJGroupsConfigFileName(configFile);
            config.setBackingSlots(slots);
            // Use SharedSlotKeyGenerator so all nodes in the cluster share the same slot keys
            config.setSlotKeyGeneratorClassName(SharedSlotKeyGenerator.class.getName());

            config.setCallTimeout(1500L);
            config.setCachingTime(30000L);
            config.setMigrateData(true);
        }

        /*
         * Stop the cache (which will close its internal channel).
         * Note: The Store's channel field is not used since ReplCache manages its own channel.
         */
        public void stop() {
            try {
                if (config.getCache() != null) {
                    try {
                        config.getCache().stop();
                    } catch (Throwable e) {
                        if ("null".equals(config.getCache().getView())) {
                            System.err.printf("ERROR: null view while stopping cache %s%n",
                                    config.getCache().getClusterName());
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (CoreEnvironmentBeanException ignore) {
            }
        }

        public void start() throws Exception {
            // Initialize slots - this will start the cache
            // slots.init(config) is called internally by startRecoveryStore
            startRecoveryStore(config);
        }
    }

    static Store createStore(String nodeName, String clusterName, String storeDir) throws Exception {
        return new Store(JGROUPS_CONFIG_FILE, clusterName, nodeName, storeDir);
    }

    /**
     * Start a new recovery manager, shutting down the current one if it is running
     * @param bean config for the recovery manager
     * @return the new store
     */
    static RecoveryStore startRecoveryStore(JGroupsStoreEnvironmentBean bean) {
        StoreManager.shutdown(); // remove any existing store

        // tell the recovery manager that we are using the slot store
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());

        try {
            /*
             * The intent is to have one recovery store per JVM, and we want to start each with a different config.
             * However, environment bean instances are global to the JVM and can only be set once so replace the current
             * bean using MethodHandles to update the BeanPopulator bean instances map (an alternative could be
             * to update all the fields of the existing bean instance).
             */
            replaceEnvironmentBean(bean);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return StoreManager.getRecoveryStore();
    }

    /*
     * update the slot store environment bean which normally gets set once per VM, but we need different
     * values of the SlotStoreEnvironmentBean for various tests
     */
    static private void replaceEnvironmentBean(SlotStoreEnvironmentBean bean) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                BeanPopulator.class,
                MethodHandles.lookup()
        );

        // Get a VarHandle for the private static field
        VarHandle varHandle = lookup.findStaticVarHandle(
                BeanPopulator.class,
                "beanInstances",
                ConcurrentMap.class
        );

        ConcurrentMap<String, Object> beanInstances = (ConcurrentMap<String, Object>) varHandle.get();

        beanInstances.put(SlotStoreEnvironmentBean.class.getName(), bean);
    }

    /*
     * reset the AtomicActionRecoveryModule
     * this is useful when restarting recovery because the AARM initialises a private static variable
     * RecoveryStore _recoveryStore which needs to be reset when testing different recovery configurations
     */
    static void resetAtomicActionRecoveryModule() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                AtomicActionRecoveryModule.class,
                MethodHandles.lookup()
        );

        // Get a VarHandle for the private static field
        VarHandle varHandle = lookup.findStaticVarHandle(
                AtomicActionRecoveryModule.class,
                "_recoveryStore",
                RecoveryStore.class
        );

        varHandle.set((Object) null);
    }

    boolean containsAtomicAction(RecoveryStore recoveryStore, AtomicAction aa) {
        InputObjectState ios = new InputObjectState();

        try {
            if (recoveryStore.allObjUids(aa.type(), ios, StateStatus.OS_UNKNOWN)) {
                Uid id;

                do {
                    try {
                        id = UidHelper.unpackFrom(ios);
                        if (id.equals(aa.get_uid())) {
                            return true;
                        }
                    } catch (Exception ex) {
                        return false;
                    }
                }
                while (id.notEquals(Uid.nullUid()));
            }
        } catch (ObjectStoreException ignore) {
        }

        return false;
    }

    static class Participant extends AbstractRecord {
        @Override
        public int typeIs() {
            return 0;
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public void setValue(Object o) {
        }

        @Override
        public int nestedAbort() {
            return 0;
        }

        @Override
        public int nestedCommit() {
            return 0;
        }

        @Override
        public int nestedPrepare() {
            return 0;
        }

        @Override
        public int topLevelAbort() {
            return 0;
        }

        @Override
        public int topLevelCommit() {
            return 0;
        }

        @Override
        public int topLevelPrepare() {
            return 0;
        }

        @Override
        public void merge(AbstractRecord a) {
        }

        @Override
        public void alter(AbstractRecord a) {
        }

        @Override
        public boolean shouldAdd(AbstractRecord a) {
            return true; // record should be added to the intentions list
        }

        @Override
        public boolean shouldAlter(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldMerge(AbstractRecord a) {
            return false;
        }

        @Override
        public boolean shouldReplace(AbstractRecord a) {
            return false;
        }
    }
}
