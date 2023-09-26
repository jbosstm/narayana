/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * A null implementation. Useful for performance tuning.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NullActionStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class NullActionStore extends ShadowNoFileLockStore
{
    /**
     * @return current state of object. Assumes that genPathName allocates
     *         enough extra space to allow extra chars to be added. Action
     *         stores only store committed objects
     */

    public int currentState (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return StateStatus.OS_UNKNOWN;
    }

    /**
     * Commit a previous write_state operation which was made with the SHADOW
     * StateType argument. This is achieved by renaming the shadow and removing
     * the hidden version.
     */

    public boolean commit_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return true;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
        return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
        return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        return null;
    }

    public InputObjectState read_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        return null;
    }

    public boolean remove_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        return true;
    }

    public boolean remove_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        return false;
    }

    public boolean write_committed (Uid storeUid, String tName,
            OutputObjectState state) throws ObjectStoreException
    {
        return true;
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s)
            throws ObjectStoreException
    {
        return false;
    }
    
    public NullActionStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }
}