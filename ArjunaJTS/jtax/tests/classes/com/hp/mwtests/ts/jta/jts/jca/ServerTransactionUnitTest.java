/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

public class ServerTransactionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransaction st = new ServerTransaction(new Uid(), new XidImple(new Uid()));
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(st.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(st.restore_state(is, ObjectType.ANDPERSISTENT));
    }
}