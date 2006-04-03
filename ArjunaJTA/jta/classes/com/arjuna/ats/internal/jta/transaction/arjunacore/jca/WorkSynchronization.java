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
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WorkSynchronization.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;

/**
 * Register a single instance of this thing when the first JCA worker is
 * imported. Not before.
 * 
 * It needs to tidy-up any Worker-to-transaction associations and to tear down
 * any other transaction-specific data that we may be holding (e.g., TxWorkers).
 * 
 * @author mcl 
 */

public class WorkSynchronization implements javax.transaction.Synchronization
{

	public WorkSynchronization (TransactionImple current)
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
	
	private TransactionImple _current;

}
