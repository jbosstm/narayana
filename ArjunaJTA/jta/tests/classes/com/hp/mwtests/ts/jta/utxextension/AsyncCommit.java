/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.utxextension;

import java.util.concurrent.Future;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.hp.mwtests.ts.jta.common.TestResource;

public class AsyncCommit
{
    @Test
    public void test() throws Exception
    {
    	UserTransactionImple ut = new UserTransactionImple();
    	ut.begin();
    	
    	TransactionImple current = TransactionImple.getTransaction();

        TestResource res1, res2;
        current.enlistResource( res1 = new TestResource() );
        current.enlistResource( res2 = new TestResource() );

        current.delistResource( res2, XAResource.TMSUCCESS );
        current.delistResource( res1, XAResource.TMSUCCESS );

        Future<Void> commitAsync = ut.commitAsync();
        
        commitAsync.get();
    }
}