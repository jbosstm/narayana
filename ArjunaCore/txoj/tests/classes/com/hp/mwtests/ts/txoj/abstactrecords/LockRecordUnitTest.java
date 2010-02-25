/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.txoj.abstactrecords;

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

import static org.junit.Assert.*;

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
