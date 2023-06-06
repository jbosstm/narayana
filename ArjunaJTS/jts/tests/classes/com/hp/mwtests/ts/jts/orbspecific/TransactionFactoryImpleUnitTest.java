/*
 * SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jts.orbspecific;

import java.io.File;

import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteTransaction;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;
import com.hp.mwtests.ts.jts.resources.TestBase;

/**
 * @author <a href="gytis@redhat.com">Gytis Trikleris</a>
 */
public final class TransactionFactoryImpleUnitTest extends TestBase {

    private TransactionFactoryImple transactionFactory;

    @BeforeClass
    public static void beforeClass() {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(ShadowNoFileLockStore.class.getName());
    }

    @Before
    public void before() {
        clearObjectStore();
        transactionFactory = new TransactionFactoryImple();
    }

    @After
    public void after() {
        clearObjectStore();
    }

    private void checkStatus(ArjunaTransactionImple transaction, String typeName, Status expect) throws Exception {
        final Uid uid = transaction.get_uid();
        final OutputObjectState outputObjectState = new OutputObjectState();

        transaction.save_state(outputObjectState, ObjectType.ANDPERSISTENT);
        StoreManager.getRecoveryStore().write_committed(uid, typeName, outputObjectState);

        Assert.assertEquals(expect, transactionFactory.getOSStatus(uid));
    }

    @Test
    public void testGetOSStatusNoTransaction() throws NoTransaction, SystemException {
        Assert.assertEquals(Status.StatusNoTransaction, transactionFactory.getOSStatus(new Uid()));
    }

    @Test
    public void testGetOSStatusWithArjunaTransactionImple() throws Exception {
        checkStatus(new ArjunaTransactionImple(new Uid(), null), ArjunaTransactionImple.typeName(),
                Status.StatusCommitted);
    }
    
    @Test
    public void testGetOSStatusWithAssumedCompleteHeuristicTransaction() throws Exception {
        checkStatus(new AssumedCompleteHeuristicTransaction(new Uid()), AssumedCompleteHeuristicTransaction.typeName(),
                Status.StatusCommitted);
    }

    @Test
    public void testGetOSStatusWithAssumedCompleteHeuristicServerTransaction() throws Exception {
        checkStatus(new AssumedCompleteHeuristicServerTransaction(new Uid()), AssumedCompleteHeuristicServerTransaction.typeName(),
                Status.StatusCommitted);
    }


    @Test
    public void testGetOSStatusWithAssumedCompleteTransaction() throws Exception {
        checkStatus(new AssumedCompleteTransaction(new Uid()), ArjunaTransactionImple.typeName(),
                Status.StatusCommitted);
    }


    @Test
    public void testGetOSStatusWithAssumedCompleteServerTransaction() throws Exception {
        checkStatus(new AssumedCompleteServerTransaction(new Uid()), AssumedCompleteServerTransaction.typeName(),
                Status.StatusNoTransaction);
    }

    private void clearObjectStore() {
        final String objectStorePath = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        final File objectStoreDirectory = new File(objectStorePath);
        
        clearDirectory(objectStoreDirectory);
    }
    
    private void clearDirectory(final File directory) {
        final File[] files = directory.listFiles();
        
        if (files != null) {
            for (final File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                
                file.delete();
            }
        }
    }
    
}