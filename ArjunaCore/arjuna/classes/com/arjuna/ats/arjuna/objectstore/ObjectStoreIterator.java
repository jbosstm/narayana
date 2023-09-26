/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore;

import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * Class that allows us to iterate through instances of objects that may be
 * stored within a specific object store.
 */

public class ObjectStoreIterator
{

    public ObjectStoreIterator(RecoveryStore recoveryStore, String tName) throws ObjectStoreException
    {
        recoveryStore.allObjUids(tName, uidList);
    }

    /**
     * return the Uids from the list one at a time. ObjStore returns either null
     * list or a list terminated by the NIL_UID. Use the latter to return 0 (for
     * end of list)
     * @throws IOException 
     */

    public final synchronized Uid iterate () throws IOException
    {
        Uid newUid = null;

        newUid = UidHelper.unpackFrom(uidList);

        return newUid;
    }

    private InputObjectState uidList = new InputObjectState();

}