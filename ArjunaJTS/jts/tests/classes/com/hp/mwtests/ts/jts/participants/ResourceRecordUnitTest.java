/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.participants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;

import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ResourceRecordUnitTest extends TestBase
{
    @Test
    public void testDefault () throws Exception
    {
        ResourceRecord rec = new ResourceRecord();
        
        rec.setValue(null);
        
        assertEquals(rec.getRCUid(), Uid.nullUid());
        assertEquals(rec.value(), null);
        assertTrue(rec.type().length() > 0);
        assertEquals(rec.typeIs(), RecordType.OTS_RECORD);
        assertEquals(rec.resourceHandle(), null);
        
        rec.alter(null);
        rec.merge(null);
        
        ResourceRecord.remove(rec);
    }
    
    @Test
    public void test () throws Exception
    {
        DemoResource res = new DemoResource();
        ResourceRecord rec = new ResourceRecord(false, res.getResource(), new Uid());
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream());
        
        rec.print(writer);
        
        assertTrue(rec.resourceHandle() != null);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(rec.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(rec.restore_state(is, ObjectType.ANDPERSISTENT));  
    }
}