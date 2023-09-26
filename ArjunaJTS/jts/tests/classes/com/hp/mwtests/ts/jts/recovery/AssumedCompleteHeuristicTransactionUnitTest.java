/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jts.recovery;

import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;

/**
 * @author <a href="gytis@redhat.com">Gytis Trikleris</a>
 */
public final class AssumedCompleteHeuristicTransactionUnitTest {
    
    private Uid uid;
    
    private AssumedCompleteHeuristicTransaction transaction;
    
    @Before
    public void before() {
        uid = new Uid();
        transaction = new AssumedCompleteHeuristicTransaction(uid);
    }
    
    @Test
    public void testGetOriginalStatus() {
        Assert.assertEquals(Status.StatusNoTransaction, transaction.getOriginalStatus());
    }
    
    @Test
    public void testType() {
        Assert.assertEquals(AssumedCompleteHeuristicTransaction.typeName(), transaction.type());
    }
    
    @Test
    public void testToString() {
        Assert.assertEquals("AssumedCompleteHeuristicTransaction <" + uid + ">", transaction.toString());
    }
    
    @Test
    public void testAssumeComplete() {
        Assert.assertEquals(false, transaction.assumeComplete());
    }
    
    @Test
    public void testSaveAndRestoreState() throws IOException {
        final OutputObjectState outputObjectState = new OutputObjectState();
        Assert.assertTrue(transaction.save_state(outputObjectState, ObjectType.ANDPERSISTENT));
        
        final Date lastActiveTime = transaction.getLastActiveTime();
        
        final InputObjectState inputObjectState = new InputObjectState(outputObjectState);
        Assert.assertTrue(transaction.restore_state(inputObjectState, ObjectType.ANDPERSISTENT));
        Assert.assertEquals(lastActiveTime.getTime(), inputObjectState.unpackLong());
    }
    
}