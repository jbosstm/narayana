/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;

/**
 * All ObjectStore implementations that are used to drive recovery
 * MUST implement this interface. Because recovery is a superset of
 * transaction logging, those methods are also available.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public interface RecoveryStore extends TxLog
{
    /**
     * Obtain all of the Uids for a specified type.
     *
     * @param s The type to scan for.
     * @param buff The object state in which to store the Uids
     * @param m The file type to look for (e.g., committed, shadowed). [StateStatus] Note: m=OS_UNKNOWN matches any state.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean allObjUids (String s, InputObjectState buff, int m) throws ObjectStoreException;

    /**
     * Obtain all of the Uids for a specified type, regardless of their state.
     *
     * @param s The type to scan for.
     * @param buff The object state in which to store the Uids
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean allObjUids (String s, InputObjectState buff) throws ObjectStoreException;
    
    /**
     * Obtain all types of objects stored in the object store.
     *
     * @param buff The state in which to store the types.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean allTypes (InputObjectState buff) throws ObjectStoreException;

    /**
     * @param u The object to query.
     * @param tn The type of the object to query.
     *
     * @return the current state of the object's state (e.g., shadowed,
     * committed ...) [StateStatus]
     */

    public int currentState (Uid u, String tn) throws ObjectStoreException;

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException;

    /**
     * Reveal a hidden object's state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException;

    /**
     * Read the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return the state of the object.
     */

    public InputObjectState read_committed (Uid u, String tn) throws ObjectStoreException;

    /**
     * Is the current state of the object the same as that provided as the last
     * parameter?
     *
     * @param u The object to work on.
     * @param tn The type of the object.
     * @param st The expected type of the object. [StateType]
     *
     * @return <code>true</code> if the current state is as expected,
     * <code>false</code> otherwise.
     */

    public boolean isType (Uid u, String tn, int st) throws ObjectStoreException;
}