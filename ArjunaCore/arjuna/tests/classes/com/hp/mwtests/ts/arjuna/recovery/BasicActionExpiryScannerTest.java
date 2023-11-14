/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.recovery.AdvancedAtomicActionExpiryScanner;
import com.arjuna.ats.internal.arjuna.recovery.AdvancedAtomicActionPurgeExpiryScanner;
import com.arjuna.ats.internal.arjuna.recovery.BasicActionExpiryScanner;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test class covers multiple {@code ExpiryScanner} classes.
 *
 * @see BasicActionExpiryScanner}
 * @see AdvancedAtomicActionExpiryScanner
 * @see AdvancedAtomicActionPurgeExpiryScanner
 */
public class BasicActionExpiryScannerTest {

    private static final Logger log = Logger.getLogger(BasicActionExpiryScannerTest.class);
    private static final String TYPE_NAME = new AtomicAction().type();
    private static final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

    @BeforeAll
    public static void beforeAll() {
        recoveryPropertyManager.getRecoveryEnvironmentBean().setExpiryScanInterval(1);
    }

    public OutputObjectState createObjectState(Uid txId, Uid processUid) throws IOException {
        OutputObjectState os = new OutputObjectState();
        os.packStringBytes("#ARJUNA#".getBytes(StandardCharsets.UTF_8));
        UidHelper.packInto(txId, os);
        UidHelper.packInto(processUid, os);
        os.packLong(Instant.now().minus(Duration.ofHours(1)).toEpochMilli());
        os.packBoolean(true);
        os.packInt(RecordType.NONE_RECORD);
        os.packInt(0);
        os.packInt(ActionStatus.H_HAZARD);
        os.packInt(ActionType.TOP_LEVEL);
        os.packInt(TwoPhaseOutcome.HEURISTIC_HAZARD);
        return os;
    }

    @Test
    public void testAdvancedAtomicActionExpiryScanner() {
        boolean passed = false;
        Uid txId = new Uid();
        Uid processUid = new Uid();

        try {
            OutputObjectState os = createObjectState(txId, processUid);

            log.debug("Creating dummy log");
            recoveryStore.write_committed(txId, TYPE_NAME, os);
            if (recoveryStore.currentState(txId, TYPE_NAME) == StateStatus.OS_COMMITTED) {
                log.debugf("Wrote dummy transaction %n", txId);
            }

            AdvancedAtomicActionExpiryScanner scanner = new AdvancedAtomicActionExpiryScanner();
            scanner.scan();

            if (recoveryStore.currentState(txId, TYPE_NAME + "/Expired") == StateStatus.OS_COMMITTED) {
                log.debug("Transaction log moved!");
                passed = true;
            } else {
                log.debug("Transaction log not moved!");
            }
        } catch (Exception ex) {
            log.warn("Exception while working on store catched", ex);
        }

        assertTrue(passed);
    }

    @Test
    public void testAdvancedAtomicActionPurgeExpiryScanner() {
        boolean passed = false;
        Uid txId = new Uid();
        Uid processUid = new Uid();

        try {
            OutputObjectState os = createObjectState(txId, processUid);

            log.debug("Creating dummy log");
            recoveryStore.write_committed(txId, TYPE_NAME, os);
            if (recoveryStore.currentState(txId, TYPE_NAME) == StateStatus.OS_COMMITTED) {
                log.debugf("Wrote dummy transaction %n", txId);
            }

            AdvancedAtomicActionPurgeExpiryScanner scanner = new AdvancedAtomicActionPurgeExpiryScanner();
            scanner.scan();

            if (recoveryStore.currentState(txId, TYPE_NAME) == StateStatus.OS_UNKNOWN
                    && recoveryStore.currentState(txId, TYPE_NAME + "/Expired") == StateStatus.OS_UNKNOWN) {
                log.debug("Transaction log purged!");
                passed = true;
            } else {
                log.debug("Transaction log not purged!");
            }
        } catch (Exception ex) {
            log.warn("Exception while working on store catched", ex);
        }

        assertTrue(passed);
    }
}
