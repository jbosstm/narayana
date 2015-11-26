/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2015,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class TestJDBCStoreOffline {

    private List<Xid> xids = new ArrayList<Xid>();
    public int commitCount;
    private boolean rollbackCalled;
    public static boolean FAULT_JDBC = false;

    @Test
    public void test() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException, NamingException {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(JDBCStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setJdbcAccess(TestJDBCAccess.class.getName());

        javax.transaction.TransactionManager tm = TransactionManager.transactionManager();
        tm.begin();
        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.getTransaction().enlistResource(new DummyXAResource());
        tm.commit();
        assertTrue(commitCount == 1);

        FAULT_JDBC = true;
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList(new String[] { "1" }));
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceOrphanFilters(Arrays.asList(new XAResourceOrphanFilter[] { new com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter(), new com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter() }));
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveries(Arrays.asList(new XAResourceRecovery[] { new DummyXAResourceRecovery() }));
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(2);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModules(Arrays.asList(new RecoveryModule[] { new XARecoveryModule() }));
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.scan();

        assertFalse(rollbackCalled);
    }

    private class DummyXAResourceRecovery implements XAResourceRecovery {

        private boolean more = true;

        @Override
        public XAResource getXAResource() throws SQLException {
            more = false;
            return new DummyXAResource();
        }

        @Override
        public boolean initialise(String p) throws SQLException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasMoreResources() {
            return more;
        }

    }

    private class DummyXAResource implements XAResource {

        @Override
        public void start(Xid xid, int flags) throws XAException {
            // TODO Auto-generated method stub

        }

        @Override
        public void end(Xid xid, int flags) throws XAException {
            // TODO Auto-generated method stub

        }

        @Override
        public int prepare(Xid xid) throws XAException {
            xids.add(xid);
            return 0;
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            if (commitCount == 1) {
                throw new XAException(XAException.XA_RETRY);
            }
            commitCount++;
            xids.remove(xid);
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            rollbackCalled = true;
            xids.remove(xid);
        }

        @Override
        public void forget(Xid xid) throws XAException {
            // TODO Auto-generated method stub

        }

        @Override
        public Xid[] recover(int flag) throws XAException {
            return xids.toArray(new Xid[0]);
        }

        @Override
        public boolean isSameRM(XAResource xaRes) throws XAException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
