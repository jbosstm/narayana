/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.transaction.TransactionManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Tests the scenario when transaction manager crashes after all resources were committed,
 * but before object store was updated.
 *
 * In such scenario, XAResourceWrapper resources with not empty JNDI name should be removed from the object store.
 * The rest of the resources, should be kept in the object store for the administrator to handle.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public class CrashAfterResourcesCommitTest {

    private static String ATOMIC_ACTION_TYPE;

    private TransactionManager transactionManager;

    private RecoveryManager recoveryManager;

    private RecoveryStore recoveryStore;

    @BeforeClass
    public static void beforeClass() {
        if (System.getProperty("com.arjuna.ats.arjuna.common.propertiesFile") == null) {
            System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "jbossts-properties.xml");
        }

        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecordWrappingPluginClassName(
                "com.hp.mwtests.ts.jta.recovery.TestXAResourceRecordWrappingPlugin"
        );
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(Arrays.asList(
                "com.hp.mwtests.ts.jta.recovery.TestXAResourceRecovery"
        ));

        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(Arrays.asList(
                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"));

        ATOMIC_ACTION_TYPE = new AtomicAction().type();
    }

    @Before
    public void before() throws Exception {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryStore = StoreManager.getRecoveryStore();

        clearObjectStore();
    }

    @After
    public void after() throws Exception {
        clearObjectStore();
    }

    /**
     * Enlists two XAResourceWrapper resources to the transaction.
     *
     * Both resources should be marked as contacted during the recovery. And transaction record should be removed.
     *
     * @throws Exception
     */
    @Test
    @BMRule(name = "Don't clean TM log after commit",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "updateState",
        targetLocation = "AT ENTRY",
        condition = "!flagged(\"testTwoXAResourceWrappers\")",
        action = "flag(\"testTwoXAResourceWrappers\"); return")
    public void testTwoXAResourceWrappers() throws Exception {
        final TestXAResourceWrapper firstXAResourceWrapper = new TestXAResourceWrapper("first", "first", "first");
        final TestXAResourceWrapper secondXAResourceWrapper = new TestXAResourceWrapper("second", "second", "second");
        TestXAResourceRecovery.setResources(firstXAResourceWrapper, secondXAResourceWrapper);

        final int uidsCountBeforeTest = getUidsCountInStore();

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(firstXAResourceWrapper);
        transactionManager.getTransaction().enlistResource(secondXAResourceWrapper);
        transactionManager.commit();

        Assert.assertEquals(1, firstXAResourceWrapper.commitCount());
        Assert.assertEquals(1, secondXAResourceWrapper.commitCount());
        Assert.assertEquals(uidsCountBeforeTest + 1, getUidsCountInStore());

        recoveryManager.initialize();
        recoveryManager.scan();

        Assert.assertEquals(uidsCountBeforeTest, getUidsCountInStore());

        final Set<String> contactedJndiNames = getContactedJndiNames();
        Assert.assertEquals(2, contactedJndiNames.size());
        Assert.assertTrue(contactedJndiNames.contains("first"));
        Assert.assertTrue(contactedJndiNames.contains("second"));
    }

    /**
     * Enlists one XAResource and one XAResourceWrapper resources.
     *
     * XAResourceWrapper resource should be marked as contacted. Transaction record should still be in the object store.
     *
     * @throws Exception
     */
    @Test
    @BMRule(name = "Don't clean TM log after commit",
            targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "updateState",
            targetLocation = "AT ENTRY",
            condition = "!flagged(\"testXAResourceAndXAResourceWrapper\")",
            action = "flag(\"testXAResourceAndXAResourceWrapper\"); return")
    public void testXAResourceAndXAResourceWrapper() throws Exception {
        final TestXAResource xaResource = new TestXAResource();
        final TestXAResourceWrapper xaResourceWrapper = new TestXAResourceWrapper("first", "first", "first");
        TestXAResourceRecovery.setResources(xaResource, xaResourceWrapper);

        final int uidsCountBeforeTest = getUidsCountInStore();

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResource);
        transactionManager.getTransaction().enlistResource(xaResourceWrapper);
        transactionManager.commit();

        Assert.assertEquals(1, xaResource.commitCount());
        Assert.assertEquals(1, xaResourceWrapper.commitCount());
        Assert.assertEquals(uidsCountBeforeTest + 1, getUidsCountInStore());

        recoveryManager.initialize();
        recoveryManager.scan();

        Assert.assertEquals(uidsCountBeforeTest + 1, getUidsCountInStore());

        final Set<String> contactedJndiNames = getContactedJndiNames();
        Assert.assertEquals(1, contactedJndiNames.size());
        Assert.assertTrue(contactedJndiNames.contains("first"));
    }

    /**
     * Enlists two XAResource resources.
     *
     * Transaction record should still be in the object store. And no resources marked as contacted.
     *
     * @throws Exception
     */
    @Test
    @BMRule(name = "Don't clean TM log after commit",
            targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "updateState",
            targetLocation = "AT ENTRY",
            condition = "!flagged(\"testTwoXAResources\")",
            action = "flag(\"testTwoXAResources\"); return")
    public void testTwoXAResources() throws Exception {
        final TestXAResource firstXAResource = new TestXAResource();
        final TestXAResource secondXAResource = new TestXAResource();
        TestXAResourceRecovery.setResources(firstXAResource, secondXAResource);

        final int uidsCountBeforeTest = getUidsCountInStore();

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(firstXAResource);
        transactionManager.getTransaction().enlistResource(secondXAResource);
        transactionManager.commit();

        Assert.assertEquals(1, firstXAResource.commitCount());
        Assert.assertEquals(1, secondXAResource.commitCount());
        Assert.assertEquals(uidsCountBeforeTest + 1, getUidsCountInStore());

        recoveryManager.initialize();
        recoveryManager.scan();

        Assert.assertEquals(uidsCountBeforeTest + 1, getUidsCountInStore());

        final Set<String> contactedJndiNames = getContactedJndiNames();
        Assert.assertEquals(0, contactedJndiNames.size());
    }

    private int getUidsCountInStore() throws Exception {
        final InputObjectState uids = new InputObjectState();

        recoveryStore.allObjUids(ATOMIC_ACTION_TYPE, uids);

        int counter = 0;
        for (Uid uid = UidHelper.unpackFrom(uids); !uid.equals(Uid.nullUid()); uid = UidHelper.unpackFrom(uids)) {
            counter++;
        }

        return counter;
    }

    private void clearObjectStore() throws Exception {
        final InputObjectState uids = new InputObjectState();

        recoveryStore.allObjUids(ATOMIC_ACTION_TYPE, uids);

        for (Uid uid = UidHelper.unpackFrom(uids); !uid.equals(Uid.nullUid()); uid = UidHelper.unpackFrom(uids)) {
            recoveryStore.remove_committed(uid, ATOMIC_ACTION_TYPE);
        }
    }

    private Set<String> getContactedJndiNames() {
        final Vector<RecoveryModule> recoveryModules = RecoveryManager.manager().getModules();

        for (final RecoveryModule recoveryModule : recoveryModules) {
            if (recoveryModule instanceof XARecoveryModule) {
                return ((XARecoveryModule) recoveryModule).getContactedJndiNames();
            }
        }

        return new HashSet<String>();
    }
}