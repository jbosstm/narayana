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
package org.jboss.narayana.jta.quickstarts.recovery;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.narayana.jta.quickstarts.util.DummyXAResource;

public class BasicXAExample extends RecoverySetup {
    public static void main(String[] args) throws Exception {
        startRecovery();
        new BasicXAExample().resourceEnlistment();
        stopRecovery();
    }

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

        if (!xares1.startCalled)
            throw new RuntimeException("start should have called");

        // commit any transactional work that was done on the two dummy XA resources
        tm.commit();

        if (!xares1.endCalled)
            throw new RuntimeException("end should have called");
        if (!xares1.prepareCalled)
            throw new RuntimeException("prepare should have called");
        if (!xares1.commitCalled)
                throw new RuntimeException("commit should have called");
    }
}
