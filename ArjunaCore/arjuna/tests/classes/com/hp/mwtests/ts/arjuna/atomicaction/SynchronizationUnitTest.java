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
package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.hp.mwtests.ts.arjuna.resources.SyncRecord;

import org.junit.Test;
import static org.junit.Assert.*;

public class SynchronizationUnitTest
{
    @Test
    public void tes () throws Exception
    {
        AtomicAction A = new AtomicAction();
        SyncRecord sr = new SyncRecord();
        
        A.begin();
        
        assertEquals(A.addSynchronization(sr), AddOutcome.AR_ADDED);
        assertEquals(A.getSynchronizations().size(), 1);
        
        A.commit();
        
        assertTrue(sr.called());
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        assertEquals(A.addSynchronization(null), AddOutcome.AR_REJECTED);
        
        A.abort();
    }
}
