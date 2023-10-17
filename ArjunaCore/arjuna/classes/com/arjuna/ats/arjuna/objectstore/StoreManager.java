/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Single point of control for the management of storage instances.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-07
 */
public class StoreManager
{
    private static ObjectStoreAPI actionStore = null; // for BasicAction i.e. tx logging / recovery
    private static ObjectStoreAPI stateStore = null; // for StateManager i.e. object type store.
    private static ObjectStoreAPI communicationStore = null; // for IPC e.g. TransactionStatusManagerItem

    public StoreManager(ObjectStoreAPI actionStore, ObjectStoreAPI stateStore, ObjectStoreAPI communicationStore) {

        if(StoreManager.actionStore != null || StoreManager.stateStore != null || StoreManager.communicationStore != null) {
            throw new IllegalStateException("store already initialized!");
        }

        StoreManager.actionStore = actionStore;
        StoreManager.stateStore = stateStore;
        StoreManager.communicationStore = communicationStore;
    }

    public static final void shutdown() {
        if(actionStore != null) {
            actionStore.stop();
        }
        if(stateStore != null) {
            stateStore.stop();
        }
        if(communicationStore != null) {
            communicationStore.stop();
        }
        actionStore = null;
        stateStore = null;
        communicationStore = null;
    }

    public static final RecoveryStore getRecoveryStore ()
    {
        return getActionStore();
    }

    public static final TxLog getTxLog() {
        return getActionStore();
    }

    /*
     * This is wrong. The participant store should not be the same as the
     * transaction log. Why has this been changed from the default?
     */
    
    public static final ParticipantStore getParticipantStore() {
        return getActionStore();
    }

    public static final ParticipantStore getCommunicationStore() {
        if(communicationStore != null) {
            return communicationStore;
        }

        synchronized(StoreManager.class) {
            if(communicationStore != null) {
                return communicationStore;
            }

            communicationStore = initStore("communicationStore");
        }

        return communicationStore;
    }

    private static final ObjectStoreAPI getActionStore()
    {
        if(actionStore != null) {
            return actionStore;
        }

        synchronized(StoreManager.class) {
            if(actionStore != null) {
                return actionStore;
            }
            
            actionStore = initStore(null); // default
        }

        //arjPropertyManager.getCoordinatorEnvironmentBean().isSharedTransactionLog()

        return actionStore;
    }

    /*
     * Why are rootName and shareStatus not used?
     * 
     * @param rootName ignored
     */
    
    public static ParticipantStore setupStore (String rootName, int sharedStatus)
    {
        if(stateStore != null) {
            return stateStore;
        }

        synchronized(StoreManager.class) {
            if(stateStore != null) {
                return stateStore;
            }

            stateStore = initStore("stateStore");
        }

        // arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot()

        return stateStore;
    }

    private static final ObjectStoreAPI initStore(String name)
    {
        ObjectStoreEnvironmentBean storeEnvBean = BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, name);
        String storeType = storeEnvBean.getObjectStoreType();
        ObjectStoreAPI store = ClassloadingUtility.loadAndInstantiateClass(ObjectStoreAPI.class, storeType, name,
                true);

        if(store == null) {
            throw new FatalError(tsLogger.i18NLogger.init_StoreManager_instantiate_class_failure(name, storeType));
        }

        store.start();

        return store;
    }

    public static ObjectStoreAPI getTxOJStore() {
        return (ObjectStoreAPI) setupStore(null, StateType.OS_UNSHARED);
    }

    public static ObjectStoreAPI getEISNameStore() {
        return (ObjectStoreAPI) setupStore(null, StateType.OS_UNSHARED);
    }
}