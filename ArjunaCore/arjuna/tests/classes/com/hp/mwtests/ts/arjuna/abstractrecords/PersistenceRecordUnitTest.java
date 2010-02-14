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
package com.hp.mwtests.ts.arjuna.abstractrecords;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

import static org.junit.Assert.*;

public class PersistenceRecordUnitTest
{
    @Test
    public void test ()
    {
        PersistenceRecord cr = new PersistenceRecord(new OutputObjectState(), TxControl.getStore(), new ExtendedObject());
        
        arjPropertyManager.getCoordinatorEnvironmentBean().setClassicPrepare(true);
        
        assertFalse(cr.propagateOnAbort());
        assertTrue(cr.propagateOnCommit());
        assertEquals(cr.typeIs(), RecordType.PERSISTENCE);
        
        assertTrue(cr.type() != null);
        assertEquals(cr.doSave(), true);

        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelAbort(), TwoPhaseOutcome.FINISH_ERROR);
 
        cr = new PersistenceRecord(new OutputObjectState(), TxControl.getStore(), new ExtendedObject());
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);

        cr.print(new PrintWriter(new ByteArrayOutputStream()));
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(cr.save_state(os, ObjectType.ANDPERSISTENT));
        assertTrue(cr.restore_state(new InputObjectState(os), ObjectType.ANDPERSISTENT));
        
        assertEquals(cr.topLevelCleanup(), TwoPhaseOutcome.FINISH_OK);
    }
}
