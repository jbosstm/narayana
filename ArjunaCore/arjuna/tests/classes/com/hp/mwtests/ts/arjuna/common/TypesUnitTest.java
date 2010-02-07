/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.internal.arjuna.abstractrecords.ActivationRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;

import static org.junit.Assert.*;

public class TypesUnitTest
{
    @Test
    public void testObjectModel ()
    {
        new ObjectModel();
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        ObjectModel.print(pw, ObjectModel.MULTIPLE);
        ObjectModel.print(pw, ObjectModel.SINGLE);
    }
    
    @Test
    public void testObjectStatus ()
    {
        new ObjectStatus();
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        ObjectStatus.print(pw, ObjectStatus.ACTIVE);
        
        assertEquals(ObjectStatus.toString(ObjectStatus.ACTIVE_NEW), "ACTIVE_NEW");
    }
    
    @Test
    public void testObjectType ()
    {
        new ObjectType();
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        ObjectType.print(pw, ObjectType.ANDPERSISTENT);
        
        assertEquals(ObjectType.toString(ObjectType.NEITHER), "NEITHER");
    }
    
    @Test
    public void testStateStatus ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        StateStatus.printStateStatus(pw, StateStatus.OS_COMMITTED);
        
        assertEquals(StateStatus.stateStatusString(StateStatus.OS_HIDDEN), "StateStatus.OS_HIDDEN");
    }
    
    @Test
    public void testStateType ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        StateType.printStateType(pw, StateType.OS_INVISIBLE);
        
        assertEquals(StateType.stateTypeString(StateType.OS_ORIGINAL), "StateType.OS_ORIGINAL");
    }
    
    @Test
    public void testActionType ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        ActionType.print(pw, ActionType.NESTED);
    }
    
    @Test
    public void testAddOutcome ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        AddOutcome.print(pw, AddOutcome.AR_ADDED);
        
        assertEquals(AddOutcome.printString(AddOutcome.AR_DUPLICATE), "AddOutcome.AR_DUPLICATE");
    }
    
    @Test
    public void testTwoPhaseOutcome ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        TwoPhaseOutcome.print(pw, TwoPhaseOutcome.FINISH_ERROR);
        
        assertEquals(TwoPhaseOutcome.stringForm(TwoPhaseOutcome.FINISH_OK), "TwoPhaseOutcome.FINISH_OK");
        
        TwoPhaseOutcome o = new TwoPhaseOutcome(TwoPhaseOutcome.HEURISTIC_COMMIT);
        
        o.setOutcome(TwoPhaseOutcome.NOT_PREPARED);
        
        assertEquals(o.getOutcome(), TwoPhaseOutcome.NOT_PREPARED);
    }
    
    @Test
    public void testActionStatus ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        ActionStatus.print(pw, ActionStatus.ABORT_ONLY);
        
        assertEquals(ActionStatus.stringForm(ActionStatus.ABORTED), "ActionStatus.ABORTED");
    }
    
    @Test
    public void testRecordType ()
    {
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        RecordType.print(pw, RecordType.ACTIVATION);
        
        assertEquals(RecordType.classToType(PersistenceRecord.class), RecordType.PERSISTENCE);
        assertEquals(RecordType.typeToClass(RecordType.ACTIVATION), ActivationRecord.class);
    }
}
