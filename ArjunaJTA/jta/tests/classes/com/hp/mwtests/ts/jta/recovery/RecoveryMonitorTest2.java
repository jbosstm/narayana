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
import org.junit.Test;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the RecoveryMonitor is verbose mode
 */
public class RecoveryMonitorTest2 {

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

        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();

        TransactionImple tx = new TransactionImple(0); // begin a transaction

        // enlist a resource that behaves correctly
        assertTrue(tx.enlistResource(dummy));
        assertTrue(tx.enlistResource(dummy));

        tx.commit();

        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

        manager.addModule(new XARecoveryModule()); // we only need to test the XARecoveryModule

        try {
            manager.startRecoveryManagerThread(); // start periodic recovery

            String host = recoveryEnvironmentBean.getRecoveryAddress(); // the recovery listener host
            String rcPort = String.valueOf(recoveryEnvironmentBean.getRecoveryPort()); // the recovery listener port

            // trigger a recovery scan with verbose output
            int status = RecoveryMonitor.main2(new String[] {"-verbose", "-port", rcPort, "-host", host});

            // check the output of the scan
            assertEquals("DONE", RecoveryMonitor.getResponse());
            assertEquals("DONE", RecoveryMonitor.getSystemOutput());
            assertEquals(0, status);
        } finally {
            manager.terminate();
        }
    }
}
