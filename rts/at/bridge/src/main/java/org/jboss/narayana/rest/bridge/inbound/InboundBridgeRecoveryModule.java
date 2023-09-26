/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound;

import java.util.HashSet;
import java.util.Set;

import jakarta.resource.spi.XATerminator;
import javax.transaction.xa.XAException;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class InboundBridgeRecoveryModule implements RecoveryModule {

    /**
     * Recovered instances of inbound bridge. After second pass every bridge with active REST-AT are passed to inbound bridge
     * manager.
     */
    private static final Set<InboundBridge> recoveredBridges = new HashSet<InboundBridge>();

    private static final Logger LOG = Logger.getLogger(InboundBridgeRecoveryModule.class);

    /**
     * UIDs found in transaction log after first pass.
     */
    private Set<Uid> firstPassUids;

    /**
     * Adds recovered bridge to recovered bridges map. This method is called by InboundBridge.readObject method during recovery.
     *
     * @param bridge
     */
    public static void addRecoveredBridge(InboundBridge bridge) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeRecoveryModule.addRecoveredBridge: " + bridge);
        }

        recoveredBridges.add(bridge);
    }

    /**
     * Called by the RecoveryManager at start up, and then PERIODIC_RECOVERY_PERIOD seconds after the completion, for all
     * RecoveryModules, of the second pass
     */
    @Override
    public void periodicWorkFirstPass() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeRecoveryModule.periodicWorkFirstPass");
        }

        firstPassUids = getUidsToRecover();
    }

    /**
     * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds after the completion of the first pass
     */
    @Override
    public void periodicWorkSecondPass() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeRecoveryModule.periodicWorkSecondPass");
        }

        recoveredBridges.clear();

        final Set<Uid> uids = getUidsToRecover();

        uids.retainAll(firstPassUids);

        for (Uid uid : uids) {
            try {
                final SubordinateTransaction st = SubordinationManager.getTransactionImporter().recoverTransaction(uid);
            } catch (XAException e) {
                RESTATLogger.atI18NLogger.warn_inboundBridgeRecoveryModulePeriodicWorkSecondPass(e.getMessage(), e);
            }
        }

        addBridgesToMapping();
    }

    /**
     * Returns UIDs of JTA subordinate transactions with format id specified in inbound bridge class which were found in
     * transaction log.
     *
     * @return Set<Uid>
     */
    private Set<Uid> getUidsToRecover() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeRecoveryModule.getUidsToRecover");
        }

        final Set<Uid> uids = new HashSet<Uid>();

        try {
            final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            final InputObjectState states = new InputObjectState();

            // Only look in the JCA section of the object store
            if (recoveryStore.allObjUids(SubordinateAtomicAction.getType(), states) && states.notempty()) {
                boolean finished = false;

                do {
                    final Uid uid = UidHelper.unpackFrom(states);

                    if (uid.notEquals(Uid.nullUid())) {
                        final SubordinateAtomicAction saa = new SubordinateAtomicAction(uid, true);
                        if (saa.getXid().getFormatId() == InboundBridge.XARESOURCE_FORMAT_ID) {
                            uids.add(uid);
                        }

                    } else {
                        finished = true;
                    }

                } while (!finished);
            }
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_InboundBridgeRecoveryModulegetUidsToRecover(e.getMessage(), e);
        }

        return uids;
    }

    /**
     * Adds bridges with active REST-AT to inbound bridge manager's mapping.
     * Rollback subordinate transaction, if bridge cannot be added.
     */
    private void addBridgesToMapping() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeRecoveryModule.addBridgesToMapping");
        }

        final InboundBridgeManager inboundBridgeManager = InboundBridgeManager.getInstance();

        for (final InboundBridge bridge : recoveredBridges) {
            if (!inboundBridgeManager.addInboundBridge(bridge)) {
                final XATerminator xaTerminator = SubordinationManager.getXATerminator();

                try {
                    xaTerminator.rollback(bridge.getXid());
                } catch (XAException e) {
                    RESTATLogger.atI18NLogger.warn_inboundBridgeRecoveryModuleAddBridgesToMapping(e.getMessage(), e);
                }
            }
        }
    }

}