/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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