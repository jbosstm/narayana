/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.abstractrecords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.RecoveryRecord;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

public class RecoveryRecordUnitTest
{
    @Test
    public void test ()
    {
        RecoveryRecord cr = new RecoveryRecord(new OutputObjectState(), new ExtendedObject());
        
        assertFalse(cr.propagateOnAbort());
        assertTrue(cr.propagateOnCommit());
        assertEquals(cr.typeIs(), RecordType.RECOVERY);
        
        assertTrue(cr.type() != null);
        assertEquals(cr.doSave(), false);
        assertTrue(cr.value() != null);
        
        cr.setValue(new OutputObjectState());

        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedAbort(), TwoPhaseOutcome.FINISH_ERROR);

        cr = new RecoveryRecord(new OutputObjectState(), new ExtendedObject());
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        
        cr = new RecoveryRecord(new OutputObjectState(), new ExtendedObject());

        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.topLevelAbort(), TwoPhaseOutcome.FINISH_ERROR);
 
        cr = new RecoveryRecord(new OutputObjectState(), new ExtendedObject());
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
        
        cr = new RecoveryRecord();
        
        cr.merge(new RecoveryRecord());
        cr.alter(new RecoveryRecord());
        
        assertTrue(cr.save_state(new OutputObjectState(), ObjectType.ANDPERSISTENT));
        assertFalse(cr.restore_state(new InputObjectState(), ObjectType.ANDPERSISTENT));
    }
}