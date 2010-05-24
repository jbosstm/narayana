package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.hp.mwtests.ts.jta.jts.common.ExtendedCrashRecord;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import org.omg.CosTransactions.HeuristicHazard;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Test the the ObjStoreBrowser MBean in a JTS environment.
 */
public class JTSObjStoreBrowserTest extends TestBase {
    private RecoveryManagerImple rcm;

    @Before
    public void setUp () throws Exception
    {
//        recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

        rcm = new RecoveryManagerImple(true);
        rcm.addModule(new XARecoveryModule());
        rcm.addModule(new AtomicActionRecoveryModule());
    }

    @After
    public void tearDown () throws Exception
    {
        rcm.removeAllModules(false);
        rcm.stop(false);
    }

    private ObjStoreBrowser createObjStoreBrowser() {
        ObjStoreBrowser osb = new ObjStoreBrowser();

        osb.setTypes( new HashMap<String, String>() {{
			put("StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple", "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean");
			put("StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction", "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean");
		}});

        return osb;
    }

    /*
         TODO JTS test-compile doesn't pull in com.arjuna.common.tests.simple
    @Test
    public void testXAResourceRecordBean() throws Exception {
        com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new XAResourceRecordBean(new UidWrapper(Uid.nullUid())));
    }
    */

    /**
     * Create an atomic action with two participants, one of which will generate a heuristic during phase 2.
     * The test will move the heuristic back into the prepared state and trigger recovery to replay phase 2.
     * The test then asserts that the corresponding MBeans have been unregistered.
     * @throws Exception if test fails unexpectedly
     */
    @Test
    public void aaReplayTest() throws Exception {
        AtomicAction A = new AtomicAction();
        ExtendedCrashRecord recs[] = startTest(A);

        int outcome = A.commit();

		assertEquals(ActionStatus.H_HAZARD, outcome);

        finishTest(A, true, recs);
    }

    /**
     * Similar to @aaReplayTest except that the whole transaction record is removed from the object store
     * (instead of replaying the record that generates a heuristic).
     * @throws Exception if test fails unexpectedly
     */
    @Test
    public void aaRemoveTest() throws Exception {
        AtomicAction A = new AtomicAction();
        ExtendedCrashRecord recs[] = startTest(A);

        int outcome = A.commit();

		assertEquals(ActionStatus.H_HAZARD, outcome);

        finishTest(A, false, recs);
    }

    /**
     * Similar to aaReplayTest but uses a JTS transaction instead of an AtomicAction
     * @throws Exception if test fails unexpectedly
     */
    // TODO for replay to work on JTS participants ExtendedCrashReocrd wou needs to extend XAResourceRecord
    // TODO @Test
    public void jtsReplayTest() throws Exception {
        ArjunaTransactionImple A = new ArjunaTransactionImple(null);
        ExtendedCrashRecord recs[] = startTest(A);

        int outcome = ActionStatus.COMMITTED;

		try {
            A.commit(true);
        } catch (HeuristicHazard e) {
            outcome = ActionStatus.H_HAZARD;
        }

		assertEquals(ActionStatus.H_HAZARD, outcome);

        finishTest(A, true, recs);
    }

    /**
     * Similar to aaRemoveTest but uses a JTS transaction instead of an AtomicAction
     * @throws Exception if test fails unexpectedly
     */
    @Test
    public void jtsRemoveTest() throws Exception {
        ArjunaTransactionImple A = new ArjunaTransactionImple(null);
        ExtendedCrashRecord recs[] = startTest(A);

        int outcome = ActionStatus.COMMITTED;

		try {
            A.commit(true);
        } catch (HeuristicHazard e) {
            outcome = ActionStatus.H_HAZARD;
        }

		assertEquals(ActionStatus.H_HAZARD, outcome);

        finishTest(A, false, recs);
    }

    private ExtendedCrashRecord[] startTest(TwoPhaseCoordinator A) throws Exception {
        ThreadActionData.purgeActions();

        ExtendedCrashRecord recs[] = {
                new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.NoCrash, ExtendedCrashRecord.CrashType.Normal),
                new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.CrashInCommit, ExtendedCrashRecord.CrashType.HeuristicHazard)
        };

        RecordTypeManager.manager().add(new RecordTypeMap() {
            public Class<? extends AbstractRecord> getRecordClass () { return ExtendedCrashRecord.class;}
            public int getType () {return RecordType.USER_DEF_FIRST0;}
        });

        A.start();

        for (ExtendedCrashRecord rec : recs)
			A.add(rec);
        
        return recs;
    }

    private void finishTest(TwoPhaseCoordinator A, boolean replay, ExtendedCrashRecord ... recs) throws Exception {
        ObjStoreBrowser osb = createObjStoreBrowser();

        // there should now be an entry in the object store containing two participants
        osb.start();

        // there should be one MBean corresponding to the AtomicAction A
        UidWrapper w = osb.findUid(A.get_uid());
        assertNotNull(w);
        OSEntryBean ai = w.getMBean();
        assertNotNull(ai);

        // the MBean should wrap an ActionBean
        assertTrue(ai instanceof ActionBean);
        ActionBean actionBean = (ActionBean) ai;

        // and there should be one MBean corresponding to the CrashRecord that got the heuristic:
        int recCount = 0;
        for (ExtendedCrashRecord rec : recs) {
            LogRecordWrapper lw = actionBean.getParticipant(rec);

            if (lw != null) {
                recCount += 1;

                if (lw.isHeuristic()) {
                    if (replay) {
                        rec.forget();
                        lw.setStatus("PREPARED");
                        // the participant record should no longer be on the heuristic list
                        assertFalse(lw.isHeuristic());
                    }
                }
            }
        }

        assertEquals(1, recCount);

        if (!replay) {
            actionBean.remove();
        } else {
            /*
            * prompt the recovery manager to have a go at replaying the record that was
            * moved off the heuristic list and back onto the prepared list
            */
            Thread.sleep(1000); // TODO why does sleep cause the scan to recognise the record can be replayed
            rcm.scan();
        }

        // another probe should no longer find the record that got the heuristic
        // (since it was either removed or the RecoveryManager replayed the commit
        // phase) so its corresponding MBean will have been unregistered
        osb.probe();

        // look up the MBean and verify that it no longer exists
        w = osb.findUid(A.get_uid());
        assertNull(w);

        osb.stop();
    }
}