/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.abstractrecords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.CadaverActivationRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

public class CadaverActivationUnitTest
{
    @Test
    public void test ()
    {
        ParticipantStore store = StoreManager.setupStore(null, StateType.OS_UNSHARED);

        CadaverActivationRecord cr = new CadaverActivationRecord(new ExtendedObject());
        
        assertTrue(cr.propagateOnAbort());
        assertTrue(cr.propagateOnCommit());
        assertEquals(cr.typeIs(), RecordType.ACTIVATION);
        
        assertTrue(cr.type() != null);
        assertEquals(cr.doSave(), false);

        assertFalse(cr.shouldReplace(new PersistenceRecord(new OutputObjectState(), store, new ExtendedObject())));
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedAbort(), TwoPhaseOutcome.FINISH_OK);

        cr = new CadaverActivationRecord(new ExtendedObject());
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        
        cr = new CadaverActivationRecord(new ExtendedObject());

        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.topLevelAbort(), TwoPhaseOutcome.FINISH_OK);
 
        cr = new CadaverActivationRecord(new ExtendedObject());
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
    }
}