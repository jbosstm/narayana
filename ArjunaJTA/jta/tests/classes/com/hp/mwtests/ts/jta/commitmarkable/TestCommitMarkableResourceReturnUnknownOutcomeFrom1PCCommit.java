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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.h2.jdbcx.JdbcDataSource;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.tools.log.EditableAtomicAction;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

public class TestCommitMarkableResourceReturnUnknownOutcomeFrom1PCCommit {

    private NamingBeanImpl namingBeanImpl = null;

    private String resetPropertiesFile;

    protected RecoveryManager manager;

    private JDBCConnectableResource nonXAResource;
    private SimpleXAResource xaResource;

    @Before
    public final void setup() throws Exception {

        File file = new File(System.getProperty("user.dir") + "/ObjectStore");
        if (file.exists()) {
            Utils.removeRecursive(file.toPath());
        }

        System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        namingBeanImpl = new NamingBeanImpl();
        namingBeanImpl.start();

        resetPropertiesFile = System.getProperty("com.arjuna.ats.arjuna.common.propertiesFile");
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "commitmarkableresourcejbossts-properties.xml");

        manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
    }

    @After
    public final void tearDown() {

        namingBeanImpl.stop();
        namingBeanImpl = null;

        if (resetPropertiesFile != null) {
            System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", resetPropertiesFile);
        } else {
            System.clearProperty("com.arjuna.ats.arjuna.common.propertiesFile");
        }
    }

    @Test
    public void testRMFAILAfterCommit() throws Exception {
        jtaPropertyManager.getJTAEnvironmentBean().setNotifyCommitMarkableResourceRecoveryModuleOfCompleteBranches(false);
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
                    XARecoveryModule xarm = (XARecoveryModule) m;
                    xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception {
                            return true;
                        }

                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[] { xaResource };
                        }
                    });
                }
            }
        }
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Uid get_uid = ((TransactionImple) tm.getTransaction()).get_uid();

        Connection localJDBCConnection = dataSource.getConnection();
        localJDBCConnection.setAutoCommit(false);

        nonXAResource = new JDBCConnectableResource(localJDBCConnection) {
            @Override
            public void commit(Xid arg0, boolean arg1) throws XAException {
                super.commit(arg0, arg1);
                throw new XAException(XAException.XAER_RMFAIL);
            }
        };
        tm.getTransaction().enlistResource(nonXAResource);

        xaResource = new SimpleXAResource();
        tm.getTransaction().enlistResource(xaResource);

        localJDBCConnection.createStatement().execute("INSERT INTO foo (bar) VALUES (1)");

        tm.commit();

        assertTrue(xaResource.wasCommitted());

        Xid committed = ((JDBCConnectableResource) nonXAResource).getStartedXid();
        assertNotNull(committed);

        InputObjectState uids = new InputObjectState();
        StoreManager.getRecoveryStore().allObjUids(new AtomicAction().type(), uids);
        Uid uid = UidHelper.unpackFrom(uids);
        assertTrue(uid.equals(get_uid));

        // Belt and braces but we don't expect the CMR to be removed anyway as
        // the RM is "offline"
        manager.scan();
        manager.scan();

        // The recovery module has to perform lookups
        new InitialContext().rebind("commitmarkableresource", dataSource);

        assertTrue(commitMarkableResourceRecoveryModule.wasCommitted("commitmarkableresource", committed));

        manager.scan(); // This will complete the atomicaction

        StoreManager.getRecoveryStore().allObjUids(new AtomicAction().type(), uids);
        uid = UidHelper.unpackFrom(uids);
        assertTrue(uid.equals(Uid.nullUid()));

        manager.scan(); // This is when the CMR deletes are done due to ordering
                        // of the recovery modules

        assertFalse(commitMarkableResourceRecoveryModule.wasCommitted("commitmarkableresource", committed));
    }

    @Test
    public void testRMFAILAfterNoCommit() throws Exception {
        jtaPropertyManager.getJTAEnvironmentBean().setNotifyCommitMarkableResourceRecoveryModuleOfCompleteBranches(false);
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
                    XARecoveryModule xarm = (XARecoveryModule) m;
                    xarm.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                        public boolean initialise(String p) throws Exception {
                            return true;
                        }

                        public XAResource[] getXAResources() throws Exception {
                            return new XAResource[] { xaResource };
                        }
                    });
                }
            }
        }
        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Uid get_uid = ((TransactionImple) tm.getTransaction()).get_uid();

        Connection localJDBCConnection = dataSource.getConnection();
        localJDBCConnection.setAutoCommit(false);

        nonXAResource = new JDBCConnectableResource(localJDBCConnection) {
            @Override
            public void commit(Xid arg0, boolean arg1) throws XAException {
                throw new XAException(XAException.XAER_RMFAIL);
            }
        };
        tm.getTransaction().enlistResource(nonXAResource);

        xaResource = new SimpleXAResource();
        tm.getTransaction().enlistResource(xaResource);

        localJDBCConnection.createStatement().execute("INSERT INTO foo (bar) VALUES (1)");

        tm.commit();

        assertTrue(xaResource.wasCommitted());

        Xid committed = ((JDBCConnectableResource) nonXAResource).getStartedXid();
        assertNotNull(committed);

        InputObjectState uids = new InputObjectState();
        StoreManager.getRecoveryStore().allObjUids(new AtomicAction().type(), uids);
        Uid uid = UidHelper.unpackFrom(uids);
        assertTrue(uid.equals(get_uid));

        // Belt and braces but we don't expect the CMR to be removed anyway as
        // the RM is "offline"
        manager.scan();
        manager.scan();

        // The recovery module has to perform lookups
        new InitialContext().rebind("commitmarkableresource", dataSource);

        assertFalse(commitMarkableResourceRecoveryModule.wasCommitted("commitmarkableresource", committed));

        manager.scan(); // This will make the AA a heuristic

        EditableAtomicAction editableAtomicAction = new EditableAtomicAction(get_uid);
        editableAtomicAction.deleteHeuristicParticipant(0);
        manager.scan();

        StoreManager.getRecoveryStore().allObjUids(new AtomicAction().type(), uids);
        uid = UidHelper.unpackFrom(uids);
        assertTrue(uid.equals(Uid.nullUid()));

        manager.scan(); // This is when the CMR deletes are done due to ordering
                        // of the recovery modules

        assertFalse(commitMarkableResourceRecoveryModule.wasCommitted("commitmarkableresource", committed));
    }
}
