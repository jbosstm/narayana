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
 * (C) 2007, 2009 @author JBoss Inc
 */
package org.jboss.jbossts.txbridge;

import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst11.UserTransactionFactory;

import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wsc.AlreadyRegisteredException;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

/**
 * Maintains the mapping data that relates WS-AT transactions to JTA subordinate transactions and related objects.
 *
 * The mappings are scoped to the singleton instance of this class and its lifetime.
 * This poses problems where you have more than one instances (classloading, clusters)
 * or where you need crash recovery. It short, it's rather limited.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class InboundBridgeManager
{
	private static Logger log = Logger.getLogger(InboundBridgeManager.class);

    // maps WS-AT Tx Id to InboundBridge instance.
	private static ConcurrentMap<String, InboundBridge> inboundBridgeMappings = new ConcurrentHashMap<String, InboundBridge>();

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
			com.arjuna.wst.SystemException, javax.transaction.SystemException, AlreadyRegisteredException
	{
		log.trace("getInboundBridge()");

		String externalTxId = UserTransactionFactory.userTransaction().toString();

        if(!inboundBridgeMappings.containsKey(externalTxId)) {
            createMapping(externalTxId);
        }

		return inboundBridgeMappings.get(externalTxId);
	}

    /**
     * Remove the mapping for the given externalTxId. This should be called for gc when the tx is finished.
     *
     * @param externalTxId The WS-AT tx identifier.
     */
    public static synchronized void removeMapping(String externalTxId)
    {
        log.trace("removeMapping(externalTxId="+externalTxId+")");

        if(externalTxId != null) {
            inboundBridgeMappings.remove(externalTxId);
        }
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
	private static synchronized void createMapping(String externalTxId)
			throws XAException, WrongStateException, UnknownTransactionException,
			com.arjuna.wst.SystemException, javax.transaction.SystemException, AlreadyRegisteredException
    {
		log.trace("createMapping(externalTxId="+externalTxId+")");

		if(inboundBridgeMappings.containsKey(externalTxId)) {
			return;
		}

        // Xid for driving the subordinate,
        // shared by the bridge (thread assoc) and Participant (termination via XATerminator)
		Xid xid = new XidImple(new Uid());

		BridgeParticipantAT bridgeParticipantAT = new BridgeParticipantAT(externalTxId, xid);

        // construct the participantId in such as way as we can recognise it at recovery time:
        String participantId = BridgeParticipantAT.TYPE_IDENTIFIER+new Uid().toString();
		TransactionManagerFactory.transactionManager().enlistForDurableTwoPhase(bridgeParticipantAT, participantId);

        inboundBridgeMappings.put(externalTxId, new InboundBridge(xid));
	}
}