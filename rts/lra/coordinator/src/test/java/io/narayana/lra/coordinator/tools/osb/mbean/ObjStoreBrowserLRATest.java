/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.coordinator.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import io.narayana.lra.coordinator.domain.model.FailedLongRunningAction;
import io.narayana.lra.coordinator.domain.model.LRAParticipantRecord;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;
import io.narayana.lra.coordinator.internal.Implementations;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ObjStoreBrowserLRATest {
    private RecoveryManagerImple recoveryManager;
    private ObjStoreBrowser osb;

    private static String[][] LRA_OSB_TYPES = {
            // osTypeClassName, beanTypeClassName - see com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser
            {LongRunningAction.getType().substring(1), LongRunningAction.class.getName(), LRAActionBean.class.getName()},
            {FailedLongRunningAction.getType().substring(1), FailedLongRunningAction.class.getName(), LRAActionBean.class.getName()}
    };

    @Before
    public void setUp() {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        Implementations.install();
        recoveryManager = new RecoveryManagerImple(false);
        recoveryManager.addModule(new LRARecoveryModule());

        // initiating the ObjStoreBrowser
        osb = new ObjStoreBrowser();
        for(String[] typeAndBean: LRA_OSB_TYPES) {
            assertTrue(osb.addType(typeAndBean[0], typeAndBean[1], typeAndBean[2]));
        }
        osb.start();
    }

    @After
    public void tearDown () {
        recoveryManager.removeAllModules(false);
        recoveryManager.stop(false);
        Implementations.uninstall();

        osb.stop();
    }

    @Test
    public void lraMBean() throws Exception {
        String lraUrl = "http://localhost:8080/lra";

        LongRunningAction lra = LRARecoveryModule.getService()
                .startLRA(lraUrl, null, "client", Long.MAX_VALUE);

        osb.probe();
        UidWrapper uidWrapper = osb.findUid(lra.get_uid());
        assertEquals("Probed LRA uid has to be equal to what the LRA was created with",
                lra.get_uid(), uidWrapper.getUid());

        LRARecoveryModule.getService()
                .endLRA(lra.getId(), false, false);

        osb.probe();
        uidWrapper = osb.findUid(lra.get_uid());
        assertNull("Expected the LRA records were removed", uidWrapper);

        osb.stop();
    }

    @Test
    public void lraMBeanRemoval() throws Exception {
        LongRunningAction lra = new LongRunningAction(new Uid());
        OSEntryBean lraOSEntryBean = null;
        try {
            lra.begin(Long.MAX_VALUE); // Creating the LRA records in the log store.
            String coordinatorUrl = "http://localhost:8080/lra-coordinator";
            String participantUrl = "http://localhost:8080/lra-participant";
            LRAParticipantRecord lraParticipant = lra.enlistParticipant(URI.create(coordinatorUrl), participantUrl,
                    "/recover", Long.MAX_VALUE, null);

            osb.probe();

            UidWrapper uidWrapper = osb.findUid(lra.get_uid());
            assertNotNull("Expected the LRA MBean uid was probed", uidWrapper);
            lraOSEntryBean = uidWrapper.getMBean();
            assertNotNull("Expecting the UID to contain the LRA mbean", lraOSEntryBean);
            assertTrue("The mbean should wrap " + ActionBean.class.getName() + " but it's " + lraOSEntryBean.getClass().getName(),
                    lraOSEntryBean instanceof ActionBean);
            ActionBean actionBean = (ActionBean) lraOSEntryBean;
            assertEquals("One participant was enlisted", 1, actionBean.getParticipants().size());
            LogRecordWrapper logRecord = actionBean.getParticipants().iterator().next();
            assertTrue("The log wrapper needs to be from LRA", logRecord instanceof LRAParticipantRecordWrapper);
            LRAParticipantRecordWrapper lraRecord = (LRAParticipantRecordWrapper) logRecord;
            Assert.assertEquals("Participant should be active", LRAStatus.Active.name(), lraRecord.getLRAStatus());
            Assert.assertEquals("Compensator URI is expected as it was registered with '/compensate' suffix",
                    participantUrl + "/compensate", lraRecord.getCompensator());
        } finally {
            // this removal is part of the test where we check that remove on OS bean works in later check
            lraOSEntryBean.remove(false);
        }

        osb.probe();

        UidWrapper uidWrapper = osb.findUid(lra.get_uid());
        assertNull("Expected the LRA records were removed", uidWrapper);
    }

    @Test
    public void lraFailedMBean() throws Exception {
        String lraUrl = "http://localhost:8080/lra";

        LongRunningAction lra = LRARecoveryModule.getService()
                .startLRA(lraUrl, null, "client", Long.MAX_VALUE);

        // LongRunningAction -> FailedLongRunningAction
        LRARecoveryModule.getInstance()
                .moveEntryToFailedLRAPath(lra.get_uid());

        osb.probe();
        UidWrapper uidWrapper = osb.findUid(lra.get_uid());
        assertEquals("Probed LRA uid has to be equal to what the LRA was created with",
                lra.get_uid(), uidWrapper.getUid());

        OSEntryBean lraOSEntryBean = uidWrapper.getMBean();
        assertNotNull("Expecting the UID wrapper to contain the LRA mbean", lraOSEntryBean);
        assertTrue("The provided jmx mbean should wrap LRAActionBean", lraOSEntryBean instanceof LRAActionBean);
        LRAActionBean lraActionBean = (LRAActionBean) lraOSEntryBean;

        Assert.assertEquals("The probed action bean should be of type 'Failed LRA'",
                noSlash(lraActionBean.type()), noSlash(FailedLongRunningAction.FAILED_LRA_TYPE));

        assertFalse("Expected the recovery module cannot remove the failed LRA by Uid",
                LRARecoveryModule.getInstance().removeCommitted(uidWrapper.getUid()));
        uidWrapper = osb.findUid(lra.get_uid());
        assertNotNull("Failed LRA record should exist in log", uidWrapper);

        lraOSEntryBean.remove(true);
        uidWrapper = osb.findUid(lra.get_uid());
        assertNull("After removal the Failed LRA record should not exist in log anymore", uidWrapper);
    }

    private String noSlash(String type) {
        if (type == null || type.length() == 0) {
            return type;
        }
        char firstChar = type.charAt(0);
        return firstChar == '/' ? type.substring(1) : type;
    }
}