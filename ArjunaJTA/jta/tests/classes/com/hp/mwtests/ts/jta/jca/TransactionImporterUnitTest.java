/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jca;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.Implementations;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.xa.XidImple;
import org.junit.Test;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertTrue;

public class TransactionImporterUnitTest {

    @Test
    public void testDifferentInstanceFromRecovery() throws XAException, RollbackException, SystemException, HeuristicRollbackException, HeuristicMixedException, HeuristicCommitException {
        Uid uid = new Uid();
        XidImple xid = new XidImple(uid);

        SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);
        Uid subordinateTransactionUid = subordinateTransaction.get_uid();
        Xid subordinateTransactionXid = subordinateTransaction.baseXid();

        SubordinateTransaction importedTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue (subordinateTransaction == importedTransaction);
        subordinateTransaction.enlistResource(new XAResource() {

            @Override
            public void commit(Xid xid, boolean b) throws XAException {

            }

            @Override
            public void end(Xid xid, int i) throws XAException {

            }

            @Override
            public void forget(Xid xid) throws XAException {

            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        });
        subordinateTransaction.doPrepare();

        Implementations.initialise();
        SubordinateTransaction subordinateTransaction1 = SubordinationManager.getTransactionImporter().recoverTransaction(subordinateTransactionUid);
        assertTrue(subordinateTransaction != subordinateTransaction1);
        SubordinateTransaction importedTransaction1 = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue(importedTransaction != importedTransaction1);
        SubordinateTransaction importedTransaction2 = SubordinationManager.getTransactionImporter().getImportedTransaction(subordinateTransactionXid);
        assertTrue(importedTransaction1 == importedTransaction2);
        importedTransaction2.doCommit();
    }
}