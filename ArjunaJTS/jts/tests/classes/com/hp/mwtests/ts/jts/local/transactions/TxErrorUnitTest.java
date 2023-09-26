/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import org.junit.Test;

import com.arjuna.ats.jts.exceptions.TxError;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class TxErrorUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TxError tx = new TxError();
        
        tx = new TxError("foobar");
    }
}