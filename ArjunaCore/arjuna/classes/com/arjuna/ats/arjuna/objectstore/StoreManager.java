/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;
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
    private static ObjectStoreAPI communicationStore = null; // for IPC e.g. JacOrbRCServiceInit, TransactionStatusManagerItem

    public StoreManager(ObjectStoreAPI actionStore, ObjectStoreAPI stateStore, ObjectStoreAPI communicationStore) {

        if(StoreManager.actionStore != null || StoreManager.stateStore != null || StoreManager.communicationStore != null) {
            throw new IllegalStateException("store already initialized!");
        }

        StoreManager.actionStore = actionStore;
        StoreManager.stateStore = stateStore;
        StoreManager.communicationStore = communicationStore;
    }

    // should these values be null after stop?
    
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
        ObjectStoreAPI store;

        try
        {
            store = ClassloadingUtility.loadAndInstantiateClass(ObjectStoreAPI.class, storeType, name);
        }
        catch (final Throwable ex)
        {
            throw new FatalError(tsLogger.i18NLogger.get_StoreManager_invalidtype() + " " + storeType, ex);
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
