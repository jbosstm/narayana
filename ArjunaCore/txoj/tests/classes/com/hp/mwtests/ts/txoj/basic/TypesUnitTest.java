/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.basic;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.txoj.ConflictType;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.arjuna.ats.txoj.LockStatus;

public class TypesUnitTest
{
    @Test
    public void test()
    {
        assertEquals(ConflictType.stringForm(ConflictType.COMPATIBLE), "ConflictType.COMPATIBLE");
        assertEquals(ConflictType.stringForm(ConflictType.CONFLICT), "ConflictType.CONFLICT");
        assertEquals(ConflictType.stringForm(ConflictType.PRESENT), "ConflictType.PRESENT");
        assertEquals(ConflictType.stringForm(-1), "Unknown");
        
        ConflictType.print(new PrintWriter(System.err), ConflictType.COMPATIBLE);
        
        assertEquals(LockMode.stringForm(LockMode.INTENTION_READ), "LockMode.INTENTION_READ");
        assertEquals(LockMode.stringForm(LockMode.INTENTION_WRITE), "LockMode.INTENTION_WRITE");
        assertEquals(LockMode.stringForm(LockMode.READ), "LockMode.READ");
        assertEquals(LockMode.stringForm(LockMode.WRITE), "LockMode.WRITE");
        assertEquals(LockMode.stringForm(LockMode.UPGRADE), "LockMode.UPGRADE");
        assertEquals(LockMode.stringForm(-1), "Unknown");
        
        LockMode.print(new PrintWriter(System.err), LockMode.INTENTION_READ);
        
        assertEquals(LockResult.stringForm(LockResult.GRANTED), "LockResult.GRANTED");
        assertEquals(LockResult.stringForm(LockResult.REFUSED), "LockResult.REFUSED");
        assertEquals(LockResult.stringForm(LockResult.RELEASED), "LockResult.RELEASED");
        assertEquals(LockResult.stringForm(-1), "Unknown");
        
        LockResult.print(new PrintWriter(System.err), LockResult.GRANTED);
        
        assertEquals(LockStatus.printString(LockStatus.LOCKFREE), "LockStatus.LOCKFREE");
        assertEquals(LockStatus.printString(LockStatus.LOCKHELD), "LockStatus.LOCKHELD");
        assertEquals(LockStatus.printString(LockStatus.LOCKRETAINED), "LockStatus.LOCKRETAINED");
        assertEquals(LockStatus.printString(-1), "Unknown");
        
        LockStatus.print(new PrintWriter(System.err), LockStatus.LOCKFREE);
    }
}