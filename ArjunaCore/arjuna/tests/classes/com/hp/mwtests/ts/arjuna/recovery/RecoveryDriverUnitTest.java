/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.Before;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryDriver;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class RecoveryDriverUnitTest
{
    @Before
    public void enableSocketBasedRecovery()
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryListener(true);
    }

    @Test
    public void testInvalid () throws Exception
    {
        RecoveryDriver rd = new RecoveryDriver(0, "non-existent-hostname");
        
        try
        {
            rd.asynchronousScan();

            fail("Recovery driver asynchronously calls to a non-existent host:port at 'non-existent-hostname:0'. Failure is expected.");
        }
        catch (final Exception expected)
        {
        }
        
        try
        {
            rd.synchronousScan();

            fail("Recovery driver synchronously calls to a non-existent host:port at 'non-existent-hostname:0'. Failure is expected.");
        }
        catch (final Exception expected)
        {
        }
    }
    
    @Test
    public void testValid () throws Exception
    {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager rm = RecoveryManager.manager();       
        
        rm.scan(null);
        
        RecoveryDriver rd = new RecoveryDriver(RecoveryManager.getRecoveryManagerPort(), recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress(), 100000);
        
        assertTrue(rd.asynchronousScan());
        assertTrue(rd.synchronousScan());
    }
}