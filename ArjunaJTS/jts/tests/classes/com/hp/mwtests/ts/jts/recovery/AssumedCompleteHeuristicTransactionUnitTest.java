/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
