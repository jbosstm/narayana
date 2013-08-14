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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.xa.XATxConverter;
import org.jboss.logging.Logger;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;

import javax.transaction.xa.Xid;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class InboundBridgeManager {

    private static InboundBridgeManager INSTANCE;

    private static final String APPLICATION_ID = "org.jboss.narayana.rest.bridge.inbound.InboundBridgeManager:application_id";

    private static final Logger LOG = Logger.getLogger(InboundBridgeManager.class);

    private final Map<Xid, InboundBridge> inboundBridges;

    private final Map<String, Xid> transactionXids;

    public static InboundBridgeManager getInstance() {
        if (INSTANCE == null) {
            synchronized (InboundBridgeManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InboundBridgeManager();
                }
            }
        }

        return INSTANCE;
    }

    private InboundBridgeManager() {
        inboundBridges = new ConcurrentHashMap<Xid, InboundBridge>();
        transactionXids = new ConcurrentHashMap<String, Xid>();

        ParticipantsManagerFactory.getInstance().registerDeserializer(APPLICATION_ID, new InboundBridgeParticipantDeserializer());
    }

    public synchronized boolean addInboundBridge(final InboundBridge inboundBridge) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeManager.addInboundBridge: inboundBridge=" + inboundBridge);
        }

        final InboundBridge existingInboundBridge = inboundBridges.get(inboundBridge.getXid());

        if (inboundBridge.equals(existingInboundBridge)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("InboundBridgeManager.addInboundBridge: " + inboundBridge + " was added before");
            }

            // This bridge was added before.
            return true;
        }

        if (existingInboundBridge != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("InboundBridgeManager.addInboundBridge: another bridge uses this xid " + existingInboundBridge);
            }

            // Another bridge has the same Xid.
            return false;
        }

        inboundBridges.put(inboundBridge.getXid(), inboundBridge);
        transactionXids.put(inboundBridge.getEnlistmentUrl(), inboundBridge.getXid());

        return true;
    }

    public InboundBridge getInboundBridge(final Xid xid) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeManager.getInboundBridge: xid=" + xid);
        }

        return inboundBridges.get(xid);
    }

    public synchronized InboundBridge createInboundBridge(final String enlistmentUrl) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeManager.createInboundBridge: enlistmentUrl=" + enlistmentUrl);
        }

        if (enlistmentUrl == null) {
            throw new IllegalArgumentException("Enlistment URL is required");
        }

        if (!transactionXids.containsKey(enlistmentUrl)) {
            createAndEnlist(enlistmentUrl);
        }

        final Xid xid = transactionXids.get(enlistmentUrl);

        return inboundBridges.get(xid);
    }

    public void removeInboundBridge(final Xid xid) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeManager.removeInboundBridge: xid=" + xid);
        }

        final InboundBridge inboundBridge = inboundBridges.get(xid);

        if (inboundBridge != null) {
            transactionXids.remove(inboundBridge.getEnlistmentUrl());
            inboundBridges.remove(xid);
        }
    }

    private void createAndEnlist(final String enlistmentUrl) {
        final InboundBridge inboundBridge = getNewInboundBridge(enlistmentUrl);
        final InboundBridgeParticipant inboundBridgeParticipant = new InboundBridgeParticipant(inboundBridge.getXid());

        final String participantId = ParticipantsManagerFactory.getInstance().enlist(APPLICATION_ID, enlistmentUrl,
                inboundBridgeParticipant);

        if (participantId == null) {
            throw new InboundBridgeException("Participant was not enlisted.");
        }

        transactionXids.put(enlistmentUrl, inboundBridge.getXid());
        inboundBridges.put(inboundBridge.getXid(), inboundBridge);

        if (LOG.isTraceEnabled()) {
            LOG.trace("New inbound bridge enlisted: " + inboundBridge);
        }
    }

    private InboundBridge getNewInboundBridge(final String enlistmentUrl) {
        final Xid xid = XATxConverter.getXid(new Uid(), false, InboundBridge.XARESOURCE_FORMAT_ID);
        final InboundBridge inboundBridge = new InboundBridge(xid, enlistmentUrl);

        return inboundBridge;
    }

}
