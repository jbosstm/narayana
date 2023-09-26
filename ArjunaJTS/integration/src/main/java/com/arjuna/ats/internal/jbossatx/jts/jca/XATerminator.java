/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jbossatx.jts.jca;

import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkCompletedException;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.transaction.jts.jca.WorkSynchronization;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;

import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.ats.jta.TransactionManager;

import com.arjuna.ats.internal.jta.transaction.jts.jca.TxWorkManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import org.jboss.tm.JBossXATerminator;

/**
 * The implementation of JBossXATerminator using the JTS implementation of the
 * JTA.
 *
 * @author mcl
 */

public class XATerminator extends XATerminatorImple implements JBossXATerminator
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
			/*
			 * Remember to convert timeout to seconds.
			 */

			Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid, (int) timeout/1000);

			switch (tx.getStatus())
			{
			case Status.STATUS_NO_TRANSACTION:
			case Status.STATUS_UNKNOWN:
				throw new WorkCompletedException(
                        jbossatxLogger.i18NLogger.get_jts_jca_inactive(),
                        WorkException.TX_RECREATE_FAILED);
			case Status.STATUS_ACTIVE:
				break;
			default:
				throw new WorkCompletedException(
                        jbossatxLogger.i18NLogger.get_jts_jca_completing(),
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
                    jbossatxLogger.i18NLogger.get_jts_jca_unknown(),
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
			Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid);

			// JBoss doesn't seem to use the work parameter!

			if (!TxWorkManager.getWork(tx).equals(work))
			{
				throw new WorkCompletedException(jbossatxLogger.i18NLogger.get_jts_jca_unknownwork(),
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
                    jbossatxLogger.i18NLogger.get_jts_jca_inactive(),
                    WorkException.TX_RECREATE_FAILED);
		}
		catch (SystemException ex)
		{
			throw new WorkCompletedException(
                    jbossatxLogger.i18NLogger.get_jts_jca_unknown(),
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
			Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid);

			TransactionManager.transactionManager().suspend();

			TxWorkManager.removeWork(work, tx);
		}
		catch (XAException xaException)
        {
            throw new RuntimeException(xaException);
        }
        catch (SystemException systemException)
        {
            throw new RuntimeException(systemException);
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
			Transaction tx = SubordinationManager.getTransactionImporter().importTransaction(xid);

			TxWorkManager.removeWork(work, tx);
		}
		catch (XAException xaException)
        {
            throw new RuntimeException(xaException);
        }
	}
}