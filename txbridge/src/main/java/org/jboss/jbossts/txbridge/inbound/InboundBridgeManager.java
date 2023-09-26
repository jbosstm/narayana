/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.UserTransactionFactory;

import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wsc.AlreadyRegisteredException;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

/**
 * Maintains the mapping data that relates WS-AT transactions to JTA subordinate transactions and related objects.
 *
 * The mappings are scoped to the singleton instance of this class and its lifetime.
 * This poses problems where you have more than one instance (classloading, clusters)
 * or where you need crash recovery. It short, it's rather limited.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class InboundBridgeManager
{
    // maps WS-AT Tx Id to InboundBridge instance.
    private static final ConcurrentMap<String, InboundBridge> inboundBridgeMappings = new ConcurrentHashMap<String, org.jboss.jbossts.txbridge.inbound.InboundBridge>();

    private static final Set<Xid> liveXids = new HashSet<>();

    /**
     * Return an InboundBridge instance that maps the current Thread's WS transaction context
     * to a JTA context. Control of the latter is provided by the returned instance.
     *
     * @return an InboundBridge corresponding to the calling Thread's current WS-AT transaction context.
     * @throws WrongStateException
     * @throws UnknownTransactionException
     * @throws com.arjuna.wst.SystemException
     * @throws AlreadyRegisteredException
     */
    public static InboundBridge getInboundBridge()
            throws XAException, WrongStateException, UnknownTransactionException,
            com.arjuna.wst.SystemException, jakarta.transaction.SystemException, AlreadyRegisteredException
    {
        txbridgeLogger.logger.trace("InboundBridgeManager.getInboundBridge()");

        UserTransaction wsUserTransaction = UserTransactionFactory.userTransaction();
        String externalTxId = wsUserTransaction.toString();
        int wsTxnTimeout = wsUserTransaction.getTimeout();

        if(!inboundBridgeMappings.containsKey(externalTxId)) {
            createMapping(externalTxId, wsTxnTimeout);
        }

        return inboundBridgeMappings.get(externalTxId);
    }

    /**
     * Return an InboundBridge instance for the specified WS-AT transaction context.
     *
     * This method exists only to allow BridgeVolatileParticipant to get tx context and may go away
     * once its no longer needed for that purpose. Therefore it should probably not be relied upon.
     *
     * @deprecated
     * @param externalTxId The WS-AT tx identifier.
     * @return
     */
    static InboundBridge getInboundBridge(String externalTxId)
    {
        return inboundBridgeMappings.get(externalTxId);
    }

    /**
     * Remove the mapping for the given externalTxId. This should be called for gc when the tx is finished.
     *
     * @param externalTxId The WS-AT tx identifier.
     */
    public static synchronized void removeMapping(String externalTxId)
    {
        txbridgeLogger.logger.trace("InboundBridgeManager.removeMapping(externalTxId="+externalTxId+")");

        if(externalTxId != null) {
            InboundBridge inboundBridge = inboundBridgeMappings.remove(externalTxId);
            if(inboundBridge != null) {
                liveXids.remove(inboundBridge.getXid());
            }
        }
    }

    public static synchronized boolean isLive(Xid xid) {
        return liveXids.contains(xid);
    }

    /**
     * Create the JTA transaction mapping and support objects for a given WS transaction context.
     *
     * @param externalTxId The WS-AT tx identifier.
     * @throws WrongStateException
     * @throws UnknownTransactionException
     * @throws com.arjuna.wst.SystemException
     * @throws AlreadyRegisteredException
     */
    private static synchronized void createMapping(String externalTxId, int timeout)
            throws XAException, WrongStateException, UnknownTransactionException,
            com.arjuna.wst.SystemException, jakarta.transaction.SystemException, AlreadyRegisteredException
    {
        txbridgeLogger.logger.trace("InboundBridgeManager.createMapping(externalTxId="+externalTxId+")");

        if(inboundBridgeMappings.containsKey(externalTxId)) {
            return;
        }

        TransactionManager transactionManager = TransactionManagerFactory.transactionManager();

        // Xid for driving the subordinate,
        // shared by the bridge (thread assoc) and Participant (termination via XATerminator)
        Xid xid = XATxConverter.getXid(new Uid(), false, BridgeDurableParticipant.XARESOURCE_FORMAT_ID);

        BridgeDurableParticipant bridgeDurableParticipant = new BridgeDurableParticipant(externalTxId, xid);

        // construct the participantId in such as way as we can recognise it at recovery time:
        String participantId = org.jboss.jbossts.txbridge.inbound.BridgeDurableParticipant.TYPE_IDENTIFIER+new Uid().toString();
        transactionManager.enlistForDurableTwoPhase(bridgeDurableParticipant, participantId);

        BridgeVolatileParticipant bridgeVolatileParticipant = new BridgeVolatileParticipant(externalTxId, xid);
        transactionManager.enlistForVolatileTwoPhase(bridgeVolatileParticipant, new Uid().toString());

        InboundBridge inboundBridge = new InboundBridge(xid, timeout);
        inboundBridgeMappings.put(externalTxId, inboundBridge);
        liveXids.add(inboundBridge.getXid());
    }
}