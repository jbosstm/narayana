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
import org.jboss.narayana.jta.quickstarts.util.Util;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

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

    public void waitForRecovery() throws InterruptedException {
        int commitRequests = DummyXAResource.getCommitRequests();
        recoveryManager.scan();

        if (commitRequests >= DummyXAResource.getCommitRequests())
            throw new RuntimeException("Did you forget to generate a recovery record before testing recovery (use the -f argument)");

        Util.emptyObjectStore();
    }
}
