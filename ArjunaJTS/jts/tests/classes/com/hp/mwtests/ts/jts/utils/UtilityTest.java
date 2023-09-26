/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Test;

import org.omg.CosTransactions.otid_t;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveryStatus;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;

public class UtilityTest
{
    @Test
    public void test()
    {
        Uid txId = new Uid();
        otid_t tid = Utility.uidToOtid(txId);
        Uid u = Utility.otidToUid(tid);

        assertTrue(txId.equals(u));
    }
    
    @Test
    public void testPrint ()
    {
        Utility.printStatus(new PrintWriter(System.err), org.omg.CosTransactions.Status.StatusNoTransaction);
        
        String vote = Utility.stringVote(org.omg.CosTransactions.Vote.VoteCommit);
        
        assertTrue(vote != null);
        assertEquals(vote, "CosTransactions::VoteCommit");
        
        String status = Utility.stringStatus(org.omg.CosTransactions.Status.StatusNoTransaction);
        
        assertTrue(status != null);
        assertEquals(status, "CosTransactions::StatusNoTransaction");
    }
    
    @Test
    public void testExceptions ()
    {
        ExceptionCodes x = new ExceptionCodes();
        
        for (int i = ExceptionCodes.OTS_GENERAL_BASE; i < ExceptionCodes.NO_TXCONTEXT; i++)
        {
            assertTrue(Utility.exceptionCode(i).length() > 1);
        }
    }
    
    @Test
    public void testReplayStatus ()
    {
        RecoveryStatus s = new RecoveryStatus();
        
        for (int i = RecoveryStatus.NEW; i < RecoveryStatus.REPLAY_FAILED + 1; i++)
        {
            assertTrue(RecoveryStatus.stringForm(i) != null);
        }
    }
}