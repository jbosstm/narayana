/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import jakarta.transaction.Transaction;

/**
 * Register a single instance of this thing when the first JCA worker is
 * imported. Not before.
 * 
 * It needs to tidy-up any Worker-to-transaction associations and to tear down
 * any other transaction-specific data that we may be holding (e.g., TxWorkers).
 * 
 * @author mcl 
 */

public class WorkSynchronization implements jakarta.transaction.Synchronization
{

	public WorkSynchronization (Transaction current)
	{
		_current = current;
	}
	
	/**
	 * If the current transaction still has work associated with it, then we need to
	 * throw an exception. This will cause the current transaction to rollback.
	 */
	
	public void beforeCompletion ()
	{
		// check no work associated with transaction
		
		try
		{
			if (TxWorkManager.hasWork(_current))
			{
				/*
				 * JBoss way of doing things is broken: they
				 * throw IllegalStateException in an invalid manner
				 * (see JTA spec.) and don't force the transaction to
				 * rollback.
				 */  
				
				throw new IllegalStateException();
			}
		}
		catch (IllegalStateException ex)
		{
			throw ex;
		}
		finally
		{
			_current = null;
		}
	}

	/**
	 * A null-op.
	 */
	
	public void afterCompletion (int status)
	{
		// do nothing
	}
	
	private Transaction _current;

}