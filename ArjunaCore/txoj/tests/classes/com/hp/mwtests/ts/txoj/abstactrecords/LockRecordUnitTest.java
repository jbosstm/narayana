/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.abstactrecords;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class LockRecordUnitTest
{
    @Test
    public void test ()
    {
	LockRecord lr = new LockRecord();
	
	assertEquals(lr.lockType(), null);
	assertTrue(lr.save_state(new OutputObjectState(), ObjectType.ANDPERSISTENT));
	assertFalse(lr.restore_state(new InputObjectState(), ObjectType.ANDPERSISTENT));
	assertEquals(lr.value(), null);
	
	lr.setValue(null);
	
	assertEquals(lr.nestedAbort(), TwoPhaseOutcome.FINISH_ERROR);
	assertEquals(lr.nestedCommit(), TwoPhaseOutcome.FINISH_ERROR);
	assertEquals(lr.topLevelAbort(), TwoPhaseOutcome.FINISH_ERROR);
        assertEquals(lr.topLevelCommit(), TwoPhaseOutcome.FINISH_ERROR);
        
        lr = new LockRecord(new AtomicObject(), new AtomicAction());
        
        assertTrue(lr.toString() != null);
        
        lr.print(new PrintWriter(new ByteArrayOutputStream()));
        
        assertTrue(lr.type() != null);
        
        lr.merge(null);
        lr.alter(null);
    }
}