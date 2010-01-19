/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import java.io.File;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

/**
 * The object store interface is the application's route to using
 * a specific object store implementation. The interface dynamically
 * binds to an implementation of the right type.
 * 
 * This is the base class from which all object store types are derived.
 * Note that because object store instances are stateless, to improve
 * efficiency we try to only create one instance of each type per process.
 * Therefore, the create and destroy methods are used instead of new
 * and delete. If an object store is accessed via create it *must* be
 * deleted using destroy. Of course it is still possible to make use of
 * new directly.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.objectstore.ObjectStore_1 [com.arjuna.ats.arjuna.objectstore.ObjectStore_1] - No implementation!
 */

public abstract class ObjectStore implements BaseStore, ParticipantStore, RecoveryStore, TxLog
{
    public boolean allObjUids (String s, InputObjectState buff) throws ObjectStoreException
    {
        return allObjUids(s, buff, StateStatus.OS_UNKNOWN);
    }

    /**
     * Some object store implementations may be running with automatic
     * sync disabled. Calling this method will ensure that any states are
     * flushed to disk.
     */

    public void sync () throws java.io.SyncFailedException, ObjectStoreException
    {
    }

    /**
     * @param localOSRoot the root of the store.
     *
     * @return the full path of the store. localOSRoot is always a relative
     * name. NOTE this path always ends in a /
     */

    public String locateStore (String localOSRoot) throws ObjectStoreException
    {
        if (_objectStoreRoot == null)
            _objectStoreRoot = arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot();

        if (_objectStoreDir == null)
        {
            _objectStoreDir = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
            if (_objectStoreDir == null || _objectStoreDir.length() == 0)
                throw new ObjectStoreException("object store location property not set.");

            if (!_objectStoreDir.endsWith(File.separator))
                _objectStoreDir = _objectStoreDir + File.separator;

            /*
             * We use the classname of the object store implementation to
             * specify the directory for the object store.
             */

            _objectStoreDir = _objectStoreDir + this.getClass().getSimpleName();
        }

        String toReturn = null;

        if ((localOSRoot == null) || (localOSRoot.length() == 0))
        {
            if ((_objectStoreRoot != null) && (_objectStoreRoot.length() > 0))
                localOSRoot = _objectStoreRoot;
            else
                localOSRoot = "defaultStore"+File.separator;
        }

        if ((localOSRoot != null) && (localOSRoot.length() > 0))
            toReturn = _objectStoreDir + File.separator+localOSRoot;

        if (!toReturn.endsWith(File.separator))
            toReturn = toReturn + File.separator;

        return toReturn;
    }

    /**
     * Does this store need to do the full write_uncommitted/commit protocol?
     *
     * @return <code>true</code> if full commit is needed, <code>false</code>
     * otherwise.
     */

    public boolean fullCommitNeeded ()
    {
        return true;
    }

    /**
     * Is the current state of the object the same as that provided as the last
     * parameter?
     *
     * @param u The object to work on.
     * @param tn The type of the object.
     * @param st The expected type of the object.
     *
     * @return <code>true</code> if the current state is as expected,
     * <code>false</code> otherwise.
     */

    public boolean isType (Uid u, String tn, int st) throws ObjectStoreException
    {
        return ((currentState(u, tn) == st) ? true : false);
    }

    /**
     * Initialise the object store.
     */

    public void initialise (Uid u, String tn)
    {
    }

    /**
     * Pack up the object store state. May be used to ship an entire
     * object store across the network, or persist a volatile object
     * store.
     */

    public void pack (OutputBuffer buff) throws IOException
    {
    }

    /**
     * Unpack an object store.
     */

    public void unpack (InputBuffer buff) throws IOException
    {
    }

    public final int shareState ()
    {
        return shareStatus;
    }

    public final String storeDir ()
    {
        return _objectStoreDir;
    }

    public final String storeRoot ()
    {
        return _objectStoreRoot;
    }

    protected ObjectStore ()
    {
        this(StateType.OS_SHARED);
    }

    protected ObjectStore (int ss)
    {
        shareStatus = ss;
    }

    /**
     * Suppress directories of the specified type from
     * allTypes etc?
     */

    protected abstract boolean supressEntry (String name);

    /**
     * Given a type id which is possibly hidden (e.g., has a ! at the
     * end), make it a valid Uid so we can return it.
     */

    protected String revealedId (String name)
    {
        return name;
    }

    protected int shareStatus = arjPropertyManager.getObjectStoreEnvironmentBean().getShare(); // is the implementation sharing states between VMs?

    private String _objectStoreRoot = arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot();
    private String _objectStoreDir = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
}

