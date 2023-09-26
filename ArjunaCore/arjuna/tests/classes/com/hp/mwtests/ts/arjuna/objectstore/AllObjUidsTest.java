/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

public class AllObjUidsTest
{
    @Test
    public void test() throws IOException, ObjectStoreException
    {
        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        String type = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/DummyAtomicAction";

        InputObjectState ios = new InputObjectState();
        recoveryStore.allObjUids(type, ios, StateStatus.OS_UNKNOWN);
        Uid uid = UidHelper.unpackFrom(ios);
        assertEquals(Uid.nullUid(), uid);

        ios = new InputObjectState();
        recoveryStore.allObjUids(type, ios, StateStatus.OS_COMMITTED);
        uid = UidHelper.unpackFrom(ios);
        assertEquals(Uid.nullUid(), uid);

        ios = new InputObjectState();
        recoveryStore.allObjUids(type, ios, StateStatus.OS_UNCOMMITTED);
        uid = UidHelper.unpackFrom(ios);
        assertEquals(Uid.nullUid(), uid);
    }
}