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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.twophase;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyRecoverableXAConnection;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.TestResource;

import static org.junit.Assert.*;

public class XAResourceRecordUnitTest
{
    @Test
    public void test () throws Exception
    {
        XAResourceRecord xares = new XAResourceRecord();
        Object obj = new Object();
        
        xares.setValue(obj);
        
        assertTrue(xares.value() != obj);
        
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(0), new DummyXA(false), new XidImple(new Uid()), params);
        
        assertTrue(xares.type() != null);
        
        xares.merge(xares);
        xares.replace(xares);
        
        assertTrue(xares.toString() != null);
    }
    
    @Test
    public void testPackUnpack () throws Exception
    {
        XAResourceRecord xares;      
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(0), new DummyXA(false), new XidImple(new Uid()), params);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(xares.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(xares.restore_state(is, ObjectType.ANDPERSISTENT));
    }
    
    @Test
    public void testReadonly () throws Exception
    {
        XAResourceRecord xares;
        
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(0), new TestResource(true), new XidImple(new Uid()), params);
        
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.NOT_PREPARED);
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_READONLY);
    }
    
    @Test
    public void testCommitFailure () throws Exception
    {
        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.commit);
        TransactionImple tx = new TransactionImple(0);
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);
        
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.HEURISTIC_MIXED);
        assertTrue(xares.forgetHeuristic());
    }
    
    @Test
    public void testRollbackFailure () throws Exception
    {
        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.rollback);
        TransactionImple tx = new TransactionImple(0);
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);
        
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelAbort(), TwoPhaseOutcome.HEURISTIC_HAZARD);
        assertTrue(xares.forgetHeuristic());
    }
    
    @Test
    public void testValid2PC () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);
        
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
    }
    
    @Test
    public void testValid1PC () throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);
        
        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.FINISH_OK);
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        XAResourceRecord xares = new XAResourceRecord();
        
        assertEquals(xares.getXid(), null);
        assertEquals(xares.value(), null);
        assertEquals(xares.topLevelOnePhaseCommit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        assertEquals(xares.topLevelPrepare(), TwoPhaseOutcome.PREPARE_NOTOK);
        assertEquals(xares.topLevelAbort(), TwoPhaseOutcome.FINISH_ERROR);
        assertEquals(xares.topLevelCommit(), TwoPhaseOutcome.FINISH_ERROR);
    }
    
    @Test
    public void testNested () throws Exception
    {
        XAResourceRecord xares = new XAResourceRecord();
        
        assertEquals(xares.nestedOnePhaseCommit(), TwoPhaseOutcome.FINISH_ERROR);
        assertEquals(xares.nestedPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(xares.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        assertEquals(xares.nestedAbort(), TwoPhaseOutcome.FINISH_OK);
    }
}
