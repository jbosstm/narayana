/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 *
 * (C) 2009 @author Red Hat Middleware LLC
 */
package org.jboss.jbossts.txbridge.outbound;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.arjuna.common.Uid;

import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains the mapping data that relates JTA transactions to WS-AT subordinate transactions and related objects.
 *
 * The mappings are scoped to the singleton instance of this class and its lifetime.
 * This poses problems where you have more than one instance (classloading, clusters)
 * or where you need crash recovery. It short, it's rather limited.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class OutboundBridgeManager
{
    public static String BRIDGEWRAPPER_PREFIX = "txbridge_";

    // maps JTA Tx Id to OutboundBridge instance.
    private static final ConcurrentMap<Uid, org.jboss.jbossts.txbridge.outbound.OutboundBridge> outboundBridgeMappings = new ConcurrentHashMap<Uid, org.jboss.jbossts.txbridge.outbound.OutboundBridge>();

    /**
     * Return an OutboundBridge instance that maps the current Thread's JTA transaction context
     * to a WS-AT transaction context. Control of the latter is provided by the returned instance.
     *
     * @return as OutboundBridge corresponding to the calling Thread's current JTA transaction context.
     */
    public static org.jboss.jbossts.txbridge.outbound.OutboundBridge getOutboundBridge()
    {
        txbridgeLogger.logger.trace("OutboundBridgeManager.getOutboundBridge()");

        try
        {
            Transaction transaction = (Transaction)TransactionManager.transactionManager().getTransaction();

            Uid externalTxId = transaction.get_uid();

            if(!outboundBridgeMappings.containsKey(externalTxId)) {
                createMapping(transaction, externalTxId);
            }

            return outboundBridgeMappings.get(externalTxId);

        }
        catch(SystemException e)
        {
            txbridgeLogger.logger.error(e);
        }

        return null;
    }

    /**
     * Remove the mapping for the given externalTxId. This should be called for gc when the tx is finished.
     *
     * @param externalTxId The JTA transaction identifier.
     */
    public static synchronized void removeMapping(Uid externalTxId)
    {
        txbridgeLogger.logger.trace("OutboundBridgeManager.removeMapping(externalTxId="+externalTxId+")");

        if(externalTxId != null) {
            outboundBridgeMappings.remove(externalTxId);
        }
    }

    /**
     * Create a WS-AT transaction mapping and support objects for a given JTA transaction context.
     *
     * @param externalTxId The JTA transaction identifier.
     * @throws SystemException
     */
    private static synchronized void createMapping(Transaction transaction, Uid externalTxId) throws SystemException
    {
        txbridgeLogger.logger.trace("OutboundBridgeManager.createmapping(externalTxId="+externalTxId+")");

        if(outboundBridgeMappings.containsKey(externalTxId)) {
            return;
        }

        // TODO: allow params to be configurable, or at least pass timeout down.
        BridgeWrapper bridgeWrapper = BridgeWrapper.create(BRIDGEWRAPPER_PREFIX, 0, false);

        org.jboss.jbossts.txbridge.outbound.OutboundBridge outboundBridge = new org.jboss.jbossts.txbridge.outbound.OutboundBridge(bridgeWrapper);
        XAResource xaResource = new org.jboss.jbossts.txbridge.outbound.BridgeXAResource(externalTxId, bridgeWrapper);
        Synchronization synchronization = new org.jboss.jbossts.txbridge.outbound.BridgeSynchronization(bridgeWrapper);

        try
        {
            transaction.enlistResource(xaResource);
            transaction.registerSynchronization(synchronization);
        }
        catch(RollbackException e)
        {
            txbridgeLogger.i18NLogger.error_obm_unabletoenlist(e);
            throw new SystemException(e.toString());
        }

        outboundBridgeMappings.put(externalTxId, outboundBridge);
    }
}