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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore;

import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.internal.jta.utils.*;
import com.arjuna.ats.internal.jta.utils.arjunacore.StatusConverter;
import com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.logging.*;
import com.arjuna.ats.jta.xa.XAModifier;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.common.util.logging.*;

import javax.transaction.xa.*;

import java.util.*;

import javax.transaction.RollbackException;
import javax.transaction.Status;

import java.lang.IllegalStateException;

import java.util.concurrent.ConcurrentHashMap;

/*
 * Is given an AtomicAction, but uses the TwoPhaseCoordinator aspects of it to
 * ensure that the thread association continues.
 */

/**
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.inactive
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.inactive] The
 *          transaction is not active!
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.nullres
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.nullres]
 *          Resource paramater is null!
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.illresstate
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.illresstate]
 *          illegal resource state
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.notatomicaction
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.notatomicaction]
 *          Current transaction is not an AtomicAction!
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.lastResourceOptimisationInterface
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.lastResourceOptimisationInterface] -
 *          failed to load Last Resource Optimisation Interface
 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate
 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate] The
 *          transaction is in an invalid state!
 */

public class TransactionImple implements javax.transaction.Transaction,
		com.arjuna.ats.jta.transaction.Transaction
{

	/*
	 * Only works with AtomicAction and TwoPhaseCoordinator.
	 */

	/**
	 * Create a new transaction with the specified timeout.
	 */

	public TransactionImple(int timeout)
	{
		_theTransaction = new AtomicAction();

		_theTransaction.begin(timeout);

		_resources = new Hashtable();
		_duplicateResources = new Hashtable();
		_suspendCount = 0;
		_xaTransactionTimeoutEnabled = getXATransactionTimeoutEnabled();

        _txLocalResources = Collections.synchronizedMap(new HashMap());
    }

	/**
	 * Overloads Object.equals()
	 */

	public boolean equals(Object obj)
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.equals");
		}

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof TransactionImple)
		{
			/*
			 * If we can't get either coordinator to compare, then assume
			 * transactions are different.
			 */

			try
			{
				TransactionImple tx = (TransactionImple) obj;

				return tx.get_uid().equals(_theTransaction.get_uid());
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	/**
	 * Return -1 if we fail.
	 */

	public int hashCode()
	{
		if (_theTransaction == null)
			return -1;
		else
			return _theTransaction.get_uid().hashCode();
	}

	/**
	 * The JTA specification is vague on whether the calling thread can have any
	 * transaction associated with it. It does say that it need not have the
	 * same transaction as this one. We could call suspend prior to making these
	 * calls, but for now we do nothing.
	 */

	/**
	 * We should never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.commitwhenaborted
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.commitwhenaborted]
	 *          Could not commit transaction.
	 */

	public void commit() throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.commit");
		}

		if (_theTransaction != null)
		{
			switch (_theTransaction.status())
			{
			case ActionStatus.RUNNING:
			case ActionStatus.ABORT_ONLY:
				break;
			default:
				throw new IllegalStateException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
			}

			/*
			 * Call end on any suspended resources. If this fails, then the
			 * transaction will be rolled back.
			 */

			if (!endSuspendedRMs())
				_theTransaction.preventCommit();

			// use end of TwoPhaseCoordinator to avoid thread changes.

			int status = _theTransaction.end(true);

			TransactionImple.removeTransaction(this);

			switch (status)
			{
			case ActionStatus.COMMITTED:
			case ActionStatus.COMMITTING: // in case of async commit
				break;
			case ActionStatus.H_MIXED:
				throw new javax.transaction.HeuristicMixedException();
			case ActionStatus.H_HAZARD:
				throw new javax.transaction.HeuristicMixedException();
			case ActionStatus.H_ROLLBACK:
			case ActionStatus.ABORTED:
				RollbackException rollbackException = new RollbackException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.commitwhenaborted"));
				if (_theTransaction.getDeferredThrowable() != null)
				{
					rollbackException.initCause(_theTransaction
							.getDeferredThrowable());
				}
				throw rollbackException;
			default:
				throw new IllegalStateException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate"));
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.rollbackstatus
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.rollbackstatus]
	 *          Transaction rollback status is:
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed1
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed1]
	 *          Ending suspended RMs failed when rolling back the transaction!
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed2
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed2]
	 *          Ending suspended RMs failed when rolling back the transaction,
	 *          but transaction rolled back.
	 */

	public void rollback() throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.rollback");
		}

		if (_theTransaction != null)
		{
			switch (_theTransaction.status())
			{
			case ActionStatus.RUNNING:
			case ActionStatus.ABORT_ONLY:
				break;
			default:
				throw new IllegalStateException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
			}

			/*
			 * Call end on any suspended resources. If this fails, then there's
			 * not a lot else we can do because the transaction is about to roll
			 * back anyway!
			 */

			boolean endSuspendedFailed = !endSuspendedRMs();

			if (endSuspendedFailed)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn("com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed1");
				}
			}

			int outcome = _theTransaction.cancel(); // use cancel of
			// TwoPhaseCoordinator to
			// avoid thread changes.

			TransactionImple.removeTransaction(this);

			switch (outcome)
			{
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING: // in case of async rollback
				break;
			default:
				throw new IllegalStateException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.rollbackstatus")
								+ ActionStatus.stringForm(outcome));
			}

			if (endSuspendedFailed)
				throw new IllegalStateException(
						jtaLogger.loggerI18N
								.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.endsuspendfailed2"));
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
	}

	public void setRollbackOnly() throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
	    if (jtaLogger.logger.isDebugEnabled())
	    {
	        jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
	                VisibilityLevel.VIS_PUBLIC,
	                com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
	        "TransactionImple.setRollbackOnly");
	    }

	    if (_theTransaction != null)
	    {
	        /*
	         * Try to mark the transaction as rollback-only. If that fails figure out why.
	         */
	        
	        if (!_theTransaction.preventCommit())
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
	        else
	        {
	            // keep a record of why we are rolling back i.e. who called us first, it's a useful debug aid.
	            if(_rollbackOnlyCallerStacktrace == null)
	            {
	                _rollbackOnlyCallerStacktrace = new Throwable("setRollbackOnly called from:");
	            }
	        }
	    }
	    else
	        throw new IllegalStateException(
	                jtaLogger.loggerI18N
	                .getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
	}

	public int getStatus() throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.getStatus");
		}

		int status = javax.transaction.Status.STATUS_NO_TRANSACTION;

		if (_theTransaction != null)
		{
			return StatusConverter.convert(_theTransaction.status());
		}

		return status;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.nullparam
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.nullparam]
	 *          null synchronization parameter!
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.syncsnotallowed
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.syncsnotallowed]
	 *          Synchronizations are not allowed! Transaction status is
	 */

	public void registerSynchronization(javax.transaction.Synchronization sync)
			throws javax.transaction.RollbackException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.registerSynchronization");
		}

		if (sync == null)
		{
			throw new javax.transaction.SystemException(
					"TransactionImple.registerSynchronization - "
							+ jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.nullparam"));
		}

		registerSynchronizationImple(new SynchronizationImple(sync, false));
	}

	// package-private method also for use by
	// TransactionSynchronizationRegistryImple
	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.syncwhenaborted
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.syncwhenaborted]
	 *          Can't register synchronization because the transaction is in
	 *          aborted state
	 */
	void registerSynchronizationImple(SynchronizationImple synchronizationImple)
			throws javax.transaction.RollbackException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (_theTransaction != null)
		{
			if (_theTransaction.addSynchronization(synchronizationImple) != AddOutcome.AR_ADDED)
			{
				int status = _theTransaction.status();

				switch (status)
				{
				case ActionStatus.ABORT_ONLY:
				case ActionStatus.ABORTED:
					throw new javax.transaction.RollbackException(
							jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.syncwhenaborted"));
				case ActionStatus.CREATED:
					throw new IllegalStateException(
							jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
				default:
					throw new IllegalStateException(
							jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.syncsnotallowed")+ActionStatus.stringForm(status));
				}
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
	}

	/**
	 * This is the JTA compliant version of the method. However, you had better
	 * know that your XAResource and family are truly compliant implementations.
	 * If they aren't then we may fail gracefully (e.g., some versions of Oracle
	 * don't work with arbitrary Xid implementations!)
	 *
	 * If the family isn't compliant, then you should use the other method and
	 * pass through a relevant XAModifier, which should address the issues we
	 * have already come across.
	 */

	public boolean enlistResource(XAResource xaRes) throws RollbackException,
			IllegalStateException, javax.transaction.SystemException
	{
		return enlistResource(xaRes, null);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.enlisterror
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.enlisterror]
	 *          {0} - caught: {1}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.timeouterror[com.arjuna.ats.internal.jta.transaction.arjunacore.timeouterror]
	 *          {0} setTransactionTimeout on XAResource threw: {1}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.enliststarterror
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.enliststarterror]
	 *          {0} - XAResource.start returned: {1} for {2}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.couldnotregister
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.couldnotregister]
	 *          could not register transaction
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.xastart
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.xastart] {0} -
	 *          caught: {1} for {2}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.elistwhenmarkedrollback
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.elistwhenmarkedrollback]
	 *          Can't enlist the resource because the transaction is marked for
	 *          rollback
	 */
	public boolean enlistResource(XAResource xaRes, Object[] params)
			throws RollbackException, IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.enlistResource ( " + xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.enlistResource - "
							+ jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.nullres"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			throw new RollbackException(
					"TransactionImple.enlistResource - "
							+ jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate"));
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		default:
			throw new IllegalStateException(
					jtaLogger.loggerI18N
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
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

						int xaStartResume = ((theModifier == null) ? XAResource.TMRESUME
								: theModifier
										.xaStartParameters(XAResource.TMRESUME));

						xaRes.start(info.xid(), xaStartResume);

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

						int xaStartJoin = ((theModifier == null) ? XAResource.TMJOIN
								: theModifier
										.xaStartParameters(XAResource.TMJOIN));

						xaRes.start(info.xid(), xaStartJoin);

						info.setState(TxInfo.ASSOCIATED);

						return true;
					}
					default:
					{
						// Note: this exception will be caught by our catch
						// block

						throw new IllegalStateException(
								"TransactionImple.enlistResource - "
										+ jtaLogger.loggerI18N
												.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.illresstate")
										+ ":" + info.getState());
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

				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn(
									"com.arjuna.ats.internal.jta.transaction.arjunacore.enlisterror",
									new Object[]
									{ "TransactionImple.enlistResource",
											XAHelper.printXAErrorCode(exp) });
				}

				return false;
			}

			// if (threadIsActive(xaRes))
			// return true; // this thread has already registered a resource for
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
						// branchRequired = false;
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
									if (jtaLogger.loggerI18N.isWarnEnabled())
									{
										jtaLogger.loggerI18N
												.warn(
														"com.arjuna.ats.internal.jta.transaction.arjunacore.timeouterror",
														new Object[]
														{
																"TransactionImple.enlistResource",
																XAHelper
																		.printXAErrorCode(te),
																xid });
									}
								}
							}
						}

						int xaStartNormal = ((theModifier == null) ? XAResource.TMNOFLAGS
								: theModifier
										.xaStartParameters(XAResource.TMNOFLAGS));


                        // Pay attention now, this bit is hairy. We need to add a new AbstractRecord (XAResourceRecord)
                        // to the BasicAction, which will thereafter drive its completion. However, the transaction
                        // core is not directly XA aware, so it's our job to start the XAResource. Problem is, if
                        // adding the record fails, BasicAction will never end the resource via the XAResourceRecord,
                        // so we must do so directly.  start may fail due to dupl xid or other reason, and transactions
                        // may rollback async, for which reasons we can't call add before start.
                        // The xid will change on each pass of the loop, so we need to create a new record on each pass.
                        // The add will fail in the case of multiple last resources being disallowed
                        // see JBTM-362 and JBTM-363
                        AbstractRecord abstractRecord = createRecord(xaRes, params, xid);
                        if(abstractRecord != null) {
                            xaRes.start(xid, xaStartNormal);
                            if(_theTransaction.add(abstractRecord) == AddOutcome.AR_ADDED) {
                                _resources.put(xaRes, new TxInfo(xid));
                                return true; // dive out, no need to set associatedWork = true;
                            } else {
                                // we called start on the resource, but _theTransaction did not accept it.
                                // we therefore have a mess which we must now clean up by ensuring the start is undone:
                                abstractRecord.topLevelAbort();
                            }
                        }

                        // if we get to here, something other than a failure of xaRes.start probably went wrong.
                        // so we don't loop and retry, we just give up.
                        markRollbackOnly();
                        return false;

					}
					catch (XAException e)
					{
						// transaction already created by another server

						if ((e.errorCode == XAException.XAER_DUPID)
								|| (e.errorCode == XAException.XAER_RMERR))
						{
							if (retry > 0)
								xid = createXid(true, theModifier);

							retry--;
						}
						else
						{
							/*
							 * Can't do start, so set transaction to rollback
							 * only.
							 */

							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N
										.warn(
												"com.arjuna.ats.internal.jta.transaction.arjunacore.enliststarterror",
												new Object[]
												{
														"TransactionImple.enlistResource",
														XAHelper
																.printXAErrorCode(e),
														xid });
							}

							markRollbackOnly();

							throw e;
						}

						if (retry < 0)
						{
							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N
										.warn(
												"com.arjuna.ats.internal.jta.transaction.arjunacore.enliststarterror",
												new Object[]
												{
														"TransactionImple.enlistResource",
														XAHelper
																.printXAErrorCode(e),
														xid });
							}

							markRollbackOnly();

							throw new javax.transaction.SystemException(
									"TransactionImple.enlistResource - XAResource.start "
											+ jtaLogger.loggerI18N
													.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.couldnotregister")
											+ ": " + xid);
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
					int xaStartJoin = ((theModifier == null) ? XAResource.TMJOIN
							: theModifier.xaStartParameters(XAResource.TMJOIN));

					xaRes.start(xid, xaStartJoin);
				}
				catch (XAException ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N
								.warn(
										"com.arjuna.ats.internal.jta.transaction.arjunacore.xastart",
										new Object[]
										{
												"TransactionImple.enlistResource - xa_start ",
												XAHelper.printXAErrorCode(ex),
												xid });
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
			e.printStackTrace();

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
     * Attempt to create an AbstractRecord wrapping the given XAResource. Return null if this fails, or
     * is diallowed by the current configuration of multiple last resource behaviour.
     *
     * @param xaRes
     * @param params
     * @param xid
     * @return
     */
    private AbstractRecord createRecord(XAResource xaRes, Object[] params, Xid xid)
    {
        final AbstractRecord record;
        if ((xaRes instanceof LastResourceCommitOptimisation)
                || ((LAST_RESOURCE_OPTIMISATION_INTERFACE != null) && LAST_RESOURCE_OPTIMISATION_INTERFACE
                .isInstance(xaRes)))
        {
            record = new LastResourceRecord(new XAOnePhaseResource(xaRes, xid, params));
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
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.unknownresource
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.unknownresource]
	 *          {0} - unknown resource
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.delistresource
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.delistresource]
	 *          {0} - caught exception during delist : {1}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.delistgeneral
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.delistgeneral]
	 *          {0} caught exception {1}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.ressuspended
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.ressuspended]
	 *          resource already suspended.
	 */

	public boolean delistResource(XAResource xaRes, int flags)
			throws IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"TransactionImple.delistResource ( " + xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.delistResource - "
							+ jtaLogger.loggerI18N
									.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.nullres"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			break;
		default:
			throw new IllegalStateException(
					jtaLogger.loggerI18N
							.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
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
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn(
									"com.arjuna.ats.internal.jta.transaction.arjunacore.unknownresource",
									new Object[]
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
						// Oracle barfs if we don't send resume first, despite
						// what XA says!

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
											+ jtaLogger.loggerI18N
													.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.ressuspended"));
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
				{
					if (!optimizedRollback)
						throw new IllegalStateException(
								"TransactionImple.delistResource - "
										+ jtaLogger.loggerI18N
												.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.illresstate")
										+ ":" + info.getState());
				}
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

			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.delistresource",
								new Object[]
								{ "TransactionImple.delistResource",
										XAHelper.printXAErrorCode(exp) });
			}

			return false;
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.delistgeneral",
								new Object[]
								{ "TransactionImple.delistResource", e });
			}

			/*
			 * Some exception occurred and we probably could not delist the
			 * resource. So, for safety mark the transaction as rollback only.
			 */

			markRollbackOnly();

			return false;
		}
	}

	public final Uid get_uid()
	{
		return _theTransaction.get_uid();
	}

	public final Xid getTxId ()
	{
	    Xid res = baseXid();
	    
	    if (res == null)
	        res = new XidImple(_theTransaction, false);
	    
	    return res;
	}
	
	public String toString()
	{
		if (_theTransaction == null)
			return "TransactionImple < ac, NoTransaction >";
		else
		{
			return "TransactionImple < ac, " + _theTransaction + " >";
		}
	}

	public int getXAResourceState(XAResource xaRes)
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

	public static final TransactionImple getTransaction()
	{
		TransactionImple tx = null;

		final BasicAction current = BasicAction.Current();
		if (current != null)
		{
			final Uid txid = current.get_uid();

			tx = (TransactionImple) _transactions.get(txid);
			if (tx == null)
				tx = new TransactionImple(current);
		}

		return tx;
	}

	public static final TransactionImple getTransaction(Uid id)
	{
		try
		{
			if (id != null)
				return (TransactionImple) _transactions.get(id);
			else
				return null;
		}
		catch (Exception e)
		{
			return new TransactionImple(null);
		}
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

    protected TransactionImple()
	{
		this(null);
	}

	/**
	 * Create a new TransactionImple representation of a specified transaction.
	 */

	protected TransactionImple(BasicAction curr)
	{
		try
		{
			if (curr == null)
			{
				_theTransaction = (com.arjuna.ats.arjuna.AtomicAction) BasicAction
						.Current();
			}
			else
				_theTransaction = (com.arjuna.ats.arjuna.AtomicAction) curr;
		}
		catch (ClassCastException ex)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn("com.arjuna.ats.internal.jta.transaction.arjunacore.notatomicaction");
			}
		}

		if (_theTransaction != null)
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
		_xaTransactionTimeoutEnabled = getXATransactionTimeoutEnabled();
	}

	final com.arjuna.ats.arjuna.AtomicAction getAtomicAction()
	{
		return _theTransaction;
	}

	/**
	 * Does the same as commit, but also changes the thread-to-tx association.
	 */

	protected void commitAndDisassociate()
			throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.commitAndDisassociate");
		}

		try
		{
			if (_theTransaction != null)
			{
				switch (_theTransaction.status())
				{
					case ActionStatus.ABORTED:
					case ActionStatus.ABORTING:
						_theTransaction.abort(); // assure thread disassociation
						throw new javax.transaction.RollbackException(
								jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));

					case ActionStatus.COMMITTED:
					case ActionStatus.COMMITTING: // in case of async commit
						_theTransaction.commit(true); // assure thread disassociation
						return;
				}

				switch (_theTransaction.commit(true))
				{
					case ActionStatus.COMMITTED:
					case ActionStatus.COMMITTING: // in case of async commit
						break;
					case ActionStatus.H_MIXED:
						throw new javax.transaction.HeuristicMixedException();
					case ActionStatus.H_HAZARD:
						throw new javax.transaction.HeuristicMixedException();
					case ActionStatus.H_ROLLBACK:
					case ActionStatus.ABORTED:
					case ActionStatus.ABORTING:
                        RollbackException rollbackException = new RollbackException(jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.commitwhenaborted"));
                        if(_rollbackOnlyCallerStacktrace != null) {
                            // we rolled back beacuse the user explicitly told us not to commit. Attach the trace of who did that for debug:
                            rollbackException.initCause(_rollbackOnlyCallerStacktrace);
                        }
                        else if(_theTransaction.getDeferredThrowable() != null) {
                            // we tried to commit but it went wrong - attach the reason for debug:
							rollbackException.initCause(_theTransaction.getDeferredThrowable());
						}
						throw rollbackException;
					default:
						throw new InvalidTerminationStateException(
								jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.invalidstate"));
				}
			}
			else
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
		}
		finally
		{
			TransactionImple.removeTransaction(this);
		}
	}

	/**
	 * If this is an imported transaction (via JCA) then this will be the Xid we
	 * are pretending to be. Otherwise, it will be null.
	 *
	 * @return null if we are a local transaction, a valid Xid if we have been
	 *         imported.
	 */

	protected Xid baseXid()
	{
		return null;
	}

	/**
	 * Does the same as rollback, but also changes the thread-to-tx association.
	 */

	protected void rollbackAndDisassociate()
			throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.rollbackAndDisassociate");
		}

		try
		{
			boolean statusIsValid = false;

			if (_theTransaction != null)
			{
				if(_theTransaction.status() == ActionStatus.RUNNING || _theTransaction.status() == ActionStatus.ABORT_ONLY) {
					// in these cases we may be able to finish without throwing an exception, if nothing else goes wrong...
					statusIsValid = true;
				}

				int outcome = _theTransaction.abort(); // assure thread disassociation, even if tx is already done.

				switch (outcome)
				{
					case ActionStatus.ABORTED:
					case ActionStatus.ABORTING: // in case of async rollback
					    statusIsValid = true;
						break;
					default:
						throw new InactiveTransactionException(
								jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.rollbackstatus")
										+ ActionStatus.stringForm(outcome));
				}
			}

			if(_theTransaction == null || !statusIsValid) {
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.arjunacore.inactive"));
			}
		}
		finally
		{
			TransactionImple.removeTransaction(this);
		}
	}

	/**
	 * If there are any suspended RMs then we should call end on them before the
	 * transaction is terminated.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.xaenderror
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.xaenderror]
	 *          Could not call end on a suspended resource!
	 */

	protected boolean endSuspendedRMs()
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
					/*
					 * Would it gain us much to just loop for _suspendCount?
					 */

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
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N
								.warn("com.arjuna.ats.internal.jta.transaction.arjunacore.xaenderror");
					}

					result = false;
				}
			}

			/*
			 * need to do the same for all duplicated resources
			 */

			el = _duplicateResources.keys();

			if (el != null)
			{
				try
				{
					/*
					 * Would it gain us much to just loop for _suspendCount?
					 */

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
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N
								.warn("com.arjuna.ats.internal.jta.transaction.arjunacore.xaenderror");
					}

					result = false;
				}
			}

			_suspendCount = 0;
		}

		return result;
	}

	/*
	 * If this thread has already registered a resource for the same db then
	 * don't use this copy. For some databases it would actually be ok for us to
	 * use the resource (at least to do an xa_start equivalent on it), but for
	 * Oracle 8.1.6 it causes their JDBC driver to crash!
	 */

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.threadexception
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.threadexception]
	 *          Caught the following error: {0}
	 */

	private final boolean threadIsActive(XAResource xaRes)
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
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.threadexception",
								new Object[]
								{ e });
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}

		return false;
	}

	/**
	 * isNewRM returns an existing TxInfo for the same RM, if present. Null
	 * otherwise.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.newtmerror
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.newtmerror]
	 *          {0} caught XAException: {0}
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.isnewrm
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.isnewrm]
	 *          Caught unexpected exception: {0}
	 */

	private final TxInfo isNewRM(XAResource xaRes)
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
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.newtmerror",
								new Object[]
								{ "TransactionImple.isNewRM",
										XAHelper.printXAErrorCode(ex) });
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(ex.toString(), ex);
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.newtmerror",
								new Object[]
								{ e });
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
		}

		return null;
	}

	private final Xid createXid(boolean branch, XAModifier theModifier)
	{
		Xid xid = baseXid();

		if (xid != null)
			return xid;

		xid = new XidImple(_theTransaction, branch);

		if (theModifier != null)
		{
			try
			{
				xid = theModifier.createXid((XidImple) xid);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return xid;
	}

	/*
	 * This method calls setRollbackOnly and catches any exceptions it may throw
	 * and issues a warning. We use this in places where we need to force the
	 * outcome of the transaction but already have an exception to throw back to
	 * the application, so a failure here will only be masked.
	 */

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.arjunacore.markrollback
	 *          [com.arjuna.ats.internal.jta.transaction.arjunacore.markrollback]
	 *          {0} - could not mark {0} as rollback only
	 */

	private final void markRollbackOnly()
	{
		try
		{
			setRollbackOnly();
		}
		catch (Exception ex)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.transaction.arjunacore.markrollback",
								new Object[]
								{ "TransactionImple.markRollbackOnly",
										_theTransaction });
			}
		}
	}

	/*
	 * Add and remove transactions from list.
	 */

	static final protected void putTransaction(TransactionImple tx)
	{
		_transactions.put(tx.get_uid(), tx);
	}

	static final protected void removeTransaction(TransactionImple tx)
	{
		_transactions.remove(tx.get_uid());
	}

	private static boolean getXATransactionTimeoutEnabled()
	{
		return XA_TRANSACTION_TIMEOUT_ENABLED;
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
        return TransactionReaper.transactionReaper().getRemainingTimeoutMills(_theTransaction);
    }

    public java.util.Map<Uid, String> getSynchronizations()
    {
        if (_theTransaction != null)
            return _theTransaction.getSynchronizations();

        return Collections.EMPTY_MAP;
    }

    protected com.arjuna.ats.arjuna.AtomicAction _theTransaction;

	private Hashtable _resources;

	private Hashtable _duplicateResources;

	private int _suspendCount;

	private final boolean _xaTransactionTimeoutEnabled;

	private Map _txLocalResources;

    private Throwable _rollbackOnlyCallerStacktrace;

	private static final boolean XA_TRANSACTION_TIMEOUT_ENABLED;

	private static final Class LAST_RESOURCE_OPTIMISATION_INTERFACE;

	static
	{
        XA_TRANSACTION_TIMEOUT_ENABLED = jtaPropertyManager.getJTAEnvironmentBean().isXaTransactionTimeoutEnabled();

		final String lastResourceOptimisationInterfaceName = jtaPropertyManager.getJTAEnvironmentBean().getLastResourceOptimisationInterface();
  		Class lastResourceOptimisationInterface = null;
		if (lastResourceOptimisationInterfaceName != null)
		{
			try
			{
				lastResourceOptimisationInterface = Thread.currentThread()
						.getContextClassLoader().loadClass(
								lastResourceOptimisationInterfaceName);
			}
			catch (final Throwable th)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn(
									"com.arjuna.ats.internal.jta.transaction.arjunacore.lastResourceOptimisationInterface",
									new Object[]
									{ lastResourceOptimisationInterfaceName },
									th);
				}
			}
		}
		LAST_RESOURCE_OPTIMISATION_INTERFACE = lastResourceOptimisationInterface;

	}

	private static ConcurrentHashMap _transactions = new ConcurrentHashMap();

}
