/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.internal.jts.recovery.transactions.ExpiredServerScanner;
import com.arjuna.ats.internal.jts.recovery.transactions.ExpiredToplevelScanner;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ExpiredAssumedCompleteScannerUnitTest extends TestBase
{
    @Test
    public void testServerScanner () throws Exception
    {
        ExpiredServerScanner scanner = new ExpiredServerScanner();
        
        assertTrue(scanner.toBeUsed());
        
        scanner.scan();
    }
    
    @Test
    public void testToplevelScanner () throws Exception
    {
        ExpiredToplevelScanner scanner = new ExpiredToplevelScanner();
        
        assertTrue(scanner.toBeUsed());
        
        scanner.scan();
    }
}