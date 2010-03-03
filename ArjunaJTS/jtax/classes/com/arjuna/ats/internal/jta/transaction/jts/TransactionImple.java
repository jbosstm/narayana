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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts;

import com.arjuna.ats.internal.jta.utils.*;
import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;
import com.arjuna.ats.internal.jta.resources.jts.CleanupSynchronization;
import com.arjuna.ats.internal.jta.resources.jts.LocalCleanupSynchronization;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.LastResourceRecord;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.SynchronizationImple;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.ManagedSynchronizationImple;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;
import com.arjuna.ats.jta.utils.XAHelper;

import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import com.arjuna.ats.jta.xa.*;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.arjuna.ats.jta.logging.*;

import com.arjuna.ats.internal.jta.xa.TxInfo;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ControlWrapper;

import com.arjuna.common.util.logging.*;

import javax.transaction.xa.*;

import com.arjuna.ats.arjuna.common.*;

import java.util.concurrent.ConcurrentHashMap;

import java.util.*;

import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.Status;

import java.lang.SecurityException;
import java.lang.IllegalStateException;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;

/**
 * An implementation of javax.transaction.Transaction.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 *
 * @message com.arjuna.ats.internal.jta.transaction.jts.xaerror
 *          [com.arjuna.ats.internal.jta.transaction.jts.xaerror] {0} caught XA
 *          exception: {1}
 * @message com.arjuna.ats.internal.jta.transaction.jts.inactivetx
 *          [com.arjuna.ats.internal.jta.transaction.jts.inactivetx] Transaction
 *          is not active.
 * @message com.arjuna.ats.internal.jta.transaction.jts.invalidtx2
 *          [com.arjuna.ats.internal.jta.transaction.jts.invalidtx2] Invalid
 *          transaction.
 * @message com.arjuna.ats.internal.jta.transaction.jts.nox
 *          [com.arjuna.ats.internal.jta.transaction.jts.notx] No such
 *          transaction.
 * @message com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx
 *          [com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx] The
 *          current transaction does not match this transaction!
 * @message com.arjuna.ats.internal.jta.transaction.jts.nullparam
 *          [com.arjuna.ats.internal.jta.transaction.jts.nullparam] paramater is
 *          null!
 * @message com.arjuna.ats.internal.jta.transaction.jts.illegalstate
 *          [com.arjuna.ats.internal.jta.transaction.jts.illegalstate] - illegal
 *          resource state:
 * @message com.arjuna.ats.internal.jta.transaction.jts.syncproblem
 *          [com.arjuna.ats.internal.jta.transaction.jts.syncproble] - cleanup
 *          synchronization failed to register:
 * @message com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface
 * 			[com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface] - failed
 *          to load Last Resource Optimisation Interface
 * @message com.arjuna.ats.internal.jta.transaction.jts.setrollback
 *                      [com.arjuna.ats.internal.jta.transaction.jts.setrollback] - setRollbackOnly called from:
 */

