/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.jts.extensions.DebugTxAssociation;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class TxAssociationUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        DebugTxAssociation tx = new DebugTxAssociation();
        
        tx.begin(null);
        tx.commit(null);
        tx.rollback(null);
        tx.suspend(null);
        tx.resume(null);
        
        assertEquals(tx.name(), "Debug");
    }
}