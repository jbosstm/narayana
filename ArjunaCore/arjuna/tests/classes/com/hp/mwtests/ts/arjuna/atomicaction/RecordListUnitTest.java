/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.internal.arjuna.abstractrecords.ActivationRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.DisposeRecord;

public class RecordListUnitTest
{
    @Test
    public void test () throws Exception
    {
        RecordList rl = new RecordList();
        DisposeRecord dr = new DisposeRecord();
        
        rl.insert(dr);
        
        assertEquals(rl.getFront(), dr);
        
        rl.insert(dr);
        
        assertEquals(rl.getRear(), dr);
        
        RecordList copy = new RecordList(rl);
        ActivationRecord ar = new ActivationRecord();
        
        rl.insert(ar);
        
        rl.print(new PrintWriter(new ByteArrayOutputStream()));
        
        assertTrue(rl.toString() != null);
        
        assertEquals(rl.getNext(dr), null);
        
        assertTrue(rl.peekFront() != null);
        assertTrue(rl.peekRear() != null);
        assertEquals(rl.peekNext(dr), null);
        
        assertTrue(rl.remove(dr));
    }
}