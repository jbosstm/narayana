package com.hp.mwtests.ts.jta.jts.xa;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceManagerImple;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

import javax.transaction.xa.XAResource;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XAResourceRecoveryTest {
    private int orphanSafetyInterval;
    private List<String> xaRecoveryNodes;
    private int recoveryBackoffPeriod;
    private List<XAResourceOrphanFilter> xaResourceOrphanFilters;
    private List<RecoveryModule> recoveryModules;
    private com.arjuna.orbportability.ORB myORB;
    private RootOA myOA;

    @Before
    public void setup() throws InvalidName {
        orphanSafetyInterval = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        recoveryBackoffPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod();
        xaResourceOrphanFilters = jtaPropertyManager.getJTAEnvironmentBean().getXaResourceOrphanFilters();
        recoveryModules = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryModules();

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);
        myORB.initORB(new String[]{}, null);
        myOA.initOA();
        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }

    @After
    public void tearDown() {
        RecoveryManager.manager().terminate();
        myOA.destroy();
        myORB.shutdown();

        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModules(recoveryModules);
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceOrphanFilters(xaResourceOrphanFilters);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(recoveryBackoffPeriod);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(orphanSafetyInterval);
    }

    @Test
    public void testRMFAILOutOfRollback() throws Exception {
        // This is set up to get an entry in the object store that will be found by bottom up recovery. It is important
        // to do it this way because for the test we want the XARecoveryModule to find a "failed" resource - which is
        // different to just scanning the XAResource
        Uid uid = new Uid();
        XidImple xidImple = new XidImple(uid);
        XAResource xaResource = new RMFAILOutofRollbackResource(xidImple); // Note that this is serializable and will be deleted after recover is failed once
        XAResourceRecord xares = new XAResourceRecord(new TransactionImple(), xaResource, xidImple, null);
        OutputObjectState os = new OutputObjectState();
        assertTrue(xares.saveState(os));
        assertTrue(StoreManager.getParticipantStore().write_committed(uid, XAResourceRecord.typeName(), os));

        // Set up to ensure that a single scan will (in a timely manner) find and try to rollback the resource in a
        // single scan
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(1);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES));
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(2);
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceOrphanFilters(Arrays.asList(new XAResourceOrphanFilter[]{new com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter(), new com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter()}));

        // Make sure that the XAResource we are going to try to recover has the same Xid as the one in setup so that the
        // XARecoveryModule will treat it as a failed one
        RecoveryModule xaRecoveryModule = new MyXARecoveryModule();
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModules(Arrays.asList(new RecoveryModule[]{xaRecoveryModule}));

        assertNotNull("Could not find recovery state before recovery fails", StoreManager.getParticipantStore().read_committed(uid, XAResourceRecord.typeName()));
        RecoveryManager.manager().scan();
        assertNotNull("Could not find recovery state after recovery fails", StoreManager.getParticipantStore().read_committed(uid, XAResourceRecord.typeName()));
    }

    /**
     * This class is intended to be similar (from a RecoveryModule point of view) to
     * com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule but in particular it uses a test class called
     * MyXARecoveryResourceManagerImple as the XARecoveryResourceManager so that we can simulate OBJECT_NOT_EXIST when
     * replay_completion is called
     */
    private class MyXARecoveryModule extends com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule {
        public MyXARecoveryModule() {
            super(new MyXARecoveryResourceManagerImple(),
                    "MyXARecoveryResourceManagerImple");

            com.arjuna.ats.internal.jta.Implementationsx.initialise();
        }
    }

    /**
     * The most signficant part of this test class is just to simulate a OBJECT_NOT_EXIST out of replay_completion
     * during the call to recover() in the XAResourceRecord when triggered by the XARecoveryModule.
     */
    private class MyXARecoveryResourceManagerImple extends XARecoveryResourceManagerImple {
        public XARecoveryResource getResource(Uid uid) {
            XARecoveryResourceImple xaRecoveryResourceImple = new XARecoveryResourceImple(uid);
            xaRecoveryResourceImple.setRecoveryCoordinator(new RecoveryCoordinator() {

                @Override
                public Status replay_completion(Resource r) throws NotPrepared {
                    throw new OBJECT_NOT_EXIST();
                }

                @Override
                public boolean _is_a(String repositoryIdentifier) {
                    return false;
                }

                @Override
                public boolean _is_equivalent(Object other) {
                    return false;
                }

                @Override
                public boolean _non_existent() {
                    return false;
                }

                @Override
                public int _hash(int maximum) {
                    return 0;
                }

                @Override
                public Object _duplicate() {
                    return null;
                }

                @Override
                public void _release() {

                }

                @Override
                public Object _get_interface_def() {
                    return null;
                }

                @Override
                public Request _request(String operation) {
                    return null;
                }

                @Override
                public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result) {
                    return null;
                }

                @Override
                public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result, ExceptionList exclist, ContextList ctxlist) {
                    return null;
                }

                @Override
                public Policy _get_policy(int policy_type) {
                    return null;
                }

                @Override
                public DomainManager[] _get_domain_managers() {
                    return new DomainManager[0];
                }

                @Override
                public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {
                    return null;
                }
            });
            return xaRecoveryResourceImple;
        }
    }
}
