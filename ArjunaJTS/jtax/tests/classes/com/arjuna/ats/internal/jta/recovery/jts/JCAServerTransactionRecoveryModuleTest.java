/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
package com.arjuna.ats.internal.jta.recovery.jts;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.Implementationsx;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.ORBPackage.InvalidName;

import javax.resource.spi.XATerminator;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JCAServerTransactionRecoveryModuleTest {
    private boolean commitCalled;

    @Before
    public void setup() throws InvalidName {
        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[0], null);
        myOA.initOA();

        com.arjuna.ats.internal.jts.ORBManager.setORB(myORB);
        com.arjuna.ats.internal.jts.ORBManager.setPOA(myOA);

        RecoveryManager.manager().initialize();
        Implementationsx.initialise();
    }

    @After
    public void tearDown() {
        RecoveryManager.manager().terminate();

        ORB myORB = ORB.getInstance("test");
        RootOA myOA = OA.getRootOA(myORB);
        myOA.destroy();
        myORB.shutdown();
    }

    @Test
    public void testReloadStateAcceptable() throws XAException, RollbackException, SystemException {
        JCAServerTransactionRecoveryModule module = new JCAServerTransactionRecoveryModule();
        XATerminator terminator = new XATerminatorImple();
        Xid xid = new Xid() {

            @Override
            public int getFormatId() {
                return 0;
            }

            @Override
            public byte[] getGlobalTransactionId() {
                return new byte[0];
            }

            @Override
            public byte[] getBranchQualifier() {
                return new byte[0];
            }
        };
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        SubordinateTransaction importedTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        importedTransaction.enlistResource(new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                commitCalled = true;
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
        });
        terminator.prepare(xid);
        module.periodicWorkFirstPass();
        module.periodicWorkSecondPass();
        module.periodicWorkFirstPass();
        module.periodicWorkSecondPass();
        assertFalse(commitCalled);
        terminator.commit(xid, false);
        assertTrue(commitCalled);
    }
}