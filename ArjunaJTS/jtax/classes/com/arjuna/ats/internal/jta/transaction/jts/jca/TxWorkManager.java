/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.ats.internal.jta.transaction.jts.jca;

import java.util.HashMap;
import java.util.Stack;
import java.util.Map;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.transaction.Transaction;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;

public class TxWorkManager
{

	/*
	 * Although we allow multiple units of work per transaction, currently
	 * JCA only allows one. Might not be worth the hassle of maintaining this
	 * support.
	 */

	/**
	 *
	 * Add the specified work unit to the specified transaction.
	 *
	 * @param work The work to associate with the transaction.
	 * @param tx The transaction to have associated with the work.
	 *
	 * @throws WorkCompletedException thrown if there is already work
	 * associated with the transaction.
	 */

	public static void addWork (Work work, Transaction tx) throws WorkCompletedException
	{
		Stack<Work> workers;

		synchronized (_transactions)
		{
			workers = _transactions.get(tx);

			if (workers == null)
			{
				workers = new Stack<Work>();

				_transactions.put(tx, workers);
			}
			else
				throw new WorkCompletedException(jtaxLogger.i18NLogger.get_jtax_transaction_jts_jca_busy(), WorkException.TX_CONCURRENT_WORK_DISALLOWED);
		}

		synchronized (workers)
		{
			workers.push(work);
		}
	}

	/**
	 * Remove the specified unit of work from the transaction.
	 *
	 * @param work the work to remove.
	 * @param tx the transaction the work should be disassociated from.
	 */

	public static void removeWork (Work work, Transaction tx)
	{
		Stack<Work> workers;

		synchronized (_transactions)
		{
			workers = _transactions.get(tx);
		}

		if (workers != null)
		{
			synchronized (workers)
			{
				// TODO what if it wasn't registered?

				workers.remove(work);

				if (workers.empty())
				{
					synchronized (_transactions)
					{
						_transactions.remove(tx);
					}
				}
			}
		}
		else
		{
			// shouldn't happen!
		}
	}

	/**
	 * Does the transaction have any work associated with it?
	 *
	 * @param tx the transaction to check.
	 *
	 * @return <code>true</code> if there is work associated with the transaction,
	 * <code>false</code> otherwise.
	 */

	public static boolean hasWork (Transaction tx)
	{
		synchronized (_transactions)
		{
			Stack workers = _transactions.get(tx);

			return (boolean) (workers != null);
		}
	}

	/**
	 * Get the work currently associated with the transaction.
	 *
	 * @param tx the transaction.
	 *
	 * @return the work, or <code>null</code> if there is none.
	 */

	public static Work getWork (Transaction tx)
	{
		Stack<Work> workers;

		synchronized (_transactions)
		{
			workers = _transactions.get(tx);
		}

		if (workers != null)
		{
			synchronized (workers)
			{
				if (!workers.empty())
					return workers.peek();
			}
		}

		return null;
	}

	private static final Map<Transaction, Stack<Work>> _transactions = new HashMap<Transaction, Stack<Work>>();

}
