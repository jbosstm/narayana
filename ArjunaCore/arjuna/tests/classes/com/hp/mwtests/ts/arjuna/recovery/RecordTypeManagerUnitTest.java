/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;


class DummyMap implements RecordTypeMap
{

    public Class<? extends AbstractRecord> getRecordClass ()
    {
        return PersistenceRecord.class;
    }

    public int getType ()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
public class RecordTypeManagerUnitTest
{
    @Test
    public void test()
    {
        DummyMap map = new DummyMap();
        RecordTypeManager.manager().add(map);
        
        assertEquals(RecordTypeManager.manager().getClass(0), PersistenceRecord.class);
        assertEquals(RecordTypeManager.manager().getType(PersistenceRecord.class), 0);
        
        RecordTypeManager.manager().remove(map);
    }
}