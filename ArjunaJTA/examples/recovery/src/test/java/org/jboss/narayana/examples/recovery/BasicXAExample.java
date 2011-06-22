package org.jboss.narayana.examples.recovery;

import junit.framework.Assert;
import org.jboss.narayana.examples.util.DummyXAResource;
import org.jboss.narayana.examples.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.*;

public class BasicXAExample extends RecoverySetup {
    public static void main(String[] args) throws Exception {
        startRecovery();
        new BasicXAExample().resourceEnlistment();
        stopRecovery();
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
    public void resourceEnlistment() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
        // obtain a reference to the transaction manager
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        DummyXAResource xares1 = new DummyXAResource(DummyXAResource.faultType.NONE);
        DummyXAResource xares2 = new DummyXAResource(DummyXAResource.faultType.NONE);

        // start a transaction
        tm.begin();

        // enlist some resources
        tm.getTransaction().enlistResource(xares1);
        tm.getTransaction().enlistResource(xares2);

        Assert.assertTrue(xares1.startCalled);

        // commit any transactional work that was done on the two dummy XA resources
        tm.commit();

        Assert.assertTrue(xares1.endCalled);
        Assert.assertTrue(xares1.prepareCalled);
        Assert.assertTrue(xares1.commitCalled);
    }
}
