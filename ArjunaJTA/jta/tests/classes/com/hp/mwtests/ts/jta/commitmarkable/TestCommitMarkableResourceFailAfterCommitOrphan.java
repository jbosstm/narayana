/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.commitmarkable;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.byteman.rule.exception.ExecuteException;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(BMUnitRunner.class)
public class TestCommitMarkableResourceFailAfterCommitOrphan extends TestCommitMarkableResourceBase {
    private JDBCConnectableResource cmrResource;
    private XARecoveryModule xarm;
    private boolean failed = false;
    private boolean wasCommitted;
    private boolean wasRolledback;
    private Xid preparedXid;
    private final XAResource xaResource = new XAResource() {

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
            wasCommitted = true;
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
            preparedXid = xid;
            return 0;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            return new Xid[]{preparedXid};
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            wasRolledback = true;
        }

        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return false;
        }

        @Override
        public void start(Xid xid, int i) throws XAException {

        }
    };


    @Test
    @BMScript("commitMarkableResourceFailAfterCommit")
    public void test() throws Exception {
        final JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE;DB_CLOSE_DELAY=-1");

        // Test code
        Utils.createTables(dataSource.getConnection());

        // We can't just instantiate one as we need to be using the
        // same one as
        // the transaction
        // manager would have used to mark the transaction for GC
        CommitMarkableResourceRecordRecoveryModule commitMarkableResourceRecoveryModule = null;
        Vector recoveryModules = manager.getModules();
        if (recoveryModules != null) {
            Enumeration modules = recoveryModules.elements();

            while (modules.hasMoreElements()) {
                RecoveryModule m = (RecoveryModule) modules.nextElement();

                if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
                    commitMarkableResourceRecoveryModule = (CommitMarkableResourceRecordRecoveryModule) m;
                } else if (m instanceof XARecoveryModule) {
                    xarm = (XARecoveryModule) m;
                    xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception {
                            return true;
                        }

                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[]{xaResource};
                        }
                    });
                }
            }
        }
        // final Object o = new Object();
        // synchronized (o) {

        Thread preCrash = new Thread(new Runnable() {

            public void run() {

                try {
                    javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                            .transactionManager();

                    tm.begin();

                    Connection localJDBCConnection = dataSource.getConnection();
                    localJDBCConnection.setAutoCommit(false);
                    cmrResource = new JDBCConnectableResource(
                            localJDBCConnection);
                    tm.getTransaction().enlistResource(cmrResource);
                    tm.getTransaction().enlistResource(xaResource);

                    localJDBCConnection.createStatement().execute(
                            "INSERT INTO foo (bar) VALUES (1)");

                    tm.commit();
                } catch (ExecuteException t) {
                } catch (Exception e) {
                    e.printStackTrace();
                    failed = true;
                } catch (Error e) {

                }
            }
        });
        preCrash.start();
        preCrash.join();

        assertFalse(failed);
        assertFalse(wasCommitted);
        assertFalse(wasRolledback);

        // The recovery module has to perform lookups
        new InitialContext().rebind("commitmarkableresource", dataSource);

        // Testing that we can find a AAC but that it won't be ignored in orphan detection so need CMRRM run
        commitMarkableResourceRecoveryModule.periodicWorkFirstPass();
        XAResourceOrphanFilter.Vote vote = new JTATransactionLogXAResourceOrphanFilter().checkXid(preparedXid);
        assertTrue(vote == XAResourceOrphanFilter.Vote.LEAVE_ALONE);

        manager.scan();
        manager.scan();
        assertTrue(wasCommitted);
    }
}
