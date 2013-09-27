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

package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.arjuna.ats.jts.extensions.TopLevelTransaction;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoSubTranResource;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;


public class TopLevelTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TopLevelTransaction A = new TopLevelTransaction();
        
        A.begin();
        
        assertTrue(A.get_transaction_name() != null);
        
        A.set_timeout(10);
        
        assertEquals(A.get_timeout(), 10);
        
        assertTrue(A.getTimeout() != -1);
        assertTrue(A.get_txcontext() != null);
        assertTrue(A.get_uid().notEquals(Uid.nullUid()));
        assertTrue(A.control() != null);
        
        assertFalse(A.equals(null));
        assertTrue(A.equals(A));
        assertFalse(A.equals(new TopLevelTransaction()));
        
        A.rollback();
    }
    
    @Test
    public void testCommit () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        assertTrue(A.get_txcontext() != null);
        
        A.registerResource(new DemoResource().getResource());
        
        TopLevelTransaction B = new TopLevelTransaction();
        DemoResource d = new DemoResource();
        
        B.begin();
        
        B.registerResource(d.getResource());
        
        B.commit(true);
        
        A.commit(true);
        
        assertEquals(d.getResourceTrace().getTrace(), ResourceTrace.ResourceTraceCommitOnePhase);
    }
    
    @Test
    public void testRollback () throws Exception
    {
        AtomicTransaction A = new AtomicTransaction();
        
        A.begin();
        
        assertTrue(A.get_txcontext() != null);
        
        A.registerResource(new DemoResource().getResource());
        
        TopLevelTransaction B = new TopLevelTransaction();
        DemoResource d = new DemoResource();
        
        B.begin();
        
        B.registerResource(d.getResource());
        
        B.commit(true);
        
        A.rollback();
        
        assertEquals(d.getResourceTrace().getTrace(), ResourceTrace.ResourceTraceCommitOnePhase);
    }
    
    @Test
    public void testSuspendResume () throws Exception
    {
        TopLevelTransaction A = new TopLevelTransaction();
        
        A.begin();
        
        assertTrue(A.control() != null);
        
        A.suspend();
        
        assertEquals(OTSImpleManager.current().get_control(), null);
        
        A.resume();
        
        assertTrue(OTSImpleManager.current() != null);
        
        A.rollback();
    }
}
