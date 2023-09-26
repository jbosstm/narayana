/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.jts.recovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.common.opPropertyManager;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ResourceManagerFailureUnitTest {

    private ORB testORB;

    private OA testOA;

    private TransactionManager transactionManager;

    private RecoveryManager recoveryManager;

    @Before
    public void before() throws Exception {
        final Map<String, String> orbInitializationProperties = new HashMap<String, String>();
        orbInitializationProperties.put("com.arjuna.orbportability.orb.PreInit1",
                "com.arjuna.ats.internal.jts.recovery.RecoveryInit");
        opPropertyManager.getOrbPortabilityEnvironmentBean()
                .setOrbInitializationProperties(orbInitializationProperties);

        final Properties initORBProperties = new Properties();
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""
                + jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort());

        testORB = ORB.getInstance("test");
        testOA = OA.getRootOA(testORB);
        testORB.initORB(new String[] {}, initORBProperties);
        testOA.initOA();
        ORBManager.setORB(testORB);
        ORBManager.setPOA(testOA);

        final List<String> recoveryExtensions = new ArrayList<String>();
        recoveryExtensions.add(XARecoveryModule.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(recoveryExtensions);

        final List<String> recoveryActivatorClassNames = new ArrayList<String>();
        recoveryActivatorClassNames.add(RecoveryEnablement.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryActivatorClassNames(recoveryActivatorClassNames);

        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryManager.initialize();

        transactionManager = new TransactionManagerImple();
    }

    @After
    public void after() throws Exception {
        try {
            if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                transactionManager.rollback();
            }
        } finally {
            testOA.destroy();
            testORB.shutdown();
        }
    }

    @Test
    public void test() throws Exception {
        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(new DummyXA(true));
        transactionManager.getTransaction().enlistResource(new DummyXARMFail(true));
        transactionManager.commit();

        final int uidsCountBeforeRecovery = getUidsCount();
        final RecoveryModule recoveryModule = recoveryManager.getModules().get(0);

        recoveryModule.periodicWorkFirstPass();
        recoveryModule.periodicWorkSecondPass();

        Assert.assertEquals(uidsCountBeforeRecovery - 1, getUidsCount());
    }

    private int getUidsCount() throws Exception {
        final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        InputObjectState states = new InputObjectState();
        int counter = 0;

        if (recoveryStore.allObjUids(XAResourceRecord.typeName(), states) && states.notempty()) {
            while (UidHelper.unpackFrom(states).notEquals(Uid.nullUid())) {
                counter++;
            }
        }

        return counter;
    }

    public static class DummyXARMFail extends DummyXA {

        private static boolean WAS_COMMITED;

        public DummyXARMFail(final boolean print) {
            super(print);
        }

        public void commit(Xid xid, boolean onePhase) throws XAException {
            super.commit(xid, onePhase);

            if (!WAS_COMMITED) {
                WAS_COMMITED = true;

                throw new XAException(XAException.XAER_RMFAIL);
            }
        }
    }
}
