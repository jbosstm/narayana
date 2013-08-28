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

import com.hp.mwtests.ts.arjuna.resources.DummyHeuristic;
import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.HeuristicRecord;

public class HeuristicNotificationUnitTest
{
    @Test
    public void test () throws Exception
    {
        AtomicAction A = new AtomicAction();
        DummyHeuristic dh = new DummyHeuristic();
        
        A.begin();
        
        A.add(new BasicRecord());
        A.add(new BasicRecord());
        A.add(new HeuristicRecord());
        
        A.addSynchronization(dh);
        
        A.commit(false);
        
        assertEquals(TwoPhaseOutcome.HEURISTIC_MIXED, dh.getStatus());
    }
}
