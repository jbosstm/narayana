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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.CadaverRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.DisposeRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;
import com.hp.mwtests.ts.arjuna.resources.ExtendedObject;

public class CadaverRecordUnitTest
{
    @Test
    public void test ()
    {
        ParticipantStore store = StoreManager.setupStore(null, StateType.OS_UNSHARED);
        
        CadaverRecord cr = new CadaverRecord(new OutputObjectState(), store, new ExtendedObject());
        
        assertTrue(cr.propagateOnAbort());
        assertTrue(cr.propagateOnCommit());
        assertEquals(cr.typeIs(), RecordType.PERSISTENCE);
        
        assertTrue(cr.type() != null);
        assertEquals(cr.doSave(), false);

        cr.merge(new PersistenceRecord(new OutputObjectState(), store, new ExtendedObject()));
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedAbort(), TwoPhaseOutcome.FINISH_OK);

        cr = new CadaverRecord(new OutputObjectState(), store, new ExtendedObject());
        cr.merge(new PersistenceRecord(new OutputObjectState(), store, new ExtendedObject()));
        
        assertEquals(cr.nestedPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(cr.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        
        cr = new CadaverRecord(new OutputObjectState(new Uid(), "foobar"), store, new ExtendedObject());
        cr.merge(new PersistenceRecord(new OutputObjectState(new Uid(), "foobar"), store, new ExtendedObject()));
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelAbort(), TwoPhaseOutcome.FINISH_OK);
 
        cr = new CadaverRecord(new OutputObjectState(new Uid(), "foobar"), store, new ExtendedObject());
        cr.merge(new PersistenceRecord(new OutputObjectState(new Uid(), "foobar"), store, new ExtendedObject()));
        cr.merge(new PersistenceRecord(new OutputObjectState(new Uid(), "foobar"), store, new ExtendedObject()));
        
        assertEquals(cr.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(cr.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);

        cr = new CadaverRecord();
        
        cr.print(new PrintWriter(new ByteArrayOutputStream()));
        
        assertFalse(cr.shouldMerge(new DisposeRecord()));
        assertFalse(cr.shouldReplace(new DisposeRecord()));
    }
}
