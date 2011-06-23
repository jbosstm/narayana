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

public class TransactionTimeoutExample {
	public static void main(String[] args) throws Exception {
        new TransactionTimeoutExample().transactionTimeout();
	}

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
