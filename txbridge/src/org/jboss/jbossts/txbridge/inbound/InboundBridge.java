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
package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Transaction;

import org.apache.log4j.Logger;

/**
 * Manages Thread association of the interposed coordinator.
 * Typically called from handlers in the WS stack.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class InboundBridge
{
	private static final Logger log = Logger.getLogger(InboundBridge.class);

    /**
     * Identifier for the subordinate transaction.
     */
	private final Xid xid;

    /**
     * Create a new InboundBridge to manage the given subordinate JTA transaction.
     *
     * @param xid the subordinate transaction id
     * @throws XAException
     * @throws SystemException
     */
	InboundBridge(Xid xid) throws XAException, SystemException
    {
		log.trace("InboundBridge(Xid="+xid+")");

		this.xid = xid;

		getTransaction(); // ensures transaction is initialized
	}

	/**
	 * Associate the JTA transaction to the current Thread.
	 * Typically used by a server side inbound handler.
	 *
	 * @throws XAException
	 * @throws SystemException
	 * @throws InvalidTransactionException
	 */
	public void start() throws XAException, SystemException, InvalidTransactionException
    {
		log.trace("start(Xid="+xid+")");

		Transaction tx = getTransaction();

		TransactionManager.transactionManager().resume(tx);
	}

	/**
	 * Disassociate the JTA transaction from the current Thread.
	 * Typically used by a server side outbound handler.
	 *
	 * @throws XAException
	 * @throws SystemException
	 * @throws InvalidTransactionException
	 */
	public void stop() throws XAException, SystemException, InvalidTransactionException
    {
		log.trace("stop("+xid+")");

		TransactionManager.transactionManager().suspend();
	}

    public void setRollbackOnly() throws XAException, SystemException
    {
        log.trace("setRollbackOnly("+xid+")");

        getTransaction().setRollbackOnly();
    }

	/**
	 * Get the JTA Transaction which corresponds to the Xid of the instance.
	 *
	 * @return
	 * @throws XAException
	 * @throws SystemException
	 */
	private Transaction getTransaction()
			throws XAException, SystemException
	{
		Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid);

		switch (tx.getStatus())
		{
            // TODO: other cases?

			case Status.STATUS_ACTIVE:
            case Status.STATUS_MARKED_ROLLBACK:
				break;
			default:
				throw new IllegalStateException("Transaction not in state ACTIVE");
		}
		return tx;
	}
}
