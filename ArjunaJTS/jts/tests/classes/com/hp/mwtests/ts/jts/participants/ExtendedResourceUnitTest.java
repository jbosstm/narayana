/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.participants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoArjunaResource;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ExtendedResourceUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        ArjunaTransactionImple tx = new ArjunaTransactionImple(null);
        ExtendedResourceRecord res1 = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), null, new Uid(), tx);
        ExtendedResourceRecord res2 = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), null, new Uid(), tx);
        
        assertTrue(res1.resourceHandle() != null);
        assertTrue(res1.value() != null);
        assertFalse(res1.propagateOnAbort());
        assertFalse(res1.propagateOnCommit());
        assertTrue(res1.order().notEquals(Uid.nullUid()));
        assertEquals(res1.typeIs(), 101);
        assertTrue(res1.order() != null);
        
        res1.setValue(null);
        res1.print(new PrintWriter(new ByteArrayOutputStream()));
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(res1.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(res1.restore_state(is, ObjectType.ANDPERSISTENT));
        
        assertFalse(res2.doSave());
        
        assertFalse(res1.shouldAdd(res2));
        assertFalse(res1.shouldAlter(res2));
        assertFalse(res1.shouldMerge(res2));
        assertFalse(res1.shouldReplace(res2));
        
        res1 = new ExtendedResourceRecord();
        
        assertTrue(res1.getRCUid().notEquals(res2.getRCUid()));
        
        res1.alter(res2);
        res1.merge(res2);
    }
    
    @Test
    public void testNestedCommit () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        OTSImpleManager.current().begin();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.nestedPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(res.nestedCommit(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
        OTSImpleManager.current().rollback();
    }
    
    @Test
    public void testNestedAbort () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        OTSImpleManager.current().begin();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.nestedPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(res.nestedAbort(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
        OTSImpleManager.current().rollback();
    }
    
    @Test
    public void testNestedOnePhase () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        OTSImpleManager.current().begin();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.nestedOnePhaseCommit(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
        OTSImpleManager.current().rollback();
    }
    
    @Test
    public void testTopLevelCommit () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(res.topLevelCommit(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
        
        assertTrue(res.forgetHeuristic());  // no heuristic but we can still call method
    }
    
    @Test
    public void testTopLevelAbort () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.topLevelPrepare(), TwoPhaseOutcome.PREPARE_OK);
        assertEquals(res.topLevelAbort(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
    }
    
    @Test
    public void testOnePhaseCommit () throws Exception
    {
        DemoArjunaResource ares = new DemoArjunaResource();
        
        OTSImpleManager.current().begin();
        
        ArjunaTransactionImple tx = OTSImpleManager.current().getControlWrapper().getImple().getImplHandle();
        
        ExtendedResourceRecord res = new ExtendedResourceRecord(false, new Uid(), ares.getReference(), tx.getControlHandle().get_coordinator(), new Uid(), OTSImpleManager.current().getControlWrapper().getImple().getImplHandle());
        
        assertEquals(res.topLevelOnePhaseCommit(), TwoPhaseOutcome.FINISH_OK);
        
        OTSImpleManager.current().rollback();
    }
}