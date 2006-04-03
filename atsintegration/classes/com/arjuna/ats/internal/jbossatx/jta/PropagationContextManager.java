/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropagationContextManager.java,v 1.4 2005/06/24 15:22:23 kconner Exp $
 */

package com.arjuna.ats.internal.jbossatx.jta;

import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.logging.Logger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

import javax.transaction.Transaction;
import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;

import java.util.Hashtable;
import java.io.Serializable;

import com.arjuna.ats.jta.TransactionManager;

public class PropagationContextManager
		implements TransactionPropagationContextFactory, TransactionPropagationContextImporter, ObjectFactory, Serializable
{
	private Logger log = org.jboss.logging.Logger.getLogger(PropagationContextManager.class);

	/**
	 *  Return a transaction propagation context for the transaction
	 *  currently associated with the invoking thread, or <code>null</code>
	 *  if the invoking thread is not associated with a transaction.
	 */

	public Object getTransactionPropagationContext()
	{
		if (log.isDebugEnabled())
		{
			log.debug("PropagationContextManager.getTransactionPropagationContext - called");
		}

		String threadId = Thread.currentThread().getName();

		String txid = ((BasicAction.Current() == null) ? null : BasicAction.Current().get_uid().stringForm());

		return txid;
	}

	/**
	 *  Return a transaction propagation context for the transaction
	 *  given as an argument, or <code>null</code>
	 *  if the argument is <code>null</code> or of a type unknown to
	 *  this factory.
	 */

	public Object getTransactionPropagationContext(Transaction tx)
	{
		if (log.isDebugEnabled())
		{
			log.debug("PropagationContextManager.getTransactionPropagationContext(Transaction) - called tx = " + tx);
		}

		if (tx instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple)
		    return ((com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple) tx).get_uid().stringForm();
		else
		    return null;
	}

	/**
	 *  Import the transaction propagation context into the transaction
	 *  manager, and return the resulting transaction.
	 *  If this transaction propagation context has already been imported
	 *  into the transaction manager, this method simply returns the
	 *  <code>Transaction</code> representing the transaction propagation
	 *  context in the local VM.
	 *  Returns <code>null</code> if the transaction propagation context is
	 *  <code>null</code>, or if it represents a <code>null</code> transaction.
	 */

	public Transaction importTransactionPropagationContext(Object tpc)
	{
		if (log.isDebugEnabled())
		{
			log.debug("PropagationContextManager.importTransactionPropagationContext(Object) - called tpc = " + tpc);
		}

		javax.transaction.TransactionManager tm = TransactionManager.transactionManager();

		if (tpc instanceof String)
		{
		    try
		    {
			Uid importedTx = new Uid((String)tpc);

			Transaction newTx = com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple.getTransaction(importedTx);

			if (log.isDebugEnabled())
			{
			    log.debug("PropagationContextManager.importTransactionPropagationContext(Object) - transaction = " + newTx);
			}

			return newTx;
		    }
		    catch (Exception e)
		    {
			log.error("Unexpected exception occurred", e);
			return null;
		    }
		}
		else
		{
		    log.error("jboss-atx: unknown Tx PropagationContext");
		    return null;
		}
	}

	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
					Hashtable environment) throws Exception
	{
		return new PropagationContextManager();
	}
}


