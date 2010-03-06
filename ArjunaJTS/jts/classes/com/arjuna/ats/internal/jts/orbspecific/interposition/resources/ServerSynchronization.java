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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerSynchronization.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources;

import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;

public class ServerSynchronization extends
		org.omg.CosTransactions.SynchronizationPOA
{

	public ServerSynchronization(ServerTransaction topLevel)
	{
		ORBManager.getPOA().objectIsReady(this);

		_theTransaction = topLevel;
		_theSynchronization = org.omg.CosTransactions.SynchronizationHelper
				.narrow(ORBManager.getPOA().corbaReference(this));
	}

	public final Synchronization getSynchronization()
	{
		return _theSynchronization;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.interposition.resources.destroyfailed
	 *          Failed to destroy server-side synchronization object!
	 */

	public void destroy()
	{
		try
		{
			ORBManager.getPOA().shutdownObject(this);
		}
		catch (Exception e)
		{
			if (jtsLogger.logger.isWarnEnabled())
			{
				jtsLogger.loggerI18N
						.warn("com.arjuna.ats.internal.jts.orbspecific.interposition.resources.destroyfailed");
			}
		}
	}

	public void before_completion() throws SystemException
	{
		if (_theTransaction == null)
			throw new BAD_OPERATION(ExceptionCodes.NO_TRANSACTION,
					CompletionStatus.COMPLETED_NO);
		else
		{
			_theTransaction.doBeforeCompletion();
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.interposition.resources.stateerror
	 *          {0} status of transaction is different from our status: <{1},
	 *          {2}>
	 */

	public void after_completion(org.omg.CosTransactions.Status status)
			throws SystemException
	{
		if (_theTransaction == null)
		{
			destroy();

			throw new BAD_OPERATION(ExceptionCodes.NO_TRANSACTION,
					CompletionStatus.COMPLETED_NO);
		}
		else
		{
			/*
			 * Check that the given status is the same as our status. It should
			 * be!
			 */

			org.omg.CosTransactions.Status myStatus = org.omg.CosTransactions.Status.StatusUnknown;

			try
			{
				myStatus = _theTransaction.get_status();
			}
			catch (Exception e)
			{
				myStatus = org.omg.CosTransactions.Status.StatusUnknown;
			}

			if (myStatus != status)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N
							.warn(
									"com.arjuna.ats.internal.jts.orbspecific.interposition.resources.stateerror",
									new Object[]
									{ "ServerSynchronization.after_completion",
											com.arjuna.ats.jts.utils.Utility.stringStatus(myStatus), com.arjuna.ats.jts.utils.Utility.stringStatus(status) });
				}

				/*
				 * There's nothing much we can do, since the transaction should
				 * have completed. The best we can hope for it to try to
				 * rollback our portion of the transaction, but this may result
				 * in heuristics (which may not be reported to the coordinator,
				 * since exceptions from after_completion can be ignored in the
				 * spec.)
				 */

				if (myStatus == org.omg.CosTransactions.Status.StatusActive)
				{
					try
					{
						_theTransaction.rollback();
					}
					catch (Exception e)
					{
					}

					/*
					 * Get the local status to pass to our local
					 * synchronizations.
					 */

					try
					{
						status = _theTransaction.get_status();
					}
					catch (Exception e)
					{
						status = org.omg.CosTransactions.Status.StatusUnknown;
					}
				}
			}

			_theTransaction.doAfterCompletion(status);
		}

		/*
		 * Now dispose of self.
		 */

		destroy();
	}

	private ServerTransaction _theTransaction;

	private Synchronization _theSynchronization;

}
