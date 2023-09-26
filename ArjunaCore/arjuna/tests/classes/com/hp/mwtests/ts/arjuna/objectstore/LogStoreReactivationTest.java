/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

public class LogStoreReactivationTest
{
    @Test
    public void test()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setSynchronousRemoval(false);
        // the byteman script will enforce this
        //System.setProperty(Environment.TRANSACTION_LOG_PURGE_TIME, "1000000");  // essentially infinite

        AtomicAction A = new AtomicAction();
        Uid txId = A.get_uid();

        System.err.println("IMPORTANT: ignore warnings about USER_DEF_FIRST0 as they are expected due to BasicRecord usage!");

        A.begin();

        A.add(new BasicRecord());

        A.commit();

        RecoverAtomicAction rAA = new RecoverAtomicAction(txId, ActionStatus.COMMITTED);

        rAA.replayPhase2();
    }
}