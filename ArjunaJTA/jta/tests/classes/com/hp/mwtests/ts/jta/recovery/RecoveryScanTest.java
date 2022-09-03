/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.tools.RecoveryMonitor;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.transaction.UserTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the RecoveryMonitor is verbose mode
 */
public class RecoveryScanTest {

    private static RecoveryManager manager;
    private static XARecoveryModule xaRecoveryModule;

    @BeforeClass
    public static void beforeClass() throws Exception {
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();

        // Ensure that test XAR is recoverable by adding the test XAResourceRecovery
        ArrayList<String> rcvClassNames = new ArrayList<>();

        rcvClassNames.add(XATestResourceXARecovery.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(rcvClassNames);

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        xaRecoveryModule = new XARecoveryModule();
        manager.addModule(xaRecoveryModule); // we only need to test the XARecoveryModule
        manager.startRecoveryManagerThread(); // start periodic recovery
    }

    @AfterClass
    public static void afterClass() throws Exception {
        manager.terminate();
        XATestResourceXARecovery.setUseFaultyResources(true);
    }

    @Test
    public void testRecoveryMonitorWithFailure() throws Exception {
        XATestResourceXARecovery.setUseFaultyResources(true);
        UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();
        ut.begin();
        TransactionImple tx = TransactionImple.getTransaction();

        // enlist a resource that behaves correctly
        assertTrue(tx.enlistResource(new XATestResource(XATestResource.OK_JNDI_NAME, false)));
        // enlist a resource that throws an exception from recover
        assertTrue(tx.enlistResource(new XATestResource(XATestResource.FAULTY_JNDI_NAME, true)));

        ut.commit();

        manager.scan();

        assertFalse(xaRecoveryModule.isPeriodicWorkSuccessful());
    }

    @Test
    public void testRecoveryMonitorWithSuccess() throws Exception {
        XAResource dummy = new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
            }

            @Override
            public void end(Xid xid, int i) throws XAException {
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {
            }
        };

        XATestResourceXARecovery.setUseFaultyResources(false);
        UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();
        ut.begin();
        TransactionImple tx = TransactionImple.getTransaction();

        // enlist a resource that behaves correctly
        assertTrue(tx.enlistResource(dummy));
        assertTrue(tx.enlistResource(dummy));

        ut.commit();

        manager.scan();

        assertTrue(xaRecoveryModule.isPeriodicWorkSuccessful());
    }

    @Test
    public void testBoth() throws Exception{
        testRecoveryMonitorWithFailure();
        testRecoveryMonitorWithSuccess();
    }
}
