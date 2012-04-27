/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

import javax.transaction.Transaction;
import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;

import java.util.Hashtable;
import java.io.Serializable;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;




public class PropagationContextManager
		implements TransactionPropagationContextFactory, TransactionPropagationContextImporter, ObjectFactory, Serializable
{
	/**
	 *  Return a transaction propagation context for the transaction
	 *  currently associated with the invoking thread, or <code>null</code>
	 *  if the invoking thread is not associated with a transaction.
	 */

	public Object getTransactionPropagationContext()
	{
		if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext - called");
        }

		String txid = ((BasicAction.Current() == null) ? null : BasicAction.Current().get_uid().stringForm());

        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext() - returned tpc = " + txid);
        }
		
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
		if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext(Transaction) - called tx = " + tx);
        }

		if (tx instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple)
		{
		    String tpc = ((com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple) tx).get_uid().stringForm();

            if (jbossatxLogger.logger.isTraceEnabled()) {
                jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext(Transaction) - returned tpc = " + tpc);
            }
		    
		    return tpc;
		}
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
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.importTransactionPropagationContext(Object) - called tpc = " + tpc);
        }

        javax.transaction.TransactionManager tm = TransactionManager.transactionManager();

        if (tpc instanceof String)
        {
            try
            {
                Uid importedTx = new Uid((String)tpc);

                Transaction newTx = com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple.getTransaction(importedTx);

                if (jbossatxLogger.logger.isTraceEnabled()) {
                    jbossatxLogger.logger.trace("PropagationContextManager.importTransactionPropagationContext(Object) - transaction = " + newTx);
                }

                return newTx;
            }
            catch (Exception e)
            {
                jbossatxLogger.i18NLogger.error_jta_PropagationContextManager_exception(e);
                return null;
            }
        }
        else
        {
            jbossatxLogger.i18NLogger.error_jta_PropagationContextManager_unknownctx();
            return null;
        }
	}

	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
					Hashtable environment) throws Exception
	{
		return new PropagationContextManager();
	}
}


