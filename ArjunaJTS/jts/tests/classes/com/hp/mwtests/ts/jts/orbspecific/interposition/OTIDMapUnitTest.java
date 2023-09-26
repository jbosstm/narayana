/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.otid_t;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.interposition.resources.osi.OTIDMap;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class OTIDMapUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        OTSImpleManager.current().begin();
        
        PropagationContext ctx = OTSImpleManager.current().get_control().get_coordinator().get_txcontext();
        otid_t tid = ctx.current.otid;
        
        assertTrue(OTIDMap.find(tid).notEquals(Uid.nullUid()));
        assertTrue(OTIDMap.remove(OTIDMap.find(tid)));
        
        OTSImpleManager.current().rollback();
    }
}