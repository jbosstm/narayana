/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;

public class RemoveCachedTest
{
    @Test
    public void test() throws IOException, ObjectStoreException
    {
        boolean passed = true;
        RecoveryStore store = new CacheStore(new ObjectStoreEnvironmentBean());
        String type = "ArjunaMS/Destinations/a3d6227_dc656_3b77ce7e_2/Messages";
        InputObjectState buff = new InputObjectState();

        if (store.allObjUids(type, buff, StateStatus.OS_COMMITTED)) {
            Uid toRemove = new Uid(Uid.nullUid());

            do {
                toRemove = UidHelper.unpackFrom(buff);

                if (toRemove.notEquals(Uid.nullUid())) {
                    System.err.println("Removing " + toRemove + "\n");

                    if (store.remove_committed(toRemove, type))
                        passed = true;
                    else {
                        System.err.println("Failed for " + toRemove);

                        passed = false;
                    }
                }
            } while (toRemove.notEquals(Uid.nullUid()));
        }

        assertTrue(passed);
    }
}