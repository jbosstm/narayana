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
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;

import org.jboss.jbossts.star.logging.RESTATLogger;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.arjuna.FormatConstants;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.TransactionManager;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class InboundBridge implements XAResource, SerializableXAResourceDeserializer, Serializable {

    /**
     * Unique (well, hopefully) formatId so we can distinguish our own Xids.
     */
    public static final int XARESOURCE_FORMAT_ID = FormatConstants.RTS_BRIDGE_FORMAT_ID;

    private static final Logger LOG = Logger.getLogger(InboundBridge.class);

    /**
     * Identifier for the subordinate transaction.
     */
    private Xid xid;

    /**
     * URL of the REST transaction.
     */
    private String enlistmentUrl;


    /**
     * Empty constructor for serialisation.
     */
    public InboundBridge() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.InboundBridge");
        }
    }

    /**
     * Constructor creates new transaction and enlists himself to it.
     *
     * @param xid
     * @param enlistmentUrl
     */
    public InboundBridge(final Xid xid, final String enlistmentUrl) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.InboundBridge: xid=" + xid + ", enlistmentUrl=" + enlistmentUrl);
        }

        this.xid = xid;
        this.enlistmentUrl = enlistmentUrl;

        enlist(this);
    }

    public void start() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.start " + this);
        }

        final Transaction transaction = getTransaction();

        if (!isTransactionGoodToResume(transaction)) {
            throw new InboundBridgeException("Transaction is not in an active state.");
        }

        try {
            TransactionManager.transactionManager().resume(transaction);
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_failedToStartBridge(e.getMessage(), e);
            throw new InboundBridgeException("Failed to start the bridge.", e);
        }
    }

    public void stop() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.stop " + this);
        }

        try {
            TransactionManager.transactionManager().suspend();
        } catch (SystemException e) {
            RESTATLogger.atI18NLogger.warn_failedToStopBridge(e.getMessage(), e);
            throw new InboundBridgeException("Failed to stop the bridge.", e);
        }
    }

    public Xid getXid() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.getXid " + this);
        }

        return xid;
    }

    public void setXid(final Xid xid) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.setXid: xid=" + xid + ". " + this);
        }

        this.xid = xid;
    }

    public String getEnlistmentUrl() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.getEnlistmentUrl " + this);
        }

        return enlistmentUrl;
    }

    public void setEnlistmentUrl(final String enlistmentUrl) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.setEnlistmentUrl: enlistmentUrl=" + enlistmentUrl + ". " + this);
        }

        this.enlistmentUrl = enlistmentUrl;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(final Object o) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeImpl.equals: o=" + o + ". " + this);
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof InboundBridge)) {
            return false;
        }

        InboundBridge inboundBridge = (InboundBridge) o;

        return this.xid.equals(inboundBridge.xid) && enlistmentUrl.equals(inboundBridge.enlistmentUrl);
    }

    @Override
    public int hashCode() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridgeImpl.hashCode " + this);
        }

        int hash = 1;

        hash = hash * 17 * xid.hashCode();
        hash = hash * 31 * enlistmentUrl.hashCode();

        return hash;
    }

    @Override
    public String toString() {
        return "<InboundBridge: xid=" + xid + ", enlistmentUrl=" + enlistmentUrl + ">";
    }

    private void enlist(final InboundBridge inboundBridge) {
        final Transaction transaction = getTransaction();

        if (!isTransactionGoodToEnlist(transaction)) {
            throw new InboundBridgeException("Transaction is not in an active state.");
        }

        try {
            transaction.enlistResource(inboundBridge);
        } catch (Exception e) {
            RESTATLogger.atI18NLogger.warn_failedToEnlistTransaction(e.getMessage(), e);
            throw new InboundBridgeException("Failed to enlist inbound bridge to the transaction.", e);
        }
    }

    /**
     * Get the JTA subordinate transaction with current XID.
     *
     * @return
     */
    private Transaction getTransaction() {
        final Transaction transaction;

        try {
            transaction = SubordinationManager.getTransactionImporter()
                    .importTransaction(xid, TxControl.getDefaultTimeout());
        } catch (XAException e) {
            RESTATLogger.atI18NLogger.warn_failedToImportTransaction(e.getMessage(), e);
            throw new InboundBridgeException("Failed to import transaction.", e);
        }

        return transaction;
    }

    private boolean isTransactionGoodToEnlist(final Transaction transaction) {
        try {
            return transaction.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            RESTATLogger.atI18NLogger.error_systemException(e.getMessage());
            return false;
        }
    }

    private boolean isTransactionGoodToResume(final Transaction transaction) {
        try {
            final int status = transaction.getStatus();

            return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK
                    || status == Status.STATUS_COMMITTING;
        } catch (SystemException e) {
            RESTATLogger.atI18NLogger.error_systemException(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean canDeserialze(final String className) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.canDeserialze");
        }

        return getClass().getName().equals(className);
    }

    @Override
    public XAResource deserialze(final ObjectInputStream ois) throws IOException, ClassNotFoundException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.deserialze");
        }

        final InboundBridge inboundBridge = (InboundBridge) ois.readObject();
        InboundBridgeRecoveryModule.addRecoveredBridge(inboundBridge);

        return inboundBridge;
    }

    /**
     * Following methods are not really used. They are required because InboundBridge has to implement XAResource interface for
     * recovery.
     */

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.commit: xid=" + xid + ", b=" + b);
        }
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.end: xid=" + xid + ", i=" + i);
        }
    }

    @Override
    public void forget(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.forget: xid=" + xid);
        }
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.getTransactionTimeout");
        }

        return 0;
    }

    @Override
    public boolean isSameRM(XAResource resource) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.isSameRM: resource=" + resource);
        }

        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.prepare: xid=" + xid);
        }

        return XAResource.XA_OK;
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.recover: i=" + i);
        }

        return new Xid[0];
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.rollback: xid=" + xid);
        }
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.setTransactionTimeout: i=" + i);
        }

        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("InboundBridge.start: xid=" + xid + ", i=" + i);
        }
    }

}