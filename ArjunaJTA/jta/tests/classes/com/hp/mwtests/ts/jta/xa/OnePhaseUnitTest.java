/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyRecoverableXAConnection;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.SampleOnePhaseResource;
import com.hp.mwtests.ts.jta.common.SampleOnePhaseResource.ErrorType;

public class OnePhaseUnitTest
{
    @Test
    public void test () throws Exception
    {
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] obj = new Object[1];
        SampleOnePhaseResource res = new SampleOnePhaseResource();
        
        obj[XAResourceRecord.XACONNECTION] = rc;
        
        XAOnePhaseResource xares = new XAOnePhaseResource(res, new XidImple(new Uid()), obj);
        
        OutputObjectState os = new OutputObjectState();
        
        xares.pack(os);
        
        InputObjectState is = new InputObjectState(os);
        
        xares.unpack(is);
    }
    
    @Test
    public void testFailure () throws Exception
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource(ErrorType.heurmix);
        XAOnePhaseResource xares = new XAOnePhaseResource(res, new XidImple(new Uid()), null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.HEURISTIC_HAZARD);
        
        res = new SampleOnePhaseResource(ErrorType.rmerr);
        xares = new XAOnePhaseResource(res, new XidImple(new Uid()), null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        
        res = new SampleOnePhaseResource(ErrorType.nota);
        xares = new XAOnePhaseResource(res, new XidImple(new Uid()), null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.HEURISTIC_HAZARD);
        
        res = new SampleOnePhaseResource(ErrorType.inval);
        xares = new XAOnePhaseResource(res, new XidImple(new Uid()), null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.HEURISTIC_HAZARD);
        
        res = new SampleOnePhaseResource(ErrorType.proto);
        xares = new XAOnePhaseResource(res, new XidImple(new Uid()), null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
    }
    
    @Test
    public void testInvalid ()
    {
        XAOnePhaseResource xares = new XAOnePhaseResource();
        
        assertEquals(xares.commit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        assertEquals(xares.rollback(), TwoPhaseOutcome.FINISH_ERROR);
    }
    
    @Test
    public void testCommit ()
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource();
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.FINISH_OK);
        assertTrue(res.onePhaseCalled());
    }
    
    @Test
    public void testCommitHeuristic ()
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource(ErrorType.heurcom);
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.FINISH_OK);
        assertTrue(res.forgetCalled());
    }
    
    @Test
    public void testRollbackHeuristic ()
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource(ErrorType.heurrb);
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);
        
        assertEquals(xares.commit(), TwoPhaseOutcome.ONE_PHASE_ERROR);
        assertTrue(res.forgetCalled());
    }
    
    @Test
    public void testPackUnpackError () throws Exception
    {
        SampleOnePhaseResource res = new SampleOnePhaseResource();
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);
        OutputObjectState os = new OutputObjectState();
        
        try
        {
            xares.pack(os);
            
            fail();
        }
        catch (final IOException ex)
        {           
        }
    }
    
    @Test
    public void testPackUnpack () throws Exception
    {
        DummyXA res = new DummyXA(false);
        XidImple xid = new XidImple(new Uid());
        XAOnePhaseResource xares = new XAOnePhaseResource(res, xid, null);
        OutputObjectState os = new OutputObjectState();
        
        xares.pack(os);
        
        InputObjectState is = new InputObjectState(os);
        
        xares.unpack(is);
    }
}