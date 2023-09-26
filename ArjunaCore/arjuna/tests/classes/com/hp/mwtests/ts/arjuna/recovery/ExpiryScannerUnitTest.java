/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner;

public class ExpiryScannerUnitTest
{
    @Test
    public void test () throws Exception
    {
        ExpiredTransactionStatusManagerScanner et = new ExpiredTransactionStatusManagerScanner();
        
        assertTrue(et.toBeUsed());
        
        et.scan();
    }
}