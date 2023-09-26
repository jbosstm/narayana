/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.jts.utils.TxStoreLog;

public class TxStoreLogUnitTest
{
    @Test
    public void test()
    {
        TxStoreLog log = new TxStoreLog();
        InputObjectState is = new InputObjectState();
        
        assertTrue(TxStoreLog.getTransactions(is));  
    }
}