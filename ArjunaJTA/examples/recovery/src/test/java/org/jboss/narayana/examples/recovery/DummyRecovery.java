package org.jboss.narayana.examples.recovery;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import junit.framework.Assert;
import org.jboss.narayana.examples.util.DummyXAResource;
import org.jboss.narayana.examples.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.*;

public class DummyRecovery extends RecoverySetup {

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            if (args[0].equals("-f")) {
                BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(Util.recoveryStoreDir);
                new DummyRecovery().enlistmentFailure();
            } else if (args[0].equals("-r")) {
                startRecovery();
                new DummyRecovery().waitForRecovery();
                stopRecovery();
            }
        } else {
            System.err.println("to generate something to recover: java DummyRecovery -f");
            System.err.println("to recover after failure: java DummyRecovery -r");
        }
    }

    @BeforeClass
    public static void beforeClass() {
        startRecovery();
    }

    @AfterClass
    public static void afterClass() {
        stopRecovery();
    }

    @Test
    public void enlistmentFailure() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
        if (Util.countLogRecords() != 0)
            return;

        // obtain a reference to the transaction manager
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // start a transaction
        tm.begin();

        // enlist some resources
        tm.getTransaction().enlistResource(new DummyXAResource(DummyXAResource.faultType.NONE));
        tm.getTransaction().enlistResource(new DummyXAResource(DummyXAResource.faultType.HALT));

        // commit any transactional work that was done on the two dummy XA resources
        System.out.println("Halting VM - next test run will not halt and should pass since there will be transactions to recover");

        tm.commit();
    }

    @Test
    public void waitForRecovery() throws InterruptedException {
        int commitRequests = DummyXAResource.getCommitRequests();
        recoveryManager.scan();

        if (commitRequests >= DummyXAResource.getCommitRequests())
            System.err.println("Did you forget to generate a recovery record before testing recovery (use the -f argument)");

        Assert.assertTrue(commitRequests < DummyXAResource.getCommitRequests());

        Util.emptyObjectStore();
    }
}