public class TransactionImple implements javax.transaction.Transaction,
		com.arjuna.ats.jta.transaction.Transaction
{

	public TransactionImple () throws SubtransactionsUnavailable
	{
		this(new AtomicTransaction());

		_theTransaction.begin();

		try
		{
			TwoPhaseCoordinator theTx = null;

			try
			{
				/*
				 * If this is an imported transaction and we have just performed
				 * interposition, then register a purely local Synchronization.
				 * This gets us over a performance issue with JacORB.
				 */

				theTx = (TwoPhaseCoordinator) BasicAction.Current();

				if (theTx != null)  // TM is local
					theTx.addSynchronization(new LocalCleanupSynchronization(this));
				else
					registerSynchronization(new CleanupSynchronization(this));
			}
			catch (ClassCastException ex)
			{
				/*
				 * Not a local/interposed transaction.
				 */

				registerSynchronization(new CleanupSynchronization(this));
			}
		}
		catch (Exception ex)
		{
			/*
			 * Could set rollback only, but let's take the memory leak hit for
			 * now.
			 */

			jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.syncproblem", ex);
		}
	}

	/**
	 * Overloads Object.equals()
	 */

	public boolean equals (Object obj)
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.OPERATORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.equals");
		}

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof TransactionImple)
		{
			if (_theTransaction != null)
			{
				return _theTransaction.equals(((TransactionImple) obj)._theTransaction);
			}
		}

		return false;
	}

	/**
	 * Return -1 if we fail.
	 */

	public int hashCode ()
	{
		if (_theTransaction != null)
			return _theTransaction.hashCode();
		else
			return -1;
	}

	public ControlWrapper getControlWrapper ()
	{
		if (_theTransaction != null)
			return _theTransaction.getControlWrapper();
		else
			return null;
	}

	/**
	 * The JTA specification is vague on whether the calling thread can have any
	 * transaction associated with it. It does say that it need not have the
	 * same transaction as this one. We could call suspend prior to making these
	 * calls, but for now we do nothing, and simply treat it like a Control.
	 */

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 */

	public void commit () throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.commit");
		}

		if (_theTransaction != null)
		{   
			/*
			 * Call end on any suspended resources. If this fails, then the
			 * transaction will be rolled back.
			 */

			try
			{
			    if ((getStatus() != Status.STATUS_ACTIVE) && (getStatus() != Status.STATUS_MARKED_ROLLBACK))
		                        throw new NoTransaction();
		             
				if (!endSuspendedRMs())
					_theTransaction.rollbackOnly();

				_theTransaction.end(true);
			}
			catch (org.omg.CosTransactions.WrongTransaction wt)
			{
                InactiveTransactionException inactiveTransactionException = new InactiveTransactionException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
                inactiveTransactionException.initCause(wt);
                throw inactiveTransactionException;
			}
			catch (org.omg.CosTransactions.NoTransaction e1)
			{
                IllegalStateException illegalStateException = new IllegalStateException(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
                illegalStateException.initCause(e1);
                throw illegalStateException;
			}
			catch (org.omg.CosTransactions.HeuristicMixed e2)
			{
                HeuristicMixedException heuristicMixedException = new javax.transaction.HeuristicMixedException();
                heuristicMixedException.initCause(e2);
                throw heuristicMixedException;
			}
			catch (org.omg.CosTransactions.HeuristicHazard e3)
			{
                HeuristicMixedException heuristicMixedException = new javax.transaction.HeuristicMixedException();
                heuristicMixedException.initCause(e3);
                throw heuristicMixedException;
			}
			catch (TRANSACTION_ROLLEDBACK e4)
			{
                RollbackException rollbackException = new RollbackException(e4.toString());
                rollbackException.initCause(e4);
                throw rollbackException;
			}
			catch (org.omg.CORBA.NO_PERMISSION e5)
			{
				throw new SecurityException(e5);
			}
			catch (INVALID_TRANSACTION e6)
			{
				InactiveTransactionException inactiveTransactionException = new InactiveTransactionException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
                inactiveTransactionException.initCause(e6);
                throw inactiveTransactionException;
			}
			catch (org.omg.CORBA.SystemException e7)
			{
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e7.toString());
                systemException.initCause(e7);
        		throw systemException;
            }
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1
	 *          [com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1]
	 *          Ending suspended RMs failed when rolling back the transaction!
	 * @message com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2
	 *          [com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2]
	 *          Ending suspended RMs failed when rolling back the transaction,
	 *          but transaction rolled back.
	 */

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.rollback");
		}

		boolean endSuspendedFailed = false;
		
		if (_theTransaction != null)
		{
			try
			{
		             if ((getStatus() != Status.STATUS_ACTIVE) && (getStatus() != Status.STATUS_MARKED_ROLLBACK))
		                        throw new NoTransaction();
		             
		                 /*
	                         * Call end on any suspended resources. If this fails, then there's
	                         * not a lot else we can do because the transaction is about to roll
	                         * back anyway!
	                         */
		             
		              endSuspendedFailed = !endSuspendedRMs();

	                        if (endSuspendedFailed)
	                        {
	                                if (jtaxLogger.loggerI18N.isWarnEnabled())
	                                {
	                                        jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1");
	                                }
	                        }
	                        
				_theTransaction.abort();
			}
			catch (org.omg.CosTransactions.WrongTransaction e1)
			{
                InactiveTransactionException inactiveTransactionException =new InactiveTransactionException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
                inactiveTransactionException.initCause(e1);
                throw inactiveTransactionException;
			}
			catch (org.omg.CORBA.NO_PERMISSION e2)
			{
				throw new SecurityException(e2);
			}
			catch (INVALID_TRANSACTION e3)
			{
                InactiveTransactionException inactiveTransactionException = new InactiveTransactionException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
                inactiveTransactionException.initCause(e3);
                throw inactiveTransactionException;
			}
			catch (NoTransaction e4)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"), e4);
			}
			catch (org.omg.CORBA.SystemException e5)
			{
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e5.toString());
                systemException.initCause(e5);
        		throw systemException;
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}

			if (endSuspendedFailed)
				throw new InvalidTerminationStateException (
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2"));
		}
		else
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	public void setRollbackOnly () throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
	    if (jtaxLogger.logger.isDebugEnabled())
	    {
	        jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.setRollbackOnly");
	    }

	    if (_theTransaction != null)
	    {
	        try
	        {
	            _theTransaction.rollbackOnly();
	            // keep a record of why we are rolling back i.e. who called us first, it's a useful debug aid.
	            if (_rollbackOnlyCallerStacktrace == null)
	            {
	                _rollbackOnlyCallerStacktrace = new Throwable(jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
	            }
	        }
	        catch (org.omg.CosTransactions.NoTransaction e3)
	        {
	            throw new IllegalStateException(
	                    jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"), e3);
	        }
	        catch (final INVALID_TRANSACTION ex)
	        {
	            switch (getStatus())
                    {
	            case Status.STATUS_ROLLEDBACK:
	            case Status.STATUS_ROLLING_BACK:
                        break;
	            case Status.STATUS_PREPARING:
	            case Status.STATUS_PREPARED:
                        throw new InvalidTerminationStateException(
                                jtaLogger.loggerI18N
                                .getString("com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate"));
                    default:
                        throw new InactiveTransactionException(
                                jtaLogger.loggerI18N
                                .getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
                    }
	        }
	        catch (org.omg.CORBA.SystemException e4)
	        {
	            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e4.toString());
	            systemException.initCause(e4);
	            throw systemException;
	        }
	    }
	    else
	        throw new IllegalStateException(
	                jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	public int getStatus () throws javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.getStatus");
		}

		int status = javax.transaction.Status.STATUS_NO_TRANSACTION;

		if (_theTransaction != null)
		{
			try
			{
				return StatusConverter.convert(_theTransaction.get_status());
			}
			catch (org.omg.CORBA.SystemException e2)
			{
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e2.toString());
                systemException.initCause(e2);
		        throw systemException;
			}
		}

		return status;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.syncerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.syncerror]
	 *          Synchronizations are not allowed!
	 */
	public void registerSynchronization (javax.transaction.Synchronization sync)
			throws javax.transaction.RollbackException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
        if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.registerSynchronization");
		}

		if (sync == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.registerSynchronization - "
							+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

        registerSynchronizationImple(new ManagedSynchronizationImple(sync));
	}

	// package-private method also for use by
	// TransactionSynchronizationRegistryImple

	void registerSynchronizationImple(SynchronizationImple synchronizationImple)
			throws javax.transaction.RollbackException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (_theTransaction != null)
		{
			try
			{
                _theTransaction.registerSynchronization(synchronizationImple.getSynchronization());
			}
			catch (TRANSACTION_ROLLEDBACK e2)
			{
                RollbackException rollbackException = new javax.transaction.RollbackException(e2.toString());
                rollbackException.initCause(e2);
                throw rollbackException;
			}
			catch (org.omg.CosTransactions.Inactive e3)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"), e3);
			}
			catch (org.omg.CosTransactions.SynchronizationUnavailable e4)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.syncerror"), e4);
			}
			catch (INVALID_TRANSACTION e5)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"), e5);
			}
			catch (org.omg.CORBA.SystemException e6)
			{
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e6.toString());
                systemException.initCause(e6);
		        throw systemException;
			}
		}
		else
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}


    public boolean enlistResource (XAResource xaRes) throws RollbackException,
			IllegalStateException, javax.transaction.SystemException
	{
		return enlistResource(xaRes, null);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.starterror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.starterror] {0}
	 *          returned XA error {1} for transaction {2}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.regerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.regerror] {0} could
	 *          not register transaction: {1}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.markedrollback
	 * 			[com.arjuna.ats.internal.jta.transaction.jts.markedrollback] Could not
	 * 			enlist resource because the transaction is marked for rollback.
	 */

	public boolean enlistResource (XAResource xaRes, Object[] params)
			throws RollbackException, IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.enlistResource ( "
					+ xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.enlistResource - "
							+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			throw new RollbackException("TransactionImple.enlistResource - "
										+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.markedrollback"));
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		default:
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
		}

		XAModifier theModifier = null;

		if (params != null)
		{
			if (params.length >= XAMODIFIER + 1)
			{
				if (params[XAMODIFIER] instanceof XAModifier)
				{
					theModifier = (XAModifier) params[XAMODIFIER];
				}
			}
		}

		try
		{
			/*
			 * For each transaction we maintain a list of resources registered
			 * with it. Each element on this list also contains a list of
			 * threads which have registered this resource, and what their XID
			 * was for that registration.
			 */

			TxInfo info = null;

			/*
			 * Have we seen this specific resource instance before? Do this
			 * trawl first before checking the RM instance later. Saves time.
			 */

			try
			{
				synchronized (this)
				{
					info = (TxInfo) _resources.get(xaRes);

					if (info == null)
					{
						/*
						 * Null info means it's not in the main resources list,
						 * but may be in the duplicates.
						 */

						info = (TxInfo) _duplicateResources.get(xaRes);
					}
				}

				if (info != null)
				{
					switch (info.getState())
					{
					case TxInfo.ASSOCIATION_SUSPENDED:
					{
						/*
						 * Have seen resource before, so do a resume. The
						 * Resource instance will still be registered with the
						 * transaction though.
						 */

						xaRes.start(info.xid(), XAResource.TMRESUME);

						info.setState(TxInfo.ASSOCIATED);

						synchronized (this)
						{
							_suspendCount--;
						}

						return true; // already registered resource with this
						// transaction!
					}
					case TxInfo.ASSOCIATED:
					{
						/*
						 * Already active on this transaction.
						 */

						return true;
					}
					case TxInfo.NOT_ASSOCIATED:
					{
						/*
						 * Resource was associated, but was presumably delisted.
						 */
						xaRes.start(info.xid(), XAResource.TMJOIN);

						info.setState(TxInfo.ASSOCIATED);

						return true;
					}
					default:
					{
						// Note: this exception will be caught by our catch
						// block
						throw new IllegalStateException(
								"TransactionImple.enlistResource - "
										+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.illegalstate")
										+ info.getState());
					}
					}
				}
			}
			catch (IllegalStateException ex)
			{
				throw ex; // we threw it in the first place
			}
			catch (XAException exp)
			{
				if (info != null)
					info.setState(TxInfo.FAILED);

				if (jtaxLogger.loggerI18N.isWarnEnabled())
				{
					jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
					{ "TransactionImple.enlistResource", XAHelper.printXAErrorCode(exp) });
				}

				return false;
			}

			//	    if (threadIsActive(xaRes))
			//		return true; // this thread has already registered a resource for
			// this db

			/*
			 * We definitely haven't seen this specific resource instance
			 * before, but that doesn't mean that we haven't seen the RM it is
			 * connected to.
			 */

			Xid xid = null;
			TxInfo existingRM = isNewRM(xaRes);

			if (existingRM == null)
			{
				/*
				 * New RM, so create xid with new branch.
				 */

				boolean branchRequired = true;

				synchronized (this)
				{
					if (_resources.size() == 0)// first ever, so no need for
					// branch
					{
						//branchRequired = false;
						branchRequired = true;
					}
				}

				xid = createXid(branchRequired, theModifier);

				boolean associatedWork = false;
				int retry = 20;

				/*
				 * If another process has (or is about to) create the same
				 * transaction association then we will probably get a failure
				 * during start with XAER_DUPID. We know this must be due to
				 * another server, since we keep track of our own registrations.
				 * So, if this happens we create a new transaction branch and
				 * try again.
				 *
				 * To save time we could always just create branches by default.
				 *
				 * Is there a benefit to a zero branch?
				 */

				while (!associatedWork)
				{
					try
					{
                        if (_xaTransactionTimeoutEnabled)
                        {
                            int timeout = _theTransaction.getTimeout();

                            if (timeout > 0)
                            {
                                try
                                {
                                    xaRes.setTransactionTimeout(timeout);
                                }
                                catch (XAException te)
                                {
                                    if (jtaxLogger.loggerI18N.isWarnEnabled())
                                    {
                                        jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.timeouterror", new Object[]
                                        { "TransactionImple.enlistResource", XAHelper.printXAErrorCode(te), xid });
                                    }
                                }
                            }
                        }

                        // Pay attention now, this bit is hairy. We need to add a new XAResourceRecord
                        // to the transaction, which will thereafter drive its completion. However, the transaction
                        // core is not directly XA aware, so it's our job to start the XAResource. Problem is, if
                        // adding the record fails, the tx will never end the resource via the XAResourceRecord,
                        // so we must do so directly.  start may fail due to dupl xid or other reason, and transactions
                        // may rollback async, for which reasons we can't call add before start.
                        // The xid will change on each pass of the loop, so we need to create a new record on each pass.
                        // The registerResource will fail in the case of multiple last resources being disallowed.
                        // see JBTM-362 and JBTM-363
                        XAResourceRecord xaResourceRecord = createRecord(xaRes, params, xid);
                        if(xaResourceRecord != null) {
                            xaRes.start(xid, XAResource.TMNOFLAGS);
                            try {
                                RecoveryCoordinator recCoord = _theTransaction.registerResource(xaResourceRecord.getResource());
                                xaResourceRecord.setRecoveryCoordinator(recCoord);
                            } catch(Exception e) {
                                // we called start on the resource, but _theTransaction did not accept it.
                                // we therefore have a mess which we must now clean up by ensuring the start is undone:
                                xaResourceRecord.rollback();
                                markRollbackOnly();
                                return false;
                            }
                            _resources.put(xaRes, new TxInfo(xid));
                            return true; // dive out, no need to set associatedWork = true;
                        }

                        // if we get to here, something other than a failure of xaRes.start probably went wrong.
                        // so we don't loop and retry, we just give up.
                        markRollbackOnly();
                        return false;
					}
					catch (XAException e)
					{
						// transaction already created by another server

						/* We get this from Oracle instead of DUPID. */
						if (e.errorCode == XAException.XAER_RMERR)
						{

							if (retry > 0)
								xid = createXid(true, theModifier);

							retry--;
						}
						else
							if (e.errorCode == XAException.XAER_DUPID)
							{
								if (retry > 0)
									xid = createXid(true, theModifier);

								retry--;
							}
							else
							{
								/*
								 * Can't do start, so set transaction to
								 * rollback only.
								 */

								if (jtaxLogger.loggerI18N.isWarnEnabled())
								{
									jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.starterror", new Object[]
									{ "TransactionImple.enlistResource - XAResource.start", XAHelper.printXAErrorCode(e), xid });
								}

								markRollbackOnly();

								throw e;
							}

						if (retry < 0)
						{
							if (jtaxLogger.loggerI18N.isWarnEnabled())
							{
								jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.starterror", new Object[]
								{ "TransactionImple.enlistResource - XAResource.start", XAHelper.printXAErrorCode(e), xid });
							}

							markRollbackOnly();

							throw new UNKNOWN();
						}
					}
				}
			}
			else
			{
				/*
				 * Have seen this RM before, so ignore this instance. The first
				 * registered RM instance will be used to drive the transaction
				 * completion. We add it to the duplicateResource list so we can
				 * delist it correctly later though.
				 */

				/*
				 * Re-create xid.
				 */

				xid = existingRM.xid();

				try
				{
					xaRes.start(xid, XAResource.TMJOIN);
				}
				catch (XAException ex)
				{
					if (jtaxLogger.loggerI18N.isWarnEnabled())
					{
						jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
						{ "TransactionImple.enlistResource - xa_start: ", XAHelper.printXAErrorCode(ex) });
					}

					markRollbackOnly();

					throw ex;
				}

				/*
				 * Add to duplicate resources list so we can keep track of it
				 * (particularly if we later have to delist).
				 */

				_duplicateResources.put(xaRes, new TxInfo(xid));

				return true;
			}

            return false;
        }
		catch (Exception e)
		{
			/*
			 * Some exceptional condition arose and we probably could not enlist
			 * the resouce. So, for safety mark the transaction as rollback
			 * only.
			 */

			markRollbackOnly();

			return false;
		}
	}

    /**
     * Attempt to create an XAResourceRecord wrapping the given XAResource. Return null if this fails, or
     * is diallowed by the current configuration of multiple last resource behaviour.
     *
     * @param xaRes
     * @param params
     * @param xid
     * @return
     */
    private XAResourceRecord createRecord(XAResource xaRes, Object[] params, Xid xid)
    {
        final XAResourceRecord record;
        if ((xaRes instanceof LastResourceCommitOptimisation)
                || ((LAST_RESOURCE_OPTIMISATION_INTERFACE != null) && LAST_RESOURCE_OPTIMISATION_INTERFACE
                .isInstance(xaRes)))
        {
                record = new LastResourceRecord(this, xaRes, xid, params);
        }
        else
        {
            record = new XAResourceRecord(this, xaRes, xid, params);
        }

        return record;
    }

    /*
	 * Do we have to unregister resources? Assume not as it would not make much
	 * sense otherwise!
	 */

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.unknownres
	 *          [com.arjuna.ats.internal.jta.transaction.jts.unknownres] {0}
	 *          attempt to delist unknown resource!
	 * @message com.arjuna.ats.internal.jta.transaction.jts.delistfailed
	 *          [com.arjuna.ats.internal.jta.transaction.jts.delistfailed]
	 *          Delist of resource failed with: {0}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.ressusp resource
	 *          [com.arjuna.ats.internal.jta.transaction.jts.ressusp resource]
	 *          is already suspended!
	 */

	public boolean delistResource (XAResource xaRes, int flags)
			throws IllegalStateException, javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.delistResource ( "
					+ xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.delistResource - "
							+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			break;
		default:
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
		}

		TxInfo info = null;

		try
		{
			synchronized (this)
			{
				info = (TxInfo) _resources.get(xaRes);

				if (info == null)
					info = (TxInfo) _duplicateResources.get(xaRes);
			}

			if (info == null)
			{
				if (jtaxLogger.loggerI18N.isWarnEnabled())
				{
					jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.unknownres", new Object[]
					{ "TransactionImple.delistResource" });
				}

				return false;
			}
			else
			{
				boolean optimizedRollback = false;

				try
				{
					/*
					 * If we know the transaction is going to rollback, then we
					 * can try to rollback the RM now. Just an optimisation.
					 */

					if (status == javax.transaction.Status.STATUS_MARKED_ROLLBACK)
					{
						if (XAUtils.canOptimizeDelist(xaRes))
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							xaRes.rollback(info.xid());

							info.setState(TxInfo.OPTIMIZED_ROLLBACK);

							optimizedRollback = true;
						}
					}
				}
				catch (Exception e)
				{
					// failed, so try again when transaction does rollback
				}

				switch (info.getState())
				{
				case TxInfo.ASSOCIATED:
				{
					if ((flags & XAResource.TMSUCCESS) != 0)
					{
						xaRes.end(info.xid(), XAResource.TMSUCCESS);
						info.setState(TxInfo.NOT_ASSOCIATED);
					}
					else
					{
						if ((flags & XAResource.TMSUSPEND) != 0)
						{
							xaRes.end(info.xid(), XAResource.TMSUSPEND);
							info.setState(TxInfo.ASSOCIATION_SUSPENDED);

							synchronized (this)
							{
								_suspendCount++;
							}
						}
						else
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							info.setState(TxInfo.FAILED);
						}
					}
				}
					break;
				case TxInfo.ASSOCIATION_SUSPENDED:
				{
					if ((flags & XAResource.TMSUCCESS) != 0)
					{
						// Oracle barfs if we don't resume first, despite what
						// XA says!

						if (XAUtils.mustEndSuspendedRMs(xaRes))
							xaRes.start(info.xid(), XAResource.TMRESUME);

						xaRes.end(info.xid(), XAResource.TMSUCCESS);
						info.setState(TxInfo.NOT_ASSOCIATED);

						synchronized (this)
						{
							_suspendCount--;
						}
					}
					else
					{
						if ((flags & XAResource.TMSUSPEND) != 0)
						{
							// Note: this exception will be caught by our catch
							// block

							throw new IllegalStateException(
									"TransactionImple.delistResource - "
											+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.ressusp"));
						}
						else
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							info.setState(TxInfo.FAILED);

							synchronized (this)
							{
								_suspendCount--;
							}
						}
					}
				}
					break;
				default:
					if (!optimizedRollback)
						throw new IllegalStateException(
								"TransactionImple.delistResource - "
										+ jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.illegalstate")
										+ info.getState());
				}

				info = null;

				return true;
			}
		}
		catch (IllegalStateException ex)
		{
			throw ex;
		}
		catch (XAException exp)
		{
			if (info != null)
				info.setState(TxInfo.FAILED);

			/*
			 * For safety mark the transaction as rollback only.
			 */

			markRollbackOnly();

			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
				{ "TransactionImple.delistResource", XAHelper.printXAErrorCode(exp) });
			}

			return false;
		}
		catch (Exception e)
		{
			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.delistfailed", new Object[]
				{ e });
			}

			/*
			 * Some exception occurred and we probably could not delist the
			 * resource. So, for safety mark the transaction as rollback only.
			 */

			markRollbackOnly();

			return false;
		}
	}

	public final Uid get_uid ()
	{
		return _theTransaction.get_uid();
	}
	
	public final Xid getTxId ()
	{
	    Xid res = baseXid();
	    
	    if (res == null)
	        return _theTransaction.get_xid(false);
	    
	    return res;
	}

	public String toString ()
	{
		if (_theTransaction == null)
			return "TransactionImple < jts, NoTransaction >";
		else
		{
			return "TransactionImple < jts, " + _theTransaction.get_uid()
					+ " >";
		}
	}

	public int getXAResourceState (XAResource xaRes)
	{
		int state = TxInfo.UNKNOWN;

		if (xaRes != null)
		{
			TxInfo info = (TxInfo) _resources.get(xaRes);

			if (info == null)
			{
				info = (TxInfo) _duplicateResources.get(xaRes);
			}

			if (info != null)
				state = info.getState();
		}

		return state;
	}

	/**
	 * Creates if does not exist and adds to our internal mapping table.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.nottximple
	 *          [com.arjuna.ats.internal.jta.transaction.jts.nottximple] Current
	 *          transaction is not a TransactionImple
	 */

	static final TransactionImple getTransaction ()
	{
		TransactionImple tx = null;

		ControlWrapper otx = OTSImpleManager.current().getControlWrapper();

		if (otx != null)
		{
            synchronized (TransactionImple._transactions)
            {
				try
				{
					tx = (TransactionImple) TransactionImple._transactions.get(otx.get_uid());

					if (tx == null)
					{
						/*
						 * If it isn't active then don't add it to the
						 * hashtable.
						 */

						tx = new TransactionImple(new AtomicTransaction(otx));

						try
						{
							if (tx.getStatus() == javax.transaction.Status.STATUS_ACTIVE)
							{
								putTransaction(tx);
							}
						}
						catch (Exception ex)
						{
							// shouldn't happen!
						}
					}
				}
				catch (ClassCastException ex)
				{
					if (jtaxLogger.loggerI18N.isWarnEnabled())
					{
						jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.nottximple");
					}
				}
			}
		}

		return tx;
	}

	public final void shutdown ()
	{
		removeTransaction(this);
	}

    // get a key-value pair from a transaction specific Map
	public Object getTxLocalResource(Object key)
	{
		return _txLocalResources.get(key);
	}

	// store a key-value pair in the scope of the transaction.
	public void putTxLocalResource(Object key, Object value)
	{
		_txLocalResources.put(key, value);
	}


    /*
     * For JBossAS integration TransactionLocal implementation, we need to know if a tx has been
     * resolved yet or not. We could use getStatus() and a case stmt, but since an instance is
     * removed from _transactions on completion this is just as effective.
     * @param tx
     * @return
     */
    public boolean isAlive() {
        return _transactions.contains(this);
    }


    protected TransactionImple (AtomicTransaction tx)
	{
		_theTransaction = tx;

		if (tx != null)
		{
			_resources = new Hashtable();
			_duplicateResources = new Hashtable();
            _txLocalResources = Collections.synchronizedMap(new HashMap());
        }
		else
		{
			_resources = null;
			_duplicateResources = null;
		}

		_suspendCount = 0;

		try
		{
			if (getStatus() == javax.transaction.Status.STATUS_ACTIVE)
			{
				TwoPhaseCoordinator theTx = null;

				try
				{
					/*
					 * If this is an imported transaction and we have just
					 * performed interposition, then register a purely local
					 * Synchronization. This gets us over a performance issue
					 * with JacORB.
					 */

					theTx = (TwoPhaseCoordinator) BasicAction.Current();

					if (theTx != null)
						theTx.addSynchronization(new LocalCleanupSynchronization(
								this));
					else
						registerSynchronization(new CleanupSynchronization(this));
				}
				catch (ClassCastException ex)
				{
					/*
					 * Not a local/interposed transaction.
					 */

					registerSynchronization(new CleanupSynchronization(this));
				}
			}
		}
		catch (Exception ex)
		{
			/*
			 * Could set rollback only, but let's take the possible memory leak
			 * hit for now.
			 */

			jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.syncproblem", ex);
		}
		_xaTransactionTimeoutEnabled = getXATransactionTimeoutEnabled() ;
    }

	protected void commitAndDisassociate ()
			throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.commitAndDisassociate");
		}

		if (_theTransaction != null)
		{
			try
			{
				_theTransaction.commit(true);
			}
			catch (org.omg.CosTransactions.WrongTransaction wt)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"), wt);
			}
			catch (org.omg.CosTransactions.NoTransaction e1)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"), e1);
			}
			catch (org.omg.CosTransactions.HeuristicMixed e2)
			{
                HeuristicMixedException heuristicMixedException = new javax.transaction.HeuristicMixedException();
                heuristicMixedException.initCause(e2);
                throw heuristicMixedException;
			}
			catch (org.omg.CosTransactions.HeuristicHazard e3)
			{
                HeuristicMixedException heuristicMixedException = new javax.transaction.HeuristicMixedException();
                heuristicMixedException.initCause(e3);
                throw heuristicMixedException;
			}
			catch (TRANSACTION_ROLLEDBACK e4)
			{
				RollbackException rollbackException = new RollbackException(e4.toString());
                if(_rollbackOnlyCallerStacktrace != null) {
                    // we rolled back beacuse the user explicitly told us not to commit. Attach the trace of who did that for debug:
                    rollbackException.initCause(_rollbackOnlyCallerStacktrace);
                }
                throw rollbackException;
			}
			catch (org.omg.CORBA.NO_PERMISSION e5)
			{
				throw new SecurityException(e5);
			}
			catch (INVALID_TRANSACTION e6)
			{
			    /*
			     * In JTS/OTS we can indicate that something was terminated by another thread.
			     * JTA doesn't really prevent this, but ...
			     */

				//throw new IllegalStateException(
				//		jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (org.omg.CORBA.SystemException e7)
			{
                InvalidTerminationStateException invalidTerminationStateException = new InvalidTerminationStateException();
                invalidTerminationStateException.initCause(e7);
                throw invalidTerminationStateException;
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	protected void rollbackAndDisassociate ()
			throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaxLogger.logger.isDebugEnabled())
		{
			jtaxLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.rollbackAndDisassociate");
		}

		if (_theTransaction != null)
		{
			try
			{
				_theTransaction.rollback();
			}
			catch (org.omg.CosTransactions.WrongTransaction e1)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"), e1);
			}
			catch (org.omg.CORBA.NO_PERMISSION e2)
			{
				throw new SecurityException(e2);
			}
			catch (INVALID_TRANSACTION e3)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"), e3);
			}
			catch (NoTransaction e4)
			{
				throw new IllegalStateException(
						jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"), e4);
			}
			catch (org.omg.CORBA.SystemException e5)
			{
                javax.transaction.SystemException systemException = new javax.transaction.SystemException(e5.toString());
                systemException.initCause(e5);
		        throw systemException;
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	/**
	 * If this is an imported JCA transaction, then this method will return the
	 * Xid we should pretend to be. Otherwise it'll return null and we will generate
	 * our own Xid.
	 *
	 * @return null for a pure ATS transaction, otherwise a valid JCA imported Xid.
	 */

	protected Xid baseXid ()
	{
		return null;
	}

	/*
	 * Add and remove transactions from list.
	 */

	protected static final void putTransaction (TransactionImple tx)
	{
		TransactionImple._transactions.put(tx.get_uid(), tx);
	}

	protected static final void removeTransaction (TransactionImple tx)
	{
		TransactionImple._transactions.remove(tx.get_uid());
	}

	/**
	 * If there are any suspended RMs then we should call end on them before the
	 * transaction is terminated.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.xaenderror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.xaenderror]
	 *          Could not call end on a suspended resource!
	 */

	protected boolean endSuspendedRMs ()
	{
		boolean result = true;

		if (_suspendCount > 0)
		{
			Enumeration el = _resources.keys();

			/*
			 * Loop over all registered resources. Those that are in a suspended
			 * state must have end called on them. If this fails, then we will
			 * eventually roll back the transaction, but we will continue down
			 * the list to try to end any other suspended resources.
			 */

			if (el != null)
			{
				try
				{
					while (el.hasMoreElements())
					{
						/*
						 * Get the XAResource in case we have to call end on it.
						 */

						XAResource xaRes = (XAResource) el.nextElement();
						TxInfo info = (TxInfo) _resources.get(xaRes);

						if (info.getState() == TxInfo.ASSOCIATION_SUSPENDED)
						{
							if (XAUtils.mustEndSuspendedRMs(xaRes))
								xaRes.start(info.xid(), XAResource.TMRESUME);

							xaRes.end(info.xid(), XAResource.TMSUCCESS);
							info.setState(TxInfo.NOT_ASSOCIATED);
						}
					}
				}
				catch (XAException ex)
				{
					if (jtaxLogger.loggerI18N.isWarnEnabled())
					{
						jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaenderror");
					}

					result = false;
				}
			}

			/*
			 * do the same again for duplicate resources
			 */

			el = _duplicateResources.keys();

			if (el != null)
			{
				try
				{
					while (el.hasMoreElements())
					{
						/*
						 * Get the XAResource in case we have to call end on it.
						 */

						XAResource xaRes = (XAResource) el.nextElement();
						TxInfo info = (TxInfo) _duplicateResources.get(xaRes);

						if (info.getState() == TxInfo.ASSOCIATION_SUSPENDED)
						{
							if (XAUtils.mustEndSuspendedRMs(xaRes))
								xaRes.start(info.xid(), XAResource.TMRESUME);

							xaRes.end(info.xid(), XAResource.TMSUCCESS);
							info.setState(TxInfo.NOT_ASSOCIATED);
						}
					}
				}
				catch (XAException ex)
				{
					if (jtaxLogger.loggerI18N.isWarnEnabled())
					{
						jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaenderror");
					}

					result = false;
				}
			}

            _suspendCount = 0;
		}

		return result;
	}

	/**
	 * If this thread has already registered a resource for the same db then
	 * don't use this copy. For some databases it would actually be ok for us to
	 * use the resource (at least to do an xa_start equivalent on it), but for
	 * Oracle 8.1.6 it causes their JDBC driver to crash!
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.threaderror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.threaderror] Active
	 *          thread error:
	 */

	private final boolean threadIsActive (XAResource xaRes)
	{
		Thread t = Thread.currentThread();

		try
		{
			Enumeration el = _resources.keys();

			if (el != null)
			{
				while (el.hasMoreElements())
				{
					XAResource x = (XAResource) el.nextElement();

					if (x.isSameRM(xaRes))
					{
						TxInfo info = (TxInfo) _resources.get(x);

						if (info.thread() == t)
							return true;
					}
				}
			}

			el = _duplicateResources.keys();

			if (el != null)
			{
				while (el.hasMoreElements())
				{
					XAResource x = (XAResource) el.nextElement();

					if (x.isSameRM(xaRes))
					{
						TxInfo info = (TxInfo) _resources.get(x);

						if (info.thread() == t)
							return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.threaderror", e);
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}

		return false;
	}

	/**
	 * isNewRM returns an existing TxInfo for the same RM, if present. Null
	 * otherwise.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.rmerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.rmerror] An error
	 *          occurred while checking if this is a new resource manager:
	 */

	private final TxInfo isNewRM (XAResource xaRes)
	{
		try
		{
			synchronized (this)
			{
				Enumeration el = _resources.keys();

				if (el != null)
				{
					while (el.hasMoreElements())
					{
						XAResource x = (XAResource) el.nextElement();

						if (x.isSameRM(xaRes))
						{
							return (TxInfo) _resources.get(x);
						}
					}
				}

				el = _duplicateResources.keys();

				if (el != null)
				{
					while (el.hasMoreElements())
					{
						XAResource x = (XAResource) el.nextElement();

						if (x.isSameRM(xaRes))
						{
							return (TxInfo) _duplicateResources.get(x);
						}
					}
				}
			}
		}
		catch (XAException ex)
		{
			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
				{ "TransactionImple.isNewRM", XAHelper.printXAErrorCode(ex) });
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(ex.toString(), ex);
		}
		catch (Exception e)
		{
			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.rmerror", e);
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}

		return null;
	}

	private final Xid createXid (boolean branch, XAModifier theModifier)
	{
		Xid jtaXid = baseXid();

		if (jtaXid != null)
			return jtaXid;

		try
		{
			jtaXid = _theTransaction.get_xid(branch);

			if (theModifier != null)
			{
				try
				{
					jtaXid = theModifier.createXid(jtaXid);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return jtaXid;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This method calls setRollbackOnly and catches any exceptions it may throw
	 * and issues a warning. We use this in places wherew we need to force the
	 * outcome of the transaction but already have an exception to throw back to
	 * the application, so a failure here will only be masked.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.rollbackerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.rollbackerror] {0}
	 *          could not mark the transaction as rollback only: {1}
	 */

	private final void markRollbackOnly ()
	{
		try
		{
			if (_theTransaction != null)
			{
				try
				{
					_theTransaction.rollbackOnly();
				}
				catch (org.omg.CosTransactions.NoTransaction e3)
				{
					// ok
				}
				catch (org.omg.CORBA.SystemException e3)
				{
                    javax.transaction.SystemException systemException = new javax.transaction.SystemException(e3.toString());
                    systemException.initCause(e3);
		            throw systemException;
                }
			}
		}
		catch (Exception ex)
		{
			if (jtaxLogger.loggerI18N.isWarnEnabled())
			{
				jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.rollbackerror", new Object[]
				{ "TransactionImple.markRollbackOnly -", ex }, ex);
			}
		}
	}

	private static boolean getXATransactionTimeoutEnabled()
	{
		return XA_TRANSACTION_TIMEOUT_ENABLED ;
	}

    public static Map<Uid, javax.transaction.Transaction> getTransactions()
    {
        return Collections.unmodifiableMap(_transactions);
    }

    public Map<XAResource, TxInfo> getResources()
    {
        return Collections.unmodifiableMap(_resources);
    }

    public int getTimeout()
    {
        return _theTransaction.getTimeout();
    }

    public long getRemainingTimeoutMills() {
        return TransactionReaper.transactionReaper().getRemainingTimeoutMills(_theTransaction.getControlWrapper());
    }

    public java.util.Map<Uid, String> getSynchronizations()
    {
        if (_theTransaction != null)
            return _theTransaction.getControlWrapper().getSynchronizations();

        return Collections.EMPTY_MAP;
    }

    protected AtomicTransaction _theTransaction;

	private Hashtable _resources;
	private Hashtable _duplicateResources;
	private int _suspendCount;
	private final boolean _xaTransactionTimeoutEnabled ;
    private Map _txLocalResources;

    private Throwable _rollbackOnlyCallerStacktrace;

	private static final boolean XA_TRANSACTION_TIMEOUT_ENABLED ;
	private static final Class LAST_RESOURCE_OPTIMISATION_INTERFACE ;

	static
	{
        XA_TRANSACTION_TIMEOUT_ENABLED = jtaPropertyManager.getJTAEnvironmentBean().isXaTransactionTimeoutEnabled();

        final String lastResourceOptimisationInterfaceName = jtaPropertyManager.getJTAEnvironmentBean().getLastResourceOptimisationInterface();
		Class lastResourceOptimisationInterface = null ;
		if (lastResourceOptimisationInterfaceName != null)
		{
			try
			{
				lastResourceOptimisationInterface = Thread.currentThread().getContextClassLoader().loadClass(lastResourceOptimisationInterfaceName) ;
			}
			catch (final Throwable th)
			{
				if (jtaxLogger.loggerI18N.isWarnEnabled())
				{
					jtaxLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface",
						new Object[] {lastResourceOptimisationInterfaceName}, th);
				}
			}
		}
		LAST_RESOURCE_OPTIMISATION_INTERFACE = lastResourceOptimisationInterface ;
	}

    private static ConcurrentHashMap _transactions = new ConcurrentHashMap();

}
