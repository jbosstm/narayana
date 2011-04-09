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
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.HeuristicNotification;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;
import com.hp.mwtests.ts.arjuna.resources.HeuristicRecord;
import com.hp.mwtests.ts.arjuna.resources.SyncRecord;

import org.junit.Test;
import static org.junit.Assert.*;

public class HeuristicNotificationUnitTest
{
    class DummyHeuristic extends HeuristicNotification
    {
        public int getStatus ()
        {
            return _status;
        }
        
        @Override
        public void heuristicOutcome (int actionStatus)
        {
            _status = actionStatus;
        }

        @Override
        public int compareTo (Object o)
        {
            return 0;
        }
        
        private int _status = -1;
    }
    
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
