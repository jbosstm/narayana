/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class RecoveryTest
{
    @Test
    public void testCommit () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        AtomicObject obj = new AtomicObject();
        OutputObjectState os = new OutputObjectState();
        Uid u = new Uid();
        
        assertTrue(obj.save_state(os, ObjectType.ANDPERSISTENT));
        
        assertTrue(StoreManager.getParticipantStore().write_uncommitted(u, obj.type(), os));
        
        MyRecoveredTO rto = new MyRecoveredTO(u, obj.type(), StoreManager.getParticipantStore());
        
        rto.replay();
        
        A.abort();
    }
    
    @Test
    public void testAbort () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        AtomicObject obj = new AtomicObject();
        OutputObjectState os = new OutputObjectState();
        Uid u = new Uid();
        
        assertTrue(obj.save_state(os, ObjectType.ANDPERSISTENT));
        
        assertTrue(StoreManager.getParticipantStore().write_uncommitted(u, obj.type(), os));
        
        MyRecoveredTO rto = new MyRecoveredTO(u, obj.type(), StoreManager.getParticipantStore());
        
        A.abort();
        
        rto.replay();
    }
}