/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteTransaction;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class AssumedCompleteUnitTest extends TestBase
{
    @Test
    public void testTransaction () throws Exception
    {
        AssumedCompleteTransaction tx = new AssumedCompleteTransaction(new Uid());
        
        assertEquals(tx.getOriginalStatus(), Status.StatusNoTransaction);
        
        assertTrue(tx.type() != null);
        assertEquals(AssumedCompleteTransaction.typeName(), tx.type());
        assertTrue(tx.toString() != null);
        
        assertFalse(tx.assumeComplete());
        assertEquals(tx.getLastActiveTime(), null);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(tx.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(tx.restore_state(is, ObjectType.ANDPERSISTENT));
    }
    
    @Test
    public void testServerTransaction () throws Exception
    {
        AssumedCompleteServerTransaction tx = new AssumedCompleteServerTransaction(new Uid());
        
        assertEquals(tx.getOriginalStatus(), Status.StatusNoTransaction);
        
        assertTrue(tx.type() != null);
        assertEquals(AssumedCompleteServerTransaction.typeName(), tx.type());
        assertTrue(tx.toString() != null);
        
        assertFalse(tx.assumeComplete());
        assertEquals(tx.getLastActiveTime(), null);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(tx.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(tx.restore_state(is, ObjectType.ANDPERSISTENT));
    }
}