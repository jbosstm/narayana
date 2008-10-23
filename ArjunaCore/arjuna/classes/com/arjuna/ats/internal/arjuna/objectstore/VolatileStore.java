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

import com.arjuna.ats.arjuna.objectstore.ObjectStoreImple;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

import java.util.Map;
import java.util.HashMap;

/**
 * An in-memory ObjectStore that never writes to stable storage.
 * Useless for most production apps, but handy for some performance testing cases.
 * Does not support crash recovery methods as there is no stable state to recover.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2008-10
 */
public class VolatileStore extends ObjectStoreImple
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
     * @param String           s The type to scan for.
     * @param InputObjectState buff The object state in which to store the Uids
     * @param int              m The file type to look for (e.g., committed, shadowed).
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
     * @param InputObjectState buff The state in which to store the types.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean allTypes(InputObjectState buff) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * @param Uid    u The object to query.
     * @param String tn The type of the object to query.
     * @return the current state of the object's state (e.g., shadowed,
     *         committed ...)
     */

    public synchronized int currentState(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.currentState(Uid="+u+", typeName="+tn+")");
        }

        return getState(u);
    }

    /**
     * @return the name of the object store.
     */

    public String getStoreName()
    {
        return ArjunaNames.Implementation_ObjectStore_VolatileStore().stringForm();
    }

    /**
     * Commit the object's state in the object store.
     *
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public synchronized boolean commit_state(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.commit_state(Uid="+u+", typeName="+tn+")");
        }

        if(currentState(u, tn) == ObjectStore.OS_UNCOMMITTED) {
            setState(u, ObjectStore.OS_COMMITTED);
        }

        if(currentState(u, tn) == ObjectStore.OS_UNCOMMITTED_HIDDEN) {
            setState(u, ObjectStore.OS_COMMITTED_HIDDEN);
        }

        return true;
    }

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
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
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
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
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
     * @return the state of the object.
     */

    public synchronized InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.read_committed(Uid="+u+", typeName="+tn+")");
        }

        return read(u, tn, ObjectStore.OS_COMMITTED);
    }

    /**
     * Read the object's shadowed state.
     *
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
     * @return the state of the object.
     */

    public synchronized InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.read_uncommitted(Uid="+u+", typeName="+tn+")");
        }

        return read(u, tn, ObjectStore.OS_UNCOMMITTED);
    }

    /**
     * Remove the object's committed state.
     *
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public synchronized boolean remove_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.remove_committed(Uid="+u+", typeName="+tn+")");
        }

        return remove(u, tn, ObjectStore.OS_COMMITTED);
    }

    /**
     * Remove the object's uncommitted state.
     *
     * @param Uid    u The object to work on.
     * @param String tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public synchronized boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.remove_uncommitted(Uid="+u+", typeName="+tn+")");
        }

        return remove(u, tn, ObjectStore.OS_UNCOMMITTED);
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param Uid               u The object to work on.
     * @param String            tn The type of the object to work on.
     * @param OutputObjectState buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public synchronized boolean write_committed(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.write_committed(Uid="+u+", typeName="+tn+")");
        }

        return write(u, tn, buff, ObjectStore.OS_COMMITTED);
    }

    /**
     * Write a copy of the object's uncommitted state.
     *
     * @param Uid               u The object to work on.
     * @param String            tn The type of the object to work on.
     * @param OutputObjectState buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public synchronized boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                         FacilityCode.FAC_OBJECT_STORE,
                         "VolatileStore.write_uncommitted(Uid="+u+", typeName="+tn+")");
        }

        return write(u, tn, buff, ObjectStore.OS_UNCOMMITTED);
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

          We assume Uid is globally uniq, so we don't store or key on type name at all
          - it's only needed during recovery, which we obviously don't support.

          This implementation is intended to be memory efficient, as we also want to use it on small footprint devices.
          Hence we map Uid to a byte array rather than e.g. some more complex value class containing the byte[] state plus
            fields for ObjectStore state (committed/hidden etc) and typeName.

          The public methods that access the internal state are synchronized, the private ones they delegate to are not.
          For even greater performance in multi-threaded apps on large machines, perhaps use ConcurrentHashMap and rework
          the synchronization. Not done initially because the small footprint apps we sometimes want to run on don't
          have ConcurrentHashMap, so we'd have to fork the impl.

          The byte[] array is the contents of the Object's state buffer, prefixed by a single byte holding
          the ObjectStore state e.g. ObjectStore.OS_COMMITTED.  Clearly this will blow up horribly if that enum
          ever makes use of its full int range potential rather than keeping to byte values as it currently does.
          but in the meanwhile it's nicely space efficient.
    */
    private Map<Uid, byte[]> stateMap = new HashMap<Uid, byte[]>();

    private boolean remove(Uid u, String tn, int state) throws ObjectStoreException
    {
        if(currentState(u, tn) == state) {
            stateMap.remove(u);
            return true;
        } else {
            return false;
        }
    }

    private InputObjectState read(Uid u, String tn, int state) throws ObjectStoreException
    {
        if(currentState(u, tn) == state) {
            byte[] withStateHeader = stateMap.get(u);
            byte[] withoutStateHeader = new byte[withStateHeader.length-1];
            System.arraycopy(withStateHeader, 1, withoutStateHeader, 0, withoutStateHeader.length);
            InputObjectState new_image = new InputObjectState(u, tn, withoutStateHeader);
            return new_image;
        } else {
            return null;
        }
    }

    private boolean write(Uid u, String tn, OutputObjectState buff, int state) throws ObjectStoreException
    {
        byte[] withoutStateHeader = buff.buffer();
        byte[] withStateHeader = new byte[withoutStateHeader.length+1];
        System.arraycopy(withoutStateHeader, 0, withStateHeader, 1, withoutStateHeader.length);
        stateMap.put(u, withStateHeader);
        setState(u, state);        
        return true;
    }

    private void setState(Uid u, int state) {
        byte[] value = stateMap.get(u);
        if(value != null) {
            value[0] = (byte)state;
        }
    }

    private int getState(Uid u) {
        byte[] value = stateMap.get(u);
        if(value == null) {
            return ObjectStore.OS_UNKNOWN;
        } else {
            return (int)value[0];
        }
    }
}
