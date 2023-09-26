/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

import org.junit.Test;
import org.omg.CosTransactions.HeuristicHazard;

import javax.management.*;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class HeuristicInformationTest extends JTSOSBTestBase {

    public ObjStoreBrowser getOSB() throws MBeanException {
        OSBTypeHandler osbTypeHandler = new OSBTypeHandler(
                true,
                "com.hp.mwtests.ts.jta.jts.tools.UserExtendedCrashRecord",
                "com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper",
                UserExtendedCrashRecord.record_type(),
                null
        );

        ObjStoreBrowser osb = createObjStoreBrowser(false);

        osb.registerHandler(osbTypeHandler);

        return osb;
    }

    @Test
    public void heuristicInformationTest() throws Exception {
        ArjunaTransactionImple A = new ArjunaTransactionImple(null);
        int expectedHeuristic = TwoPhaseOutcome.HEURISTIC_ROLLBACK;
        ThreadActionData.purgeActions();

        UserExtendedCrashRecord recs[] = {
                new UserExtendedCrashRecord(UserExtendedCrashRecord.CrashLocation.NoCrash, UserExtendedCrashRecord.CrashType.Normal, null),
                new UserExtendedCrashRecord(UserExtendedCrashRecord.CrashLocation.CrashInCommit, UserExtendedCrashRecord.CrashType.HeuristicHazard,
                        new UserExtendedCrashRecord.HeuristicInformationOverride(expectedHeuristic)) // this value will override HeuristicHazard
        };

        RecordTypeManager.manager().add(new RecordTypeMap() {
            public Class<? extends AbstractRecord> getRecordClass () { return UserExtendedCrashRecord.class;}
            public int getType () {return RecordType.USER_DEF_FIRST1;}
        });

        A.start();

        for (UserExtendedCrashRecord rec : recs)
            A.add(rec);

        try {
            A.commit(true);
            fail("transaction commit should have produced a heuristic hazard");
        } catch (HeuristicHazard e) {
            // expected
        }

        ObjStoreBrowser osb = getOSB();

        osb.start();
        osb.probe();

        // there should now be an MBean entry corresponding to a JTS record, read it via JMX:
        MBeanServer mbs = JMXServer.getAgent().getServer();
        UidWrapper w = osb.findUid(A.get_uid());
        ObjectName txnON = new ObjectName(w.getName());
        Object aid = mbs.getAttribute(txnON, "Id");

        assertNotNull(aid);
        Set<ObjectName> participants = mbs.queryNames(new ObjectName(w.getName() + ",puid=*"), null);

        for (ObjectName on : participants) {
            AttributeList al = mbs.getAttributes(on, new String[]{"Id", "Status", "HeuristicStatus", "GlobalTransactionId"});
            for (Attribute a : al.asList()) {
                if ("HeuristicStatus".equals(a.getName())) {
                    HeuristicStatus ahs = HeuristicStatus.valueOf(a.getValue().toString());
                    HeuristicStatus ehs = HeuristicStatus.intToStatus(expectedHeuristic);

                    // assert that the instrumented heuristic status has the expected value
                    assertTrue(ahs.equals(ehs));
                }
            }
        }
    }
}