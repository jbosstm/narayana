/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import org.junit.Test;

import com.arjuna.ats.internal.jts.recovery.transactions.TopLevelTransactionRecoveryModule;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class TopLevelTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TopLevelTransactionRecoveryModule module = new TopLevelTransactionRecoveryModule();
        
        module.periodicWorkFirstPass();
        module.periodicWorkSecondPass();
    }
}