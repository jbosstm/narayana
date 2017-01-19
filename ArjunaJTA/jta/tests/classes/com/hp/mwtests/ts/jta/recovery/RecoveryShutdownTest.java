/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2016,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.xa.XidImple;
import org.junit.Test;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertTrue;

public class RecoveryShutdownTest {
    /**
     * test that the call sequence
     *
     * XARecoveryModule#getNewXAResource()
     * RecoveryManager#terminate();
     * XARecoveryModule#removeXAResourceRecoveryHelper
     *
     * does not hang
     *
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void test() throws InterruptedException {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        XARecoveryModule xarm = new XARecoveryModule();

        final SimpleResource testXAResource = new SimpleResource() {
            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[] {new Xid() {
                    @Override
                    public int getFormatId() { return 0; }

                    @Override
                    public byte[] getGlobalTransactionId() { return new byte[0]; }

                    @Override
                    public byte[] getBranchQualifier() { return new byte[0]; }
                }};
            }
        };

        final XAResourceRecoveryHelper xaResourceRecoveryHelper = new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] {testXAResource};
            }
        };

        xarm.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        manager.removeAllModules(false);

        manager.addModule(xarm);

        manager.scan();

        manager.terminate();
        xarm.getNewXAResource( new XAResourceRecord(null, null, new XidImple(), null) );

        final boolean[] removedHelper = {false};
        Runnable r = () -> {
            // the next call will hang unless JBTM-2837 is fixed
            xarm.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
            removedHelper[0] = true;
        };

        Thread t = new Thread(r);

        t.start();

        Thread.sleep(100);

        assertTrue("removal of an XAResourceRecoveryHelper hung", removedHelper[0]);
    }
}
