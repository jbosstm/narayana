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

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;

/**
 * Single point of control for the management of storage instances.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-07
 */
public class StoreManager
{
    private static ObjectStoreAPI actionStore = null; // for BasicAction i.e. tx logging / recovery
    private static ObjectStoreAPI stateStore = null; // for StateManager i.e. txoj object type store.
    private static ObjectStoreAPI communicationStore = null; // for IPC e.g. JacOrbRCServiceInit, TransactionStatusManagerItem

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
    }

    public static final RecoveryStore getRecoveryStore ()
    {
        return getActionStore();
    }

    public static final TxLog getTxLog() {
        return getActionStore();
    }

    public static final ParticipantStore getParticipantStore() {
        return getActionStore();
    }

    public static final ParticipantStore getCommunicationStore() {
        return getCommunicationStoreInternal();
    }

    private static final ObjectStoreAPI getCommunicationStoreInternal()
    {
        if(communicationStore != null) {
            return communicationStore;
        }

        synchronized(StoreManager.class) {

            if(communicationStore != null) {
                return communicationStore;
            }

            String communicationStoreType = arjPropertyManager.getCoordinatorEnvironmentBean().getCommunicationStore();

            try
            {
                Class osc = Class.forName(communicationStoreType);

                communicationStore = (ObjectStoreAPI) osc.newInstance();
            }
            catch (final Throwable ex)
            {
                throw new FatalError(tsLogger.i18NLogger.get_StoreManager_invalidtype() + " " + communicationStoreType, ex);
            }

            communicationStore.start();
        }

        return communicationStore;
    }

    /**
     * @return the <code>ObjectStore</code> implementation which the
     *         transaction coordinator will use.
     * @see ObjectStore
     */

    private static final ObjectStoreAPI getActionStore()
    {
        if(actionStore != null) {
            return actionStore;
        }

        synchronized(StoreManager.class) {

            if(actionStore != null) {
                return actionStore;
            }

            /*
            * Check for action store once per application. The second parameter is
            * the default value, which is returned if no other value is specified.
            */

            String actionStoreType = null;

            if (arjPropertyManager.getCoordinatorEnvironmentBean().isTransactionLog()) {
                actionStoreType = LogStore.class.getName();
            } else {
                actionStoreType = arjPropertyManager.getCoordinatorEnvironmentBean().getActionStore();
            }

            // Defaults to ObjectStore.OS_UNSHARED

            if (arjPropertyManager.getCoordinatorEnvironmentBean().isSharedTransactionLog()) {
                arjPropertyManager.getObjectStoreEnvironmentBean().setShare(StateType.OS_SHARED);
            }

            try
            {
                Class osc = Class.forName(actionStoreType);

                actionStore = (ObjectStoreAPI) osc.newInstance();
            }
            catch (final Throwable ex)
            {
                throw new FatalError(tsLogger.i18NLogger.get_StoreManager_invalidtype() + " " + actionStoreType, ex);
            }

            actionStore.start();
        }

        return actionStore;
    }

    public static ObjectStoreAPI getTxOJStore() {
        return (ObjectStoreAPI) setupStore(null, StateType.OS_UNSHARED);
    }

    public static ParticipantStore setupStore (String rootName, int sharedStatus)
    {
        if(stateStore != null) {
            return stateStore;
        }

        synchronized(StoreManager.class) {

            if(stateStore != null) {
                return stateStore;
            }

            if(sharedStatus != arjPropertyManager.getObjectStoreEnvironmentBean().getShare()) {
                arjPropertyManager.getObjectStoreEnvironmentBean().setShare(sharedStatus);
            }

            if(rootName != null && !rootName.equals(arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot())) {
                throw new IllegalArgumentException(tsLogger.i18NLogger.get_StoreManager_invalidroot(
                        arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot(), rootName));
            }

            String storeType = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreType();

            try {
                Class osc = Class.forName(storeType);

                stateStore = (ObjectStoreAPI) osc.newInstance();

            } catch (final Throwable ex)
            {
                throw new FatalError(tsLogger.i18NLogger.get_StoreManager_invalidtype() + " " + storeType, ex);
            }

            stateStore.start();
        }
        return stateStore;
    }
}
