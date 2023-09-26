/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jts.recovery.contact.ExpiredContactScanner;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ExpiredContactUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ExpiredContactScanner scanner = new ExpiredContactScanner();
        
        scanner.scan();
        
        assertTrue(scanner.toBeUsed());
    }
}