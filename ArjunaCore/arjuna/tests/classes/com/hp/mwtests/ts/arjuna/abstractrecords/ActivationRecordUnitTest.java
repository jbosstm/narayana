/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.abstractrecords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.ActivationRecord;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

public class ActivationRecordUnitTest
{
    @Test
    public void test ()
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        
        A.begin();
        B.begin();
        
        ActivationRecord cr = new ActivationRecord(ObjectType.ANDPERSISTENT, new ExtendedObject(), B);
        
        assertFalse(cr.propagateOnAbort());
        assertTrue(cr.propagateOnCommit());
        assertEquals(cr.typeIs(), RecordType.ACTIVATION);
        
        assertTrue(cr.type() != null);
        assertEquals(cr.doSave(), false);
        
        assertEquals((Integer) cr.value(), Integer.valueOf(ObjectType.ANDPERSISTENT));

        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedAbort(), TwoPhaseOutcome.FINISH_OK);

        cr = new ActivationRecord(ObjectType.ANDPERSISTENT, new ExtendedObject(), B);
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        
        B.abort();
        
        cr = new ActivationRecord(ObjectType.ANDPERSISTENT, new ExtendedObject(), A);

        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelAbort(), TwoPhaseOutcome.FINISH_OK);
 
        cr = new ActivationRecord(ObjectType.ANDPERSISTENT, new ExtendedObject(), A);
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
        
        cr = new ActivationRecord();
        
        cr.merge(new ActivationRecord());
        cr.alter(new ActivationRecord());
        
        assertTrue(cr.save_state(new OutputObjectState(), ObjectType.ANDPERSISTENT));
        assertFalse(cr.restore_state(new InputObjectState(), ObjectType.ANDPERSISTENT));
        
        A.abort();
    }
}