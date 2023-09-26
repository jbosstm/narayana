/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.arjuna.tools;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.arjuna.tools.log.EditableAtomicAction;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;

class DummyMap2 implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return CrashRecord.class;
    }

    public int getType ()
    {
        return RecordType.USER_DEF_FIRST0;
    }    
}

public class LogEditorUnitTest
{
    @Test
    public void test () throws Exception
    {
        String localOSRoot = "foobar";
        String objectStoreDir = System.getProperty("java.io.tmpdir")+"/bar";

        arjPropertyManager.getObjectStoreEnvironmentBean().setLocalOSRoot(localOSRoot);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreDir(objectStoreDir);
        
        // dummy to set up ObjectStore

        AtomicAction A = new AtomicAction();

        A.begin();

        A.add(new CrashRecord(CrashRecord.CrashLocation.NoCrash,
                CrashRecord.CrashType.Normal));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));

        int outcome = A.commit();

        System.out.println("Transaction " + A + " committed with "
                + ActionStatus.stringForm(outcome));

        AtomicAction B = new AtomicAction();

        B.begin();

        B.add(new CrashRecord(CrashRecord.CrashLocation.NoCrash,
                CrashRecord.CrashType.Normal));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.Normal));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));

        outcome = B.commit();

        System.out.println("Transaction " + B + " committed with "
                + ActionStatus.stringForm(outcome));
        
        RecordTypeManager.manager().add(new DummyMap2());
        
        EditableAtomicAction eaa = new EditableAtomicAction(B.get_uid());
        
        assertTrue(eaa.toString() != null);
        
        eaa.moveHeuristicToPrepared(1);
        
        try
        {
            eaa.moveHeuristicToPrepared(-1);
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        eaa = new EditableAtomicAction(A.get_uid());
        
        eaa.deleteHeuristicParticipant(0);
        
        try
        {
            eaa.deleteHeuristicParticipant(-1);
            fail();
        }
        catch (final Exception ex)
        {
        }
    }
}