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

package com.hp.mwtests.ts.jts.orbspecific.interposition;

import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;

import static org.junit.Assert.*;

public class ServerTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);
        
        assertTrue(sc.type() != null);
        assertTrue(ServerTransaction.typeName() != null);
        assertTrue(sc.getSavingUid().notEquals(Uid.nullUid()));
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(sc.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(sc.restore_state(is, ObjectType.ANDPERSISTENT));
        
        sc.setRecoveryCoordinator(null);
    }
    
    @Test
    public void testPrepareCommit () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.register_synchronization(new demosync(false).getReference());
        
        sc.doBeforeCompletion();
        
        assertEquals(sc.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
        assertEquals(sc.doPhase2Commit(), TwoPhaseOutcome.FINISH_OK);
        
        sc.doAfterCompletion(Status.StatusCommitted);
    }
    
    @Test
    public void testPrepareRollback () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        assertEquals(sc.doPrepare(), TwoPhaseOutcome.PREPARE_READONLY);  // readonly so we commit here
        assertEquals(sc.doPhase2Abort(), TwoPhaseOutcome.HEURISTIC_COMMIT);
    }
    
    @Test
    public void testOnePhaseCommit () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.doCommit(true);
    }
    
    @Test
    public void testRollback () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);

        sc.rollback();
    }
}
