/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jta.resources.jts.LocalCleanupSynchronization;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

public class SynchronizationUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TransactionImple tx = new TransactionImple();
        LocalCleanupSynchronization sync = new LocalCleanupSynchronization(tx);
        
        assertTrue(sync.beforeCompletion());
        assertTrue(sync.afterCompletion(ActionStatus.COMMITTED));
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        assertTrue(sync.compareTo(new LocalCleanupSynchronization(null)) != 0);
    }
}