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

import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.Aborted;
import org.jboss.narayana.rest.integration.api.HeuristicException;
import org.jboss.narayana.rest.integration.api.HeuristicType;
import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantException;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.ReadOnly;
import org.jboss.narayana.rest.integration.api.Vote;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.Serializable;


/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class InboundBridgeParticipant implements Participant, Serializable {

    private static final Logger LOG = Logger.getLogger(InboundBridgeParticipant.class);

    private final Xid xid;

    public InboundBridgeParticipant(final Xid xid) {
        this.xid = xid;
    }

    @Override
    public Vote prepare() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.prepare: xid=" + xid);
        }

        startBridge();

        final Vote outcome = prepareSubordinate();

        if (!(outcome instanceof Prepared)) {
            cleanup();
        } else {
            stopBridge();
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.prepare: xid=" + xid + ", outcome=" + outcome.getClass().getName());
        }

        return outcome;
    }

    @Override
    public void commit() throws HeuristicException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.commit: xid=" + xid);
        }

        startBridge();

        try {
            commitSubordinate();
        } finally {
            cleanup();
        }
    }

    @Override
    public void commitOnePhase() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.commitOnePhase: xid=" + xid);
        }

        startBridge();

        final Vote outcome = prepareSubordinate();

        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.commitOnePhase: xid=" + xid + ", outcome=" + outcome);
        }

        if (outcome instanceof Prepared) {
            try {
                commitSubordinate();
            } catch (HeuristicException e) {
            }
        }

        cleanup();
    }

    @Override
    public void rollback() throws HeuristicException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeParticipant.rollback: xid=" + xid);
        }

        startBridge();

        try {
            rollbackSubordinate();
        } finally {
            cleanup();
        }
    }

    private void startBridge() {
        final InboundBridge inboundBridge = getInboundBridge();

        if (inboundBridge != null) {
            inboundBridge.start();
        } else {
            throw new ParticipantException("Inbound bridge is not available.");
        }
    }

    private void stopBridge() {
        final InboundBridge inboundBridge = getInboundBridge();

        if (inboundBridge != null) {
            try {
                inboundBridge.stop();
            } catch (Exception e) {
                RESTATLogger.atI18NLogger.warn_failedToStopBridge(e.getMessage(), e);
            }
        }
    }

    private Vote prepareSubordinate() {
        final InboundBridge inboundBridge = getInboundBridge();
        int prepareResult = -1;

        try {
            if (inboundBridge != null) {
                prepareResult = SubordinationManager.getXATerminator().prepare(inboundBridge.getXid());
            }
        } catch (XAException e) {
            RESTATLogger.atI18NLogger.warn_subordinateVoteXAException(e.getMessage(), e);
        }

        return prepareResultToVote(prepareResult);
    }

    private void commitSubordinate() throws HeuristicException {
        final InboundBridge inboundBridge = getInboundBridge();
        
        try {
            if (inboundBridge != null) {
                SubordinationManager.getXATerminator().commit(inboundBridge.getXid(), false);
            }
        } catch (XAException e) {
            RESTATLogger.atI18NLogger.warn_subordinateCommitXAException(e.getMessage(), e);

            switch (e.errorCode) {
                case XAException.XA_HEURCOM:
                    throw new HeuristicException(HeuristicType.HEURISTIC_COMMIT);
                case XAException.XA_HEURRB:
                    throw new HeuristicException(HeuristicType.HEURISTIC_ROLLBACK);
                case XAException.XA_HEURMIX:
                    throw new HeuristicException(HeuristicType.HEURISTIC_MIXED);
                case XAException.XA_HEURHAZ:
                    throw new HeuristicException(HeuristicType.HEURISTIC_MIXED);
            }
        }
    }

    private void rollbackSubordinate() throws HeuristicException {
        final InboundBridge inboundBridge = getInboundBridge();

        try {
            if (inboundBridge != null) {
                SubordinationManager.getXATerminator().rollback(inboundBridge.getXid());
            }
        } catch (XAException e) {
            RESTATLogger.atI18NLogger.warn_subordinateRollbackXAException(e.getMessage(), e);

            switch (e.errorCode) {
                case XAException.XA_HEURCOM:
                    throw new HeuristicException(HeuristicType.HEURISTIC_COMMIT);
                case XAException.XA_HEURRB:
                    throw new HeuristicException(HeuristicType.HEURISTIC_ROLLBACK);
                case XAException.XA_HEURMIX:
                    throw new HeuristicException(HeuristicType.HEURISTIC_MIXED);
                case XAException.XA_HEURHAZ:
                    throw new HeuristicException(HeuristicType.HEURISTIC_MIXED);
            }
        }
    }

    private void cleanup() {
        stopBridge();
        InboundBridgeManager.getInstance().removeInboundBridge(xid);
    }

    private InboundBridge getInboundBridge() {
        return InboundBridgeManager.getInstance().getInboundBridge(xid);
    }

    private Vote prepareResultToVote(final int prepareResult) {
        if (prepareResult == XAResource.XA_OK) {
            return new Prepared();
        } else if (prepareResult == XAResource.XA_RDONLY) {
            return new ReadOnly();
        } else {
            return new Aborted();
        }
    }

}	