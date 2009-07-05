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
package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.AtomicAction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.listener.ReaperMonitor;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReaperMonitorTest
{
    class DummyMonitor implements ReaperMonitor
    {
        public void rolledBack (Uid txId)
        {
            success = true;
        }
        
        public void markedRollbackOnly (Uid txId)
        {
            success = false;
        }
        
        public boolean success = false;
    }
    
    @Test
    public void test()
    {
        TransactionReaper.create(100);
        TransactionReaper reaper = TransactionReaper.transactionReaper();
        DummyMonitor listener = new DummyMonitor();
       
        reaper.addListener(listener);
        
        AtomicAction A = new AtomicAction();

        A.begin();

        reaper.insert(A, 1);
        
        try
        {
            Thread.sleep(1100);
        }
        catch (final Throwable ex)
        {  
        }

        reaper.check();
        
        try
        {
            Thread.sleep(500);
        }
        catch (final Throwable ex)
        {  
        }
        
        reaper.check();
        
        assertTrue(listener.success);
    }

    public static boolean success = false;
}
