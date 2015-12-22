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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.recovery;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.common.opPropertyManager;

public class UnserializableSerializableXAResourceTest {
    private ORB myORB;
    private RootOA myOA;
    private RecoveryManager recoveryManager;
    private boolean committed;

    @Before
    public void setup() throws InvalidName, SystemException {
        System.setProperty("OrbPortabilityEnvironmentBean.orbImpleClassName", System.getProperty("OrbPortabilityEnvironmentBean.orbImpleClassName", "com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4"));
        System.setProperty("OrbPortabilityEnvironmentBean.poaImpleClassName", System.getProperty("OrbPortabilityEnvironmentBean.poaImpleClassName", "com.arjuna.orbportability.internal.orbspecific.javaidl.oa.implementations.javaidl_1_4"));
        System.setProperty("OrbPortabilityEnvironmentBean.orbDataClassName", System.getProperty("OrbPortabilityEnvironmentBean.orbDataClassName", "com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4"));

        final Map<String, String> orbInitializationProperties = new HashMap<String, String>();
        orbInitializationProperties.put("com.arjuna.orbportability.orb.PreInit1",
            "com.arjuna.ats.internal.jts.recovery.RecoveryInit");
        opPropertyManager.getOrbPortabilityEnvironmentBean()
            .setOrbInitializationProperties(orbInitializationProperties);

        final Properties initORBProperties = new Properties();
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""
            + jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort());

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);
        myORB.initORB(new String[] {}, initORBProperties);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        final List<String> recoveryExtensions = new ArrayList<String>();
        recoveryExtensions.add(XARecoveryModule.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(recoveryExtensions);

        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(2);
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(1);

        final List<String> recoveryActivatorClassNames = new ArrayList<String>();
        recoveryActivatorClassNames.add(RecoveryEnablement.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean()
            .setRecoveryActivatorClassNames(recoveryActivatorClassNames);

        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryManager.initialize();

    }

    @After
    public void tearDown() {
        recoveryManager.terminate();
        myOA.destroy();
        myORB.shutdown();
    }

    @Test
    public void test() throws Exception
    {
        XAResource res1 = new XAResource() {

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
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                // TODO Auto-generated method stub

            }

            @Override
            public void rollback(Xid xid) throws XAException {
                // TODO Auto-generated method stub

            }

            @Override
            public void forget(Xid xid) throws XAException {
                // TODO Auto-generated method stub

            }

            @Override
            public Xid[] recover(int flag) throws XAException {
                // TODO Auto-generated method stub
                return null;
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

        };

        UnserializableSerializableXAResource res2 = new UnserializableSerializableXAResource(true);

        final javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
            .transactionManager();
        tm.getStatus();
        tm.begin();
        javax.transaction.Transaction theTransaction = tm.getTransaction();
        assertTrue(theTransaction.enlistResource(res1));
        assertTrue(theTransaction.enlistResource(res2));
        tm.commit();

        XARecoveryModule xaRecoveryModule = null;
        for (RecoveryModule recoveryModule : ((Vector<RecoveryModule>) recoveryManager.getModules())) {
            if (recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule) recoveryModule;
                break;
            }
        }
        final Xid xid = res2.getXid();

        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {

            @Override
            public boolean initialise(String p) throws Exception {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] { new XAResource() {

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
                        // TODO Auto-generated method stub
                        return 0;
                    }

                    @Override
                    public void commit(Xid xid, boolean onePhase) throws XAException {
                        committed = true;
                    }

                    @Override
                    public void rollback(Xid xid) throws XAException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void forget(Xid xid) throws XAException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public Xid[] recover(int flag) throws XAException {
                        if (committed) {
                            return null;
                        } else {
                            return new Xid[]{xid};
                        }
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

                } };
            }
        });


        recoveryManager.scan();
        
        assertTrue(committed);

    }

}
