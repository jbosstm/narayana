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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.ats.internal.jbossatx.jts.jca;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.transaction.jts.jca.TxImporter;
import com.arjuna.ats.internal.jta.transaction.jts.jca.WorkSynchronization;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;

import org.jboss.tm.JBossXATerminator;
import org.jboss.util.UnexpectedThrowable;

import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.ats.jta.TransactionManager;

import com.arjuna.ats.internal.jta.transaction.jts.jca.TxWorkManager;

/**
 * The implementation of JBossXATerminator using the JTS implementation of the
 * JTA.
 * 
 * @author mcl
 * 
 * @message com.arjuna.ats.jbossatx.jts.jca.inactive [message
 *          com.arjuna.ats.jbossatx.jts.jca.inactive] Transaction is inactive!
 * @message com.arjuna.ats.jbossatx.jts.jca.completing [message
 *          com.arjuna.ats.jbossatx.jts.jca.completion] Transaction is
 *          completing!
 * @message com.arjuna.ats.jbossatx.jts.jca.unknown [message
 *          com.arjuna.ats.jbossatx.jts.jca.unknown] Unexpected error!
 * @message com.arjuna.ats.jbossatx.jts.jca.unknownwork [message
 *          com.arjuna.ats.jbossatx.jts.jca.unknownwork] Work not registered!
 */

public class XATerminator extends XATerminatorImple implements
		JBossXATerminator
{

	/**
	 * Register the unit of work with the specified transaction. The
	 * thread-to-transaction association is not changed yet. Basically this
	 * operation only lets the transaction system know about the work and
	 * nothing else.
	 * 
	 * @param work
	 *            the work to associate with the transaction.
	 * @param xid
	 *            the transaction within which the work will be performed.
	 * @param timeout
	 *            the lifetime of the transaction.
	 * 
	 * @throws WorkCompletedException
	 *             thrown if the work cannot be associated with the transactin.
	 * 
	 * 
	 */
	
	public void registerWork (Work work, Xid xid, long timeout)
			throws WorkCompletedException
	{
		try
		{
			TransactionImple tx = TxImporter.importTransaction(xid, (int) timeout);

			switch (tx.getStatus())
			{
			case Status.STATUS_NO_TRANSACTION:
			case Status.STATUS_UNKNOWN:
				throw new WorkCompletedException(
						jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.inactive"),
						WorkException.TX_RECREATE_FAILED);
			case Status.STATUS_ACTIVE:
				break;
			default:
				throw new WorkCompletedException(
						jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.completing"),
						WorkException.TX_CONCURRENT_WORK_DISALLOWED);
			}

			TxWorkManager.addWork(work, tx);

			/*
			 * TODO currently means one synchronization per work item and that
			 * instance isn't removed when/if the work item is cancelled and
			 * another work item is later added.
			 * 
			 * Synchronizations are pretty lightweight and this add/remove/add
			 * scenario will hopefully not happen that much. So, we don't
			 * optimise for it at the moment. Re-evaluate if it does become an
			 * overhead.
			 */

			tx.registerSynchronization(new WorkSynchronization(tx));
		}
		catch (WorkCompletedException ex)
		{
			throw ex;
		}
		catch (XAException ex)
		{
			throw new WorkCompletedException(ex);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			throw new WorkCompletedException(
					jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.unknown"),
					WorkException.INTERNAL);
		}
	}

	/**
	 * Associate the current thread with the specified transaction. In JBoss
	 * 4.x, they assume that the work has already been registered, so we do
	 * likewise, i.e., we don't do a register if it hasn't, but we will throw an
	 * exception (which is more than JBoss does).
	 * 
	 * @param work the Work to start
	 * @param xid the transaction to associate with the current thread.
	 * 
	 * @throws WorkCompletedException thrown if there are any errors.
	 */
	
	public void startWork (Work work, Xid xid) throws WorkCompletedException
	{
		try
		{
			TransactionImple tx = TxImporter.importTransaction(xid);

			// JBoss doesn't seem to use the work parameter!

			if (!TxWorkManager.getWork(tx).equals(work))
			{
				throw new WorkCompletedException(jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.unknownwork"),
						WorkException.INTERNAL);
			}
			
			TransactionManager.transactionManager().resume(tx);
		}
		catch (XAException ex)
		{
			throw new WorkCompletedException(ex);
		}
		catch (InvalidTransactionException ex)
		{
			throw new WorkCompletedException(
					jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.inactive"),
					WorkException.TX_RECREATE_FAILED);
		}
		catch (SystemException ex)
		{
			throw new WorkCompletedException(
					jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jts.jca.unknown"),
					WorkException.INTERNAL);
		}
	}

	/**
	 * Disassociate the thread from the transaction and remove the
	 * work from the transaction pool of workers. This assumes that
	 * the invoking thread is the one doing the work.
	 * 
	 * @param work the Work unit to remove.
	 * @param xid the transaction to remove the work from.
	 */
	
	public void endWork (Work work, Xid xid)
	{
		try
		{
			TransactionImple tx = TxImporter.importTransaction(xid);

			TransactionManager.transactionManager().suspend();

			TxWorkManager.removeWork(work, tx);
		}
		catch (Exception ex)
		{
			throw new UnexpectedThrowable(ex);
		}
	}

	/**
	 * Remove the associated work from the transaction. Do not do
	 * any thread-to-transaction disassociation.
	 * 
	 * @param work the unit of work to remove.
	 * @param xid the transaction from which it should be disassociated.
	 */
	
	public void cancelWork (Work work, Xid xid)
	{
		try
		{
			TransactionImple tx = TxImporter.importTransaction(xid);

			TxWorkManager.removeWork(work, tx);
		}
		catch (Exception ex)
		{
			throw new UnexpectedThrowable(ex);
		}
	}
}
