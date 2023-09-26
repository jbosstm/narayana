/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import javax.transaction.xa.XAResource;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.hp.mwtests.ts.jta.common.CrashXAResource;

@RunWith(BMUnitRunner.class)
@BMScript("recovery")
public class CrashRecovery
{
    @Test
    public void test() throws Exception
    {
        // this test is supposed to leave a record around in the log store during a commit long enough
        // that the periodic recovery thread runs and detects it. rather than rely on delays to make
        // this happen (placing us at the mercy of the scheduler) we use a byteman script to enforce
        // the thread sequence we need

        // set the smallest possible backoff period so we don't have to wait too long for the test to run
        
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        // start the recovery manager

        RecoveryManager.manager().initialize();

        // ok, now drive a TX to completion. the script should ensure that the recovery 

        XAResource firstResource = new CrashXAResource();
        XAResource secondResource = new CrashXAResource();

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        theTransaction.enlistResource(firstResource);
        theTransaction.enlistResource(secondResource);

        tm.commit();
    }
}