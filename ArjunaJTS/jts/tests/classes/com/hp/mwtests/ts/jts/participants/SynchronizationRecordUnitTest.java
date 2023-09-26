/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.participants;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.resources.SynchronizationRecord;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class SynchronizationRecordUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        demosync theSync = new demosync();
        SynchronizationRecord sync = new SynchronizationRecord(theSync.getReference(), false);
        
        assertTrue(sync.contents() == theSync.getReference());
        assertTrue(sync.get_uid() != Uid.nullUid());
    }
    
    @Test
    public void testCompare () throws Exception
    {
        demosync theSync = new demosync();
        SynchronizationRecord sync1 = new SynchronizationRecord(theSync.getReference(), false);
        SynchronizationRecord sync2 = new SynchronizationRecord(theSync.getReference(), true);
        
        assertEquals(sync1.compareTo(sync2), -1);    
        assertEquals(sync2.compareTo(sync1), 1);
        assertEquals(sync1.compareTo(sync1), 0);
        
        sync2 = new SynchronizationRecord(theSync.getReference(), false);
        
        assertEquals(sync1.compareTo(sync2), -1);
    }
}