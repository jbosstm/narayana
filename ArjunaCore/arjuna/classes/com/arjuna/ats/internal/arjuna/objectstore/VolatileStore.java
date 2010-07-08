/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2008,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An in-memory ObjectStore that never writes to stable storage.
 * Useless for most production apps, but handy for some performance testing cases.
 * Does not support crash recovery methods as there is no stable state to recover.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2008-10
 */
public class VolatileStore extends ObjectStore
{
    /**
     * The type of the object store. This is used to order the
     * instances in the intentions list.
     *
     * @return the type of the record.
     * @see com.arjuna.ats.arjuna.coordinator.RecordType
     */

    public int typeIs()
    {
        return ObjectStoreType.VOLATILE;
    }

    /**
     * Obtain all of the Uids for a specified type.
     *
     * @param s The type to scan for.
     * @param buff The object state in which to store the Uids
     * @param m The file type to look for (e.g., committed, shadowed).
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean allObjUids(String s, InputObjectState buff, int m) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Obtain all types of objects stored in the object store.
     *
     * @param buff The state in which to store the types.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean allTypes(InputObjectState buff) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * @param u The object to query.
     * @param tn The type of the object to query.
     * @return the current state of the object's state (e.g., shadowed,
     *         committed ...)
     */

    public int currentState(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("VolatileStore.currentState(Uid=" + u + ", typeName=" + tn + ")");
        }

        return getState(u);
    }

    /**
     * @return the name of the object store.
     */

    public String getStoreName()
    {
        return "VolatileStore";
    }

    /**
     * Commit the object's state in the object store.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean commit_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean hide_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Reveal a hidden object's state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean reveal_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Read the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return the state of the object.
     */

    public InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("VolatileStore.read_committed(Uid=" + u + ", typeName=" + tn + ")");
        }

        return read(u, tn, StateStatus.OS_COMMITTED);
    }

    /**
     * Read the object's shadowed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return the state of the object.
     */

    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Remove the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean remove_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("VolatileStore.remove_committed(Uid=" + u + ", typeName=" + tn + ")");
        }

        return remove(u, tn, StateStatus.OS_COMMITTED);
    }

    /**
     * Remove the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean write_committed(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("VolatileStore.write_committed(Uid=" + u + ", typeName=" + tn + ")");
        }

        return write(u, tn, buff, StateStatus.OS_COMMITTED);
    }

    /**
     * Write a copy of the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Suppress directories of the specified type from
     * allTypes etc?
     */

    protected boolean supressEntry(String name)
    {
        return false;
    }

    ///////////////////////////////////////////

    /*
        Implementation notes:

          We assume Uid is globally unique, so we don't store or key on type name at all
          - it's only needed during recovery, which we obviously don't support.

          This implementation is intended to be memory efficient, as we also want to use it on small footprint devices.
          Hence we map Uid to a byte array rather than e.g. some more complex value class containing the byte[] state plus
            fields for ObjectStore state (committed/hidden etc) and typeName.

          In recent times we have been using this more for performance testing than small footprint installs.
          We therefore prefer ConcurrentHashMap, even though it's not available in J2ME. Fork from previous version
          that used synchronized Map if you want a build for small footprint environments.

          The byte[] array is simply the contents of the Object's state buffer.
    */
    private ConcurrentMap<Uid, byte[]> stateMap = new ConcurrentHashMap<Uid, byte[]>();

    private boolean remove(Uid u, String tn, int state) throws ObjectStoreException
    {
        Object oldValue = stateMap.remove(u);
        return (oldValue != null);
    }

    private InputObjectState read(Uid u, String tn, int state) throws ObjectStoreException
    {
        byte[] data = stateMap.get(u);

        if(data != null) {
            InputObjectState new_image = new InputObjectState(u, tn, data);
            return new_image;
        } else {
            return null;
        }
    }

    private boolean write(Uid u, String tn, OutputObjectState buff, int state) throws ObjectStoreException
    {
        stateMap.put(u, buff.buffer());
        return true;
    }

    private int getState(Uid u)
    {
        if(stateMap.containsKey(u)) {
            return StateStatus.OS_COMMITTED;
        } else {
            return StateStatus.OS_UNKNOWN;
        }
    }
}
