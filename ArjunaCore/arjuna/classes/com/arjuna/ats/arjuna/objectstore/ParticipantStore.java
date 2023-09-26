/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * Transactional participants MUST use this type of ObjectStore. It allows
 * them to be driven through 2PC.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public interface ParticipantStore extends TxLog
{
    /**
     * Commit the object's state in the object store.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean commit_state (Uid u, String tn) throws ObjectStoreException;

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
     * Read the object's shadowed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return the state of the object.
     */

    public InputObjectState read_uncommitted (Uid u, String tn) throws ObjectStoreException;

    /**
     * Remove the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException;

    /**
     * Write a copy of the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @param buff The state to write.
     *
     * @return <code>true</code> if no errors occurred, <code>false</code>
     * otherwise.
     */

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState buff) throws ObjectStoreException;

    public boolean fullCommitNeeded ();
}