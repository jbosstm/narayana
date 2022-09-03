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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import org.h2.jdbcx.JdbcDataSource;
import org.jboss.byteman.rule.exception.ExecuteException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FailAfterPrepareBase extends TestCommitMarkableResourceBase {

    private JDBCConnectableResource nonXAResource;
    private boolean failed = false;
    private SimpleXAResource xaResource;
    private CommitMarkableResourceRecordRecoveryModule recoveryModule;

    protected Uid generateCMRRecord(final DataSource dataSource) throws Exception {

        ((JdbcDataSource) dataSource)
                .setURL("jdbc:h2:mem:JBTMDB;MVCC=TRUE;DB_CLOSE_DELAY=-1");

        // Test code
        Utils.createTables(dataSource.getConnection());

        // We can't just instantiate one as we need to be using the
        // same one as
        // the transaction
        // manager would have used to mark the transaction for GC

        Vector recoveryModules = manager.getModules();
        if (recoveryModules != null) {
            Enumeration modules = recoveryModules.elements();

            while (modules.hasMoreElements()) {
                RecoveryModule m = (RecoveryModule) modules.nextElement();

                if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
                    recoveryModule = (CommitMarkableResourceRecordRecoveryModule) m;
                } else if (m instanceof XARecoveryModule) {
                    XARecoveryModule  xarm = (XARecoveryModule) m;
                    xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception {
                            return true;
                        }

                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[] {xaResource};
                        }
                    });
                }
            }
        }
        // final Object o = new Object();
        // synchronized (o) {
        final Uid[] uids = new Uid[1];

        Thread foo = new Thread(new Runnable() {

            public void run() {

                try {
                    jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                            .transactionManager();

                    tm.begin();

                    Connection localJDBCConnection = dataSource.getConnection();
                    localJDBCConnection.setAutoCommit(false);
                    nonXAResource = new JDBCConnectableResource(
                            localJDBCConnection);
                    tm.getTransaction().enlistResource(nonXAResource);

                    xaResource = new SimpleXAResource();
                    tm.getTransaction().enlistResource(xaResource);

                    localJDBCConnection.createStatement().execute(
                            "INSERT INTO foo (bar) VALUES (1)");
                    uids[0] = ((TransactionImple)tm.getTransaction()).get_uid();
                    tm.commit();
                } catch (ExecuteException t) {
                } catch (Exception t) {
                    t.printStackTrace();
                    failed = true;
                } catch (Error t) {
                }
            }
        });
        foo.start();
        foo.join();

        assertFalse(failed);

        return uids[0];
    }

    protected void checkCMRRecovery(final DataSource dataSource) throws NamingException, ObjectStoreException {
        // This is test code, it allows us to verify that the
        // correct XID was
        // removed
        Xid committed = nonXAResource.getStartedXid();
        assertNotNull(committed);
        // The recovery module has to perform lookups
        new InitialContext().rebind("commitmarkableresource", dataSource);

        // Now we need to correctly complete the transaction
        manager.scan();
        assertFalse(recoveryModule.wasCommitted("commitmarkableresource",
                committed));
        // Now we need to correctly complete the transaction
        manager.scan();

        assertFalse(xaResource.wasCommitted());
        assertTrue(xaResource.wasRolledback());
    }
}
