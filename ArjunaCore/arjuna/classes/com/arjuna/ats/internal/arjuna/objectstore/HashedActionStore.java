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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: HashedActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * The basic action store implementations store the object states in a separate
 * file within the same directory in the object store, determined by the
 * object's type. However, as the number of file entries within the directory
 * increases, so does the search time for finding a specific file. The HashStore
 * implementation hashes object states over many different sub-directories to
 * attempt to keep the number of files in a given directory low, thus improving
 * performance as the number of object states grows. Currently the hash number
 * is set for both user hashed stores and action hashed stores.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HashedActionStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.1.
 */

public class HashedActionStore extends HashedStore
{

    public int typeIs ()
    {
        return ObjectStoreType.HASHED_ACTION;
    }

    /*
     * Protected constructors and destructor
     */

    public HashedActionStore()
    {
        this(ObjectStore.OS_SHARED);
    }

    public HashedActionStore(int shareStatus)
    {
        super(shareStatus);

        if (tsLogger.arjLogger.isDebugEnabled())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
                    VisibilityLevel.VIS_PROTECTED,
                    FacilityCode.FAC_OBJECT_STORE,
                    "HashedStore.HashedActionStore( " + shareStatus + " )");
        }
        
        try
        {
            setupStore(arjPropertyManager.getObjectStoreEnvironmentBean().getLocalOSRoot());
        }
        catch (ObjectStoreException e)
        {
            throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(),
                    e);
        }
    }

    public HashedActionStore(String locationOfStore)
    {
        this(locationOfStore, ObjectStore.OS_SHARED);

        if (tsLogger.arjLogger.isDebugEnabled())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
                                     FacilityCode.FAC_OBJECT_STORE,
                                     "HashedStore.HashedActionStore("+locationOfStore+")");
        }

        try
        {
            setupStore(locationOfStore);
        }
        catch (ObjectStoreException e)
        {
            tsLogger.arjLogger.warn(e);

            throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
        }
    }

    public HashedActionStore(String locationOfStore, int shareStatus)
    {
        super(shareStatus);

        if (tsLogger.arjLogger.isDebugEnabled())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS,
                    VisibilityLevel.VIS_PROTECTED,
                    FacilityCode.FAC_OBJECT_STORE,
                    "HashedStore.HashedActionStore(" + locationOfStore + ")");
        }

        try
        {
            setupStore(locationOfStore);
        }
        catch (ObjectStoreException e)
        {
            tsLogger.arjLogger.warn(e);

            throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(),
                    e);
        }
    }

    protected synchronized boolean setupStore (String location)
            throws ObjectStoreException
    {
        if (!checkSync)
        {
            if (arjPropertyManager.getObjectStoreEnvironmentBean()
                    .isTransactionSync())
            {
                syncOn();
            }
            else
            {
                syncOff();
            }
        }

        checkSync = true;

        return super.setupStore(location);
    }

    private static boolean checkSync = false;

}
