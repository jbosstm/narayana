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

public class RecoveryModuleUnitTest
{
    @Test
    public void test () throws Exception
    {
        DummyTOModule trm = new DummyTOModule();
        AtomicAction A = new AtomicAction();
        
        trm.intialise();
        
        A.begin();
        
        AtomicObject obj = new AtomicObject();
        OutputObjectState os = new OutputObjectState();
        Uid u = new Uid();
        
        assertTrue(obj.save_state(os, ObjectType.ANDPERSISTENT));
        
        assertTrue(StoreManager.getParticipantStore().write_uncommitted(u, obj.type(), os));
        
        A.abort();
        
        trm.periodicWorkFirstPass();
        trm.periodicWorkSecondPass();
    }
}