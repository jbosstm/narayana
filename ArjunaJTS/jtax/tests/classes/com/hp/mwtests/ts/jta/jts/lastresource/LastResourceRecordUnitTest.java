/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.lastresource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jta.resources.jts.orbspecific.LastResourceRecord;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

public class LastResourceRecordUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TransactionImple tx = new TransactionImple();
        LastResourceRecord rec = new LastResourceRecord(tx, new DummyXA(false), tx.getTxId(), null);
        
        rec.commit();
        
        assertEquals(rec.prepare(), Vote.VoteCommit);
        
        assertTrue(rec.toString() != null);
        
        assertFalse(rec.saveRecord());
        
        assertTrue(rec.type() != null);
    }
}