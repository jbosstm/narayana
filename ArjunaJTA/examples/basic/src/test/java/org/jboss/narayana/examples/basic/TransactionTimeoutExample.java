package org.jboss.narayana.examples.basic;

import org.junit.Assert;
import org.junit.Test;

import javax.transaction.*;

public class TransactionTimeoutExample {
	public static void main(String[] args) throws Exception {
        TransactionTimeoutExample txeg = new TransactionTimeoutExample();

        txeg.transactionTimeout();
	}

	@Test
	public void transactionTimeout() throws SystemException, NotSupportedException, InterruptedException, HeuristicRollbackException, HeuristicMixedException {
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.setTransactionTimeout(1);
		utx.begin();
        Thread.sleep(1500);
         try {
            utx.commit();
            throw new RuntimeException("Should have got an exception whilst committing a transaction that exceeded its timeout");
        } catch (RollbackException e) {
        }
	}
}
