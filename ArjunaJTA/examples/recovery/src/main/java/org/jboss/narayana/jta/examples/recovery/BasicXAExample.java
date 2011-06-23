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
package org.jboss.narayana.jta.examples.recovery;

import org.jboss.narayana.jta.examples.util.DummyXAResource;
import org.jboss.narayana.jta.examples.util.Util;

import javax.transaction.*;

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

        assert (xares1.startCalled);

        // commit any transactional work that was done on the two dummy XA resources
        tm.commit();

        assert (xares1.endCalled);
        assert (xares1.prepareCalled);
        assert (xares1.commitCalled);
    }
}
