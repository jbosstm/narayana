/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.jta.examples;

import javax.transaction.*;

public class TransactionExample {
	public static void main(String[] args) throws Exception {
        TransactionExample txeg = new TransactionExample();

		txeg.commitUserTransaction();
		txeg.commitTransactionManager();
        txeg.rollbackUserTransaction();
        txeg.setRollbackOnly();
        txeg.transactionStatus();
	}

    public void commitUserTransaction() throws SystemException, NotSupportedException, RollbackException, HeuristicRollbackException, HeuristicMixedException {
		//get UserTransaction
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		// start transaction
		utx.begin();
        // ... do some transactional work ...
		// commit it
		utx.commit();
	}

	public void commitTransactionManager() throws NotSupportedException, RollbackException, SystemException, HeuristicMixedException, HeuristicRollbackException {
		//get TransactionManager
		TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        // start a transaction by calling begin on the transaction manager
		tm.begin();

		tm.commit();
	}

	public void rollbackUserTransaction() throws SystemException, NotSupportedException {
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		utx.begin();

		// abort the transaction
		utx.rollback();
	}

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

	public void transactionStatus() throws SystemException, NotSupportedException {
		UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

		utx.begin();

		// abort the transaction
		assert (utx.getStatus() == Status.STATUS_ACTIVE);
        utx.setRollbackOnly();
        assert (utx.getStatus() == Status.STATUS_MARKED_ROLLBACK);
        utx.rollback();
        assert (utx.getStatus() == Status.STATUS_NO_TRANSACTION);
	}
}
