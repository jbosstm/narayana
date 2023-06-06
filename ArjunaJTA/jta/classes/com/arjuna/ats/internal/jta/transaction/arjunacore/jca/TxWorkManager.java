/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkCompletedException;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.Transaction;

import com.arjuna.ats.jta.logging.jtaLogger;

public class TxWorkManager
{

	/*
	 * Although we allow multiple units of work per transaction, currently
	 * JCA only allows one. Might not be worth the hassle of maintaing this
	 * support.
	 */
	
	/**
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

			/*
			 * Stack is not required due to JCA 15.4.4 which restricts to one unit of work per TX.
			 */
			
			if (workers == null)
			{
				workers = new Stack<Work>();
				
				_transactions.put(tx, workers);
			}
			else
				throw new WorkCompletedException(jtaLogger.i18NLogger.get_transaction_arjunacore_jca_busy(), WorkException.TX_CONCURRENT_WORK_DISALLOWED);
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
				// TODO what if the work wasn't associated?
				
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