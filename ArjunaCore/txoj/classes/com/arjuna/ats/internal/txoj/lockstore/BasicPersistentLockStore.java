/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.txoj.lockstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore;
import com.arjuna.ats.txoj.exceptions.LockStoreException;
import com.arjuna.ats.txoj.lockstore.LockStore;
import com.arjuna.ats.txoj.logging.txojLogger;

/**
 * An implementation of the lock store which saves locks into files on the local
 * machine. Thus, multiple JVMs may share locks by reading and writing to the
 * same files.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: BasicPersistentLockStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class BasicPersistentLockStore extends LockStore
{

    /*
     * Ignore key as we can make use of the basic type information for this type
     * of store. Really only need it for shared memory.
     */

    public BasicPersistentLockStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("BasicPersistentLockStore.BasicPersistentLockStore()");
        }

        /*
         * Use the ShadowingStore since it has file-level locking which we
         * require. The default object store assumes locking is provided
         * entirely by the object.
         */

        _lockStore = new ShadowingStore(objectStoreEnvironmentBean);
    }

    public InputObjectState read_state (Uid u, String tName)
            throws LockStoreException
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("BasicPersistentLockStore.read_state(" + u + ", " + tName + ")");
        }

        try
        {
            return _lockStore.read_committed(u, LOCK_ROOT+tName);
        }
        catch (ObjectStoreException e)
        {
            throw new LockStoreException("Persistent store error.", e);
        }
    }

    public boolean remove_state (Uid u, String tName)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("BasicPersistentLockStore.remove_state(" + u + ", " + tName + ")");
        }

        try
        {
            return _lockStore.remove_committed(u, LOCK_ROOT+tName);
        }
        catch (ObjectStoreException e)
        {
            return false;
        }
    }

    public boolean write_committed (Uid u, String tName, OutputObjectState state)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("BasicPersistentLockStore.write_committed(" + u + ", " + tName + ", " + state + ")");
        }

        try
        {
            return _lockStore.write_committed(u, LOCK_ROOT+tName, state);
        }
        catch (ObjectStoreException e)
        {
            return false;
        }
    }

    private ParticipantStore _lockStore;
    
    /*
     * At this time we just place locks in a different sub-tree within the main object store.
     */
    
    private static final String LOCK_ROOT = "LockStore";
}