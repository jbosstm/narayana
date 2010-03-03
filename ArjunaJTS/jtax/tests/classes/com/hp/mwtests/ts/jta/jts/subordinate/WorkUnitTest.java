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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.subordinate;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.jca.TxWorkManager;
import com.arjuna.ats.internal.jta.transaction.jts.jca.WorkSynchronization;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import static org.junit.Assert.*;

public class WorkUnitTest
{
    class DummyWork implements Work
    {       
        public DummyWork ()
        {
        }
        
        public void release ()
        {
        }
        
        public void run ()
        {
        }
    }
    
    @Test
    public void testWorkManager () throws Exception
    {
        DummyWork work = new DummyWork();
        Transaction tx = new TransactionImple();
        
        TxWorkManager.addWork(work, tx);
        
        try
        {
            TxWorkManager.addWork(new DummyWork(), tx);
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        assertTrue(TxWorkManager.hasWork(tx));
        
        assertEquals(work, TxWorkManager.getWork(tx));       
        
        TxWorkManager.removeWork(work, tx);
        
        assertEquals(TxWorkManager.getWork(tx), null);
    }
    
    @Test
    public void testWorkSynchronization () throws Exception
    {
        Transaction tx = new TransactionImple();
        Synchronization ws = new WorkSynchronization(tx);
        DummyWork work = new DummyWork();
        
        TxWorkManager.addWork(work, tx);
        
        try
        {
            ws.beforeCompletion();
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        ws.afterCompletion(Status.STATUS_COMMITTED);
    }
    
    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
}
