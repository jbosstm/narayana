/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import org.junit.Test;

import com.arjuna.ats.internal.jts.recovery.transactions.ServerTransactionRecoveryModule;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransactionRecoveryModule module = new ServerTransactionRecoveryModule();
        
        module.periodicWorkFirstPass();
        module.periodicWorkSecondPass();
    }
}