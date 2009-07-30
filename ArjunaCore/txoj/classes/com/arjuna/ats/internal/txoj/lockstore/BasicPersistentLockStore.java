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
 * $Id: BasicPersistentLockStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.lockstore;

import com.arjuna.ats.txoj.TxOJNames;
import com.arjuna.ats.txoj.lockstore.LockStoreImple;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.txoj.common.Environment;
import com.arjuna.ats.txoj.common.Configuration;
import com.arjuna.ats.txoj.common.txojPropertyManager;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.ats.txoj.logging.txojLogger;
import com.arjuna.ats.txoj.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.txoj.exceptions.LockStoreException;

/**
 * An implementation of the lock store which saves locks into files
 * on the local machine. Thus, multiple JVMs may share locks by reading
 * and writing to the same files.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: BasicPersistentLockStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class BasicPersistentLockStore extends LockStoreImple
{

    /*
     * Ignore key as we can make use of the basic type information for
     * this type of store. Really only need it for shared memory.
     */

    public BasicPersistentLockStore (String key)
    {
	if (txojLogger.aitLogger.debugAllowed())
	{
	    txojLogger.aitLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_LOCK_STORE, "BasicPersistentLockStore.BasicPersistentLockStore("+key+")");
	}

        lockStoreLocation = txojPropertyManager.getTxojEnvironmentBean().getLockStoreDir();
	    com.arjuna.ats.txoj.common.Configuration.setLockStoreRoot(lockStoreLocation);

	_key = lockStoreLocation;

	/*
	 * Use the ShadowingStore since it has file-level locking which
	 * we require. The default object store assumes locking is provided
	 * entirely by the object.
	 */

	_lockStore = new ObjectStore(ArjunaNames.Implementation_ObjectStore_ShadowingStore(), _key);
    }

public InputObjectState read_state (Uid u, String tName) throws LockStoreException
    {
	if (txojLogger.aitLogger.debugAllowed())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_LOCK_STORE, "BasicPersistentLockStore.read_state("+u+", "+tName+")");
	}

	try
	{
	    return _lockStore.read_committed(u, tName);
	}
	catch (ObjectStoreException e)
	{
	    throw new LockStoreException("Persistent store error.", e);
	}
    }

public boolean remove_state (Uid u, String tName)
    {
	if (txojLogger.aitLogger.debugAllowed())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_LOCK_STORE, "BasicPersistentLockStore.remove_state("+u+", "+tName+")");
	}

	try
	{
	    return _lockStore.remove_committed(u, tName);
	}
	catch (ObjectStoreException e)
	{
	    return false;
	}
    }

public boolean write_committed (Uid u, String tName, OutputObjectState state)
    {
	if (txojLogger.aitLogger.debugAllowed())
	{
	    txojLogger.aitLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_LOCK_STORE,
				     "BasicPersistentLockStore.write_committed("+u+", "+tName+", "+state+")");
	}

	try
	    {
		return _lockStore.write_committed(u, tName, state);
	}
	catch (ObjectStoreException e)
	{
	    return false;
	}
    }

public ClassName className ()
    {
	return TxOJNames.Implementation_LockStore_BasicPersistentLockStore();
    }

public static ClassName name ()
    {
	return TxOJNames.Implementation_LockStore_BasicPersistentLockStore();
    }

public static final BasicPersistentLockStore create (Object[] param)
    {
	if (param == null)
	    return null;

	String key = (String) param[0];

	return new BasicPersistentLockStore(key);
    }

private String      _key;
private ObjectStore _lockStore;

private static String lockStoreLocation = null;

}
