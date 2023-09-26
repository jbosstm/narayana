/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.rest.bridge.inbound;

import java.io.IOException;

import javax.transaction.xa.Xid;


import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.logging.Logger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class InboundBridgeOrphanFilter implements XAResourceOrphanFilter {

    private static final Logger LOG = Logger.getLogger(InboundBridgeOrphanFilter.class);

    /**
     * Called by the XARecoveryModule for each in-doubt Xid.
     * Implementations should return
     *   Vote.ROLLBACK if they recognize the xid and believe it should be aborted.
     *   Vote.LEAVE_ALONE if they recognize the xid and do not want the XARecovery module to roll it back.
     *   Vote.ABSTAIN if they do not recognize the xid.
     * Each registered XAResourceOrphanFilter will be consulted before any rollback on each recovery pass,
     * so they may change their mind over time e.g. if new information becomes available due to other recovery
     * activity.
     *
     * @param xid The in-doubt xid.
     * @return a Vote in accordance with the guidelines above.
     */
    @Override
    public Vote checkXid(Xid xid) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeOrphanFilter.checkXid(" + xid + ")");
        }

        Vote vote = Vote.ROLLBACK;

        if (xid.getFormatId() != InboundBridge.XARESOURCE_FORMAT_ID) {
            vote = Vote.ABSTAIN;
        }

        if (isInStore(xid)) {
            vote = Vote.LEAVE_ALONE;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeOrphanFilter.checkXid: " + vote.name());
        }

        return vote;
    }

    private boolean isInStore(Xid xid) {
        final RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        final InputObjectState states = new InputObjectState();

        try {
            if (recoveryStore.allObjUids(SubordinateAtomicAction.getType(), states) && states.notempty()) {
                boolean finished = false;

                do {
                    final Uid uid = UidHelper.unpackFrom(states);

                    if (uid.notEquals(Uid.nullUid())) {
                        final SubordinateAtomicAction saa = new SubordinateAtomicAction(uid, true);
                        if (saa.getXid().equals(xid)) {
                            return true;
                        }
                    } else {
                        finished = true;
                    }
                } while (!finished);
            }
        } catch (ObjectStoreException e) {
            RESTATLogger.atI18NLogger.warn_loadInStoreInboundBridgeOrphanFilter(e);
        } catch (IOException e) {
            RESTATLogger.atI18NLogger.warn_isInStoreInboundBridgeOrphanFilter(e);
        }

        return false;
    }

}