package org.jboss.narayana.examples.basic;

import javax.transaction.*;

import org.junit.Assert;
import org.junit.Test;

public class TransactionExample {
	public static void main(String[] args) throws Exception {
        TransactionExample txeg = new TransactionExample();

		txeg.commitUserTransaction();
		txeg.commitTransactionManager();
        txeg.rollbackUserTransaction();
        txeg.setRollbackOnly();
        txeg.transactionStatus();
	}

	@Test
	public void commitUserTransaction() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
		//get UserTransaction
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		// start transaction
		utx.begin();
        // ... do some transactional work ...
		// commit it
		utx.commit();
	}

	@Test
	public void commitTransactionManager() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
		//get TransactionManager
		TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // start a transaction by calling begin on the transaction manager
		tm.begin();

		tm.commit();
	}

	@Test
	public void rollbackUserTransaction() throws SystemException, NotSupportedException {
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		utx.begin();

		// abort the transaction
		utx.rollback();
	}

	@Test
	public void setRollbackOnly() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException {
		//get TransactionManager
		TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

		// start transaction work..
		tm.begin();

		// perform transactional work
		tm.setRollbackOnly();
        try {
            tm.commit();
            throw new RuntimeException("Should have got an exception whilst committing a transaction is marked as rollback only");
        } catch (RollbackException e) {
        }
    }

	@Test
	public void transactionStatus() throws SystemException, NotSupportedException {
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		utx.begin();

		// abort the transaction
		Assert.assertEquals(utx.getStatus(), Status.STATUS_ACTIVE);
        utx.setRollbackOnly();
        Assert.assertEquals(utx.getStatus(), Status.STATUS_MARKED_ROLLBACK);
        utx.rollback();
        Assert.assertEquals(utx.getStatus(), Status.STATUS_NO_TRANSACTION);
	}
}
