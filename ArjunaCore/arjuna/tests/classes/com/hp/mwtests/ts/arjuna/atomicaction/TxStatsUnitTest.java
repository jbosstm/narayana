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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TxStats;

import org.junit.Test;
import static org.junit.Assert.*;

public class TxStatsUnitTest
{
    @Test
    public void test() throws Exception
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);
        
        for (int i = 0; i < 100; i++)
        {
            AtomicAction A = new AtomicAction();
            AtomicAction B = new AtomicAction();
            
            A.begin();
            B.begin();
            
            B.commit();
            A.commit();
        }
        
        for (int i = 0; i < 100; i++)
        {
            AtomicAction A = new AtomicAction();
            
            A.begin();
            
            A.abort();
        }
        
        AtomicAction B = new AtomicAction();
        
        B.begin();
        
        assertTrue(TxStats.enabled());
        assertEquals(TxStats.getInstance().getNumberOfAbortedTransactions(), 100);
        assertEquals(TxStats.getInstance().getNumberOfApplicationRollbacks(), 100);
        assertEquals(TxStats.getInstance().getNumberOfCommittedTransactions(), 200);
        assertEquals(TxStats.getInstance().getNumberOfHeuristics(), 0);
        assertEquals(TxStats.getInstance().getNumberOfInflightTransactions(), 1);
        assertEquals(TxStats.getInstance().getNumberOfNestedTransactions(), 100);
        assertEquals(TxStats.getInstance().getNumberOfResourceRollbacks(), 0);
        assertEquals(TxStats.getInstance().getNumberOfTimedOutTransactions(), 0);
        assertEquals(TxStats.getInstance().getNumberOfTransactions(), 301);
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        TxStats.getInstance().printStatus(pw);
    }
}
