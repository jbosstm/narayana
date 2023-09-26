/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery.nonuniquexids;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.resource.spi.XATerminator;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.List;

import static com.arjuna.ats.internal.arjuna.FormatConstants.XTS_BRIDGE_FORMAT_ID;

// unit test based on a Jonathan Halliday's code
/**
 * If two resources are enlisted with a transaction and they have the same branch id
 * then it is possible for one of the resources to be asked to commit twice and the
 * other one not at all.
 *
 * In this case we need to use the JNDI name to discriminate between them.
 * If the resources are wrapped using XAResourceWrapper then the JNDI name is
 * used when choosing the which XA Resource to XA ops on.
 *
 * The wrapping is managed by the XAResourceRecordWrappingPluginImpl (which has been copied
 * here from the jbossatx integration API).
 *
 * There are two tests, one which uses the wrapper and one that does not.
 */
public class ImportNonUniqueBranchTest {
    private static XARecoveryModule xaRecoveryModule;
    private static PeriodicRecovery periodicRecovery;

    @BeforeClass
    public static void beforeClass() {
        RecoveryEnvironmentBean environmentBean = BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class);
        List<String> moduleNames = new ArrayList<>();
        moduleNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinateAtomicActionRecoveryModule");
        moduleNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");
        environmentBean.setRecoveryModuleClassNames(moduleNames);
        environmentBean.setRecoveryBackoffPeriod(1);

        BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class).setXAResourceRecordWrappingPlugin(new XAResourceRecordWrappingPluginImpl());

        periodicRecovery = new PeriodicRecovery(false, false);

        for (RecoveryModule recoveryModule : periodicRecovery.getModules()) {
            if (recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }
    }

    @AfterClass
    public static void afterClass() {
        periodicRecovery.shutdown(false);
    }

    @Test
    public void testWrapped() throws Exception {
        test(true);
        Assert.assertEquals("resource commit error", 0, XAResourceImpl.getErrorCount());
    }

    public void test(boolean wrap) throws Exception {
        XAResourceImpl.clearErrorCount();
        ResourceManager resourceManagerA = new ResourceManager("jndi:/A", wrap);
        ResourceManager resourceManagerB = new ResourceManager("jndi:/B", wrap);

        xaRecoveryModule.addXAResourceRecoveryHelper(resourceManagerA);
        xaRecoveryModule.addXAResourceRecoveryHelper(resourceManagerB);

        // create an Xid that is different from JTA_FORMAT_ID in order to exercise the non JTA code path
        // (see XARecoveryModule#getTheKey())
        Xid xid = XATxConverter.getXid(new Uid(), false, XTS_BRIDGE_FORMAT_ID);
        Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid, 10000);
        XATerminator xaTerminator = SubordinationManager.getXATerminator();

        XAResource resource1 = resourceManagerA.getResource("a1");
        XAResource resource2 = resourceManagerB.getResource("b1");

        tx.enlistResource(resource1);
        tx.enlistResource(resource2);

        xaTerminator.prepare(xid);
        // ./target/test-classes/ObjectStore/ShadowNoFileLockStore/defaultStore/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/SubordinateAtomicAction/JCA/_

        periodicRecovery.doWork(); // will take recoveryBackoffPeriod seconds, be patient

        xaTerminator.commit(xid, false);
    }
}