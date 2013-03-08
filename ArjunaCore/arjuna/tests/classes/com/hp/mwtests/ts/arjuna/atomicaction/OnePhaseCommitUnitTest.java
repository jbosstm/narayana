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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.OnePhaseAbstractRecord;

public class OnePhaseCommitUnitTest
{
    @Test
    public void testBasic () throws Exception
    {
        AtomicAction A = new AtomicAction();
        OnePhaseAbstractRecord rec = new OnePhaseAbstractRecord();
        
        A.begin();
        
        A.add(rec);
        
        A.commit();
        
        assertTrue(rec.onePhaseCalled());
    }
    
    @Test
    public void testFailed () throws Exception
    {
        AtomicAction A = new AtomicAction();
        OnePhaseAbstractRecord rec1 = new OnePhaseAbstractRecord();
        BasicRecord rec2 = new BasicRecord();
        
        A.begin();
        
        A.add(rec1);
        A.add(rec2);
        
        A.commit();
        
        assertTrue(!rec1.onePhaseCalled());
    }
    
    @Test
    public void testDynamic () throws Exception
    {
        AtomicAction A = new AtomicAction();
        OnePhaseAbstractRecord rec1 = new OnePhaseAbstractRecord();
        OnePhaseAbstractRecord rec2 = new OnePhaseAbstractRecord();
        
        A.begin();
        
        /*
         * Because these are the same record type, we know that they will
         * be called in the order in which they were registered.
         * 
         * There are two records, so 1PC will not be triggered initially. But
         * the first record will return read-only from prepare, which will
         * then trigger 1PC to happen dynamically.
         */
        
        A.add(rec1);
        A.add(rec2);
        
        A.commit();
        
        assertTrue(!rec1.onePhaseCalled());
        assertTrue(rec2.onePhaseCalled());
    }
}
