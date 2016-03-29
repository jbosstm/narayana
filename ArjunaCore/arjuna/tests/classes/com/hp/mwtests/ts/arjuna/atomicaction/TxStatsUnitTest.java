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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TxStats;

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
        assertEquals(100, TxStats.getInstance().getNumberOfAbortedTransactions());
        assertEquals(100, TxStats.getInstance().getNumberOfApplicationRollbacks());
        assertEquals(200, TxStats.getInstance().getNumberOfCommittedTransactions());
        assertEquals(0, TxStats.getInstance().getNumberOfHeuristics());
        assertEquals(1, TxStats.getInstance().getNumberOfInflightTransactions());
        assertEquals(100, TxStats.getInstance().getNumberOfNestedTransactions());
        assertEquals(0, TxStats.getInstance().getNumberOfResourceRollbacks());
        assertEquals(0, TxStats.getInstance().getNumberOfTimedOutTransactions());
        assertEquals(301, TxStats.getInstance().getNumberOfTransactions());
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        TxStats.getInstance().printStatus(pw);

        // https://issues.jboss.org/browse/JBTM-2643
        for (int i = 0; i < 100; i++) {
            final AtomicAction D = new AtomicAction();
            D.begin();
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    D.cancel();
                }
            };
            Thread t2 = new Thread() {
                @Override
                public void run() {
                    D.abort();
                }
            };
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }

        assertEquals(200, TxStats.getInstance().getNumberOfAbortedTransactions());
        assertEquals(200, TxStats.getInstance().getNumberOfApplicationRollbacks());
        assertEquals(401, TxStats.getInstance().getNumberOfTransactions());
    }
}
