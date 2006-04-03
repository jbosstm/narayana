/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: CurrentImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.*;
import com.arjuna.ats.internal.jts.context.ContextManager;
import com.arjuna.ats.internal.jts.coordinator.CheckedActions;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.ats.internal.arjuna.template.*;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;

import com.arjuna.common.util.logging.*;

import java.io.PrintStream;
import java.util.*;
import java.lang.Object;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.*;

import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.NO_MEMORY;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import java.lang.OutOfMemoryError;
import java.util.EmptyStackException;

/**
 * The implementation of CosTransactions::Current.
 * 
 * In a multi-threaded environment where threads can terminate transactions they
 * may not have started, then it is possible for a thread to call
 * commit/rollback/rollback_only on a transaction which has already been (or is
 * in the process of being) terminated. We assume that the subsequent thread is
 * still associated with the transaction so that it can determine its status,
 * rather than simply disassociate it when it tries to terminate and return
 * NoTransaction, so we would return INVALID_TRANSACTION. This allows us to
 * distinguish between the situation where there really is no transaction
 * associated with the thread.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CurrentImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class CurrentImple extends LocalObject implements
		org.omg.CosTransactions.Current, com.arjuna.ats.jts.extensions.Current
{

	public static final int TX_BEGUN = 0;
	public static final int TX_COMMITTED = 1;
	public static final int TX_ABORTED = 2;
	public static final int TX_SUSPENDED = 3;
	public static final int TX_RESUMED = 4;

	public CurrentImple ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::CurrentImple ()");
		}

		_theManager = new ContextManager();
	}

	public void begin () throws SubtransactionsUnavailable, SystemException
	{
		ControlWrapper currentAction = _theManager.current();

		if (currentAction == null) // no current, so create top-level action
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::begin - creating new top-level transaction.");
			}

			if (OTSImpleManager.localFactory())
				currentAction = new ControlWrapper(
						OTSImpleManager.factory().createLocal(get_timeout()));
			else
				currentAction = new ControlWrapper(
						OTSImpleManager.get_factory().create(get_timeout()));
		}
		else
		{
			if (jtsLogger.logger.isDebugEnabled())
			{
				jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::begin - creating new subtransaction.");
			}

			/*
			 * If the current transaction has terminated (by another thread)
			 * then we could simply start a new top-level transaction. However,
			 * the application may be assuming that the transaction returned by
			 * begin is a subtransaction. So, we throw INVALID_TRANSACTION.
			 */

			try
			{
				currentAction = currentAction.create_subtransaction();
			}
			catch (Unavailable ex)
			{
				throw new INVALID_TRANSACTION(
						ExceptionCodes.UNAVAILABLE_COORDINATOR,
						CompletionStatus.COMPLETED_NO);
			}
			catch (Inactive e)
			{
				throw new INVALID_TRANSACTION(
						ExceptionCodes.INACTIVE_TRANSACTION,
						CompletionStatus.COMPLETED_NO);
			}
			catch (NO_MEMORY nme)
			{
				System.gc();

				throw nme;
			}
			catch (SystemException sysEx)
			{
				throw new INVALID_TRANSACTION(
						ExceptionCodes.INACTIVE_TRANSACTION,
						CompletionStatus.COMPLETED_NO);
			}
			catch (OutOfMemoryError me)
			{
				System.gc();

				throw new NO_MEMORY(0, CompletionStatus.COMPLETED_NO);
			}
		}

		_theManager.pushAction(currentAction);

		try
		{
			ThreadAssociationControl.updateAssociation(currentAction, TX_BEGUN);
		}
		catch (Exception e)
		{
			/*
			 * An error happened, so mark the transaction as rollback only (in
			 * case it hasn't already been so marked.)
			 */

			try
			{
				rollback_only();
			}
			catch (Exception ex)
			{
			}
		}

		currentAction = null;
	}

	/**
	 * It's not possible to commit/abort out of order using the current
	 * interface.
	 * 
	 * Do we delete the control if the transaction gives an heuristic result?
	 * CurrentImplely we do.
	 * 
	 * If another thread has already terminated the transaction then: (i) if it
	 * committed, we do nothing - could throw TransactionRequired of
	 * INVALID_TRANSACTION, or NoTransaction. Probably not NoTransaction, since
	 * it would be better to distinguish between the situation where the
	 * transaction has already been terminated and there really is no
	 * transaction for this thread. (ii) if it rolledback, we throw
	 * TRANSACTION_ROLLEDBACK.
	 *  
	 */

	public void commit (boolean report_heuristics) throws NoTransaction,
			HeuristicMixed, HeuristicHazard, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::commit ( "
					+ report_heuristics + " )");
		}

		ControlWrapper currentAction = _theManager.current();

		if (currentAction != null)
		{
			try
			{
				ThreadAssociationControl.updateAssociation(currentAction, TX_COMMITTED);
			}
			catch (Exception e)
			{
				/*
				 * An error happened, so mark the transaction as rollback only
				 * (in case it hasn't already been so marked.)
				 */

				rollback_only();
			}

			/*
			 * Note: we only destroy the control if we do not get an exception.
			 * This lets the user see what happened, and relies upon them to
			 * later destroy the control.
			 */

			try
			{
				currentAction.commit(report_heuristics);

				_theManager.popAction();
			}
			catch (TRANSACTION_ROLLEDBACK e1)
			{
				/*
				 * Is ok to destroy transaction. Different for heuristics.
				 */

				_theManager.popAction();

				throw e1;
			}
			catch (HeuristicMixed e2)
			{
				_theManager.popAction();

				throw e2;
			}
			catch (HeuristicHazard e3)
			{
				_theManager.popAction();

				throw e3;
			}
			catch (SystemException e4)
			{
				_theManager.popAction();

				throw e4;
			}
			catch (Unavailable e5)
			{
				/*
				 * If terminated by some other thread then the reference we have
				 * will no longer be valid.
				 */

				_theManager.popAction();

				throw new INVALID_TRANSACTION();
			}
		}
		else
			throw new NoTransaction();
	}

	/**
	 * If another thread has already terminated the transaction then: (i) if it
	 * rolled back, we do nothing - could throw TransactionRequired of
	 * INVALID_TRANSACTION, or NoTransaction. Probably not NoTransaction, since
	 * it would be better to distinguish between the situation where the
	 * transaction has already been terminated and there really is no
	 * transaction for this thread. (ii) if it committed, we throw
	 * INVALID_TRANSACTION.
	 */

	public void rollback () throws NoTransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::rollback ()");
		}

		ControlWrapper currentAction = _theManager.current();

		if (currentAction != null)
		{
			ThreadAssociationControl.updateAssociation(currentAction, TX_ABORTED);

			try
			{
				currentAction.rollback();

				_theManager.popAction();
			}
			catch (INVALID_TRANSACTION e1)
			{
				/*
				 * If transaction has already terminated, then throw
				 * INVALID_TRANSACTION. Differentiates between this stat and not
				 * actually having a transaction associated with the thread.
				 */

				_theManager.popAction();

				throw e1;
			}
			catch (SystemException e2)
			{
				_theManager.popAction();

				throw e2;
			}
			catch (Unavailable e)
			{
				/*
				 * If no terminator then not allowed!
				 */

				_theManager.popAction();

				throw new INVALID_TRANSACTION();
			}
		}
		else
			throw new NoTransaction();
	}

	/**
	 * If the transaction has already terminated (or is being terminated) then
	 * throw INVALID_TRANSACTION.
	 */

	public void rollback_only () throws NoTransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::rollback_only ()");
		}

		ControlWrapper currentAction = _theManager.current();

		if (currentAction != null)
		{
			try
			{
				currentAction.rollback_only();
			}
			catch (org.omg.CosTransactions.Inactive exc)
			{
				throw new INVALID_TRANSACTION(
						ExceptionCodes.INACTIVE_TRANSACTION,
						CompletionStatus.COMPLETED_NO);
			}
			catch (SystemException e)
			{
				throw e;
			}
			catch (Unavailable ex)
			{
				throw new NoTransaction();
			}
		}
		else
			throw new NoTransaction();
	}

	public org.omg.CosTransactions.Status get_status () throws SystemException
	{
		ControlWrapper currentAction = _theManager.current();
		org.omg.CosTransactions.Status stat = ((currentAction == null) ? org.omg.CosTransactions.Status.StatusNoTransaction
				: currentAction.get_status());

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::get_status - returning "
					+ Utility.stringStatus(stat));
		}

		return stat;
	}

	public String get_transaction_name () throws SystemException
	{
		ControlWrapper currentAction = _theManager.current();
		String ch = ((currentAction == null) ? "null"
				: currentAction.get_transaction_name());

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::get_transaction_name - returning "
					+ ch);
		}

		return ch;
	}

	public synchronized void set_timeout (int seconds) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::set_timeout ( "
					+ seconds + " )");
		}

		/*
		 * Only bother if the value is anything other than the default.
		 */

		if (seconds > 0)
		{
			otsTransactionTimeout.set(new Integer(seconds));
		}
		else
		{
			if (seconds < 0)
			{
				throw new BAD_PARAM(ExceptionCodes.INVALID_TIMEOUT,
						CompletionStatus.COMPLETED_NO);
			}
			else
			{
				otsTransactionTimeout.set(null);
			}
		}
	}

	public final synchronized int get_timeout () throws SystemException
	{
		Integer value = (Integer) otsTransactionTimeout.get();
		int v = 0; // if not set then assume 0. What else can we do?

		if (value != null)
		{
			v = value.intValue();
		}

		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::get_timeout - returning "
					+ v);
		}

		return v;
	}

	public void setCheckedAction (CheckedAction ca) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::setCheckedAction ( "
					+ ca + " )");
		}

		CheckedActions.set(ca);
	}

	public CheckedAction getCheckedAction () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::getCheckedAction ()");
		}

		return CheckedActions.get();
	}

	public org.omg.CosTransactions.Control get_control ()
			throws SystemException
	{
		ControlWrapper theControl = _theManager.current();

		if (theControl == null)
			return null;

		/*
		 * Always return a Control with a null terminator? Forces users to go
		 * through current, and attempts to stop explicit propagation, which
		 * prevents fully checked transactions. By default we don't do that
		 * because explicit transaction propagation is the only means of
		 * propagation we can guarantee between Orbs.
		 */

		if (true)
		{
			/*
			 * If it's a locally created control then we may not have registered
			 * it with the ORB yet, so do so now.
			 */

			try
			{
				return theControl.get_control();
			}
			catch (Unavailable e)
			{
				return null;
			}
		}
		else
		{
			Coordinator coord = null;

			try
			{
				coord = theControl.get_coordinator();
			}
			catch (Unavailable e)
			{
				coord = null;

				throw new UNKNOWN(ExceptionCodes.UNAVAILABLE_COORDINATOR,
						CompletionStatus.COMPLETED_NO);
			}
			catch (SystemException sysEx)
			{
				coord = null;

				throw sysEx;
			}

			org.omg.CosTransactions.Control proxyControl = TransactionFactoryImple.createPropagatedControl(coord);

			coord = null;
			theControl = null;

			return proxyControl;
		}
	}

	/*
	 * Problem: there is a general problem with the Orb and memory management.
	 * If this method, say, is invoked remotely then we must duplicate the
	 * reference before returning it since the Orb will call release on the
	 * return value once it has been sent to the caller. However, in the local
	 * case, if we call duplicate then there is nothing to call release and we
	 * get memory leaks!
	 * 
	 * Also assume that BasicAction's notion of current is the same as
	 * CurrentImple's, if the action is local.
	 *  
	 */

	/**
	 * The spec. states that after suspend we should have a null transaction
	 * context, but is hazy as to what to do if we are nested. We shall assume
	 * that the control returned is for the current transaction and that we
	 * suspend the entire transaction hierarchy. Given the returned control, we
	 * can always regenerate the hierarchy later if required by resume.
	 */

	public org.omg.CosTransactions.Control suspend () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::suspend ()");
		}
		
		ControlWrapper actPtr = _theManager.popAction();

		if (actPtr == null)
		{
			ThreadAssociationControl.updateAssociation(null, TX_SUSPENDED);

			return null;
		}
		else
		{
			ThreadAssociationControl.updateAssociation(actPtr, TX_SUSPENDED);

			/*
			 * Purge the remaining controls from the thread context. If the
			 * controls are remote and proxies then we delete them here, since
			 * we must recreate them next time we want to use them anyway.
			 */

			_theManager.purgeActions();

			if (actPtr.isLocal())
				return actPtr.getImple().getControl();
			else
				return actPtr.getControl();
		}
	}

	/**
	 * To support checked transactions we can only resume if the action is local
	 * or we received it implicitly.
	 * 
	 * If the control refers to a nested transaction then we must recreate the
	 * entire hierarchy, i.e., the effect of a suspend/resume on the same
	 * control should be the same as never calling suspend in the first place.
	 * 
	 * If the control is for a local transaction then it is simple to recreate
	 * the hierarchy. Otherwise we rely upon the PropagationContext to recreate
	 * it.
	 * 
	 * If this control is a "proxy" then create a new proxy instance, so we can
	 * delete proxies whenever suspend is called.
	 * 
	 * Should check if "new" transaction is not actually the current one anyway.
	 * If so, just return. The spec. doesn't mention what to do in this case, so
	 * for now we go to the overhead of the work regardless.
	 */

	public void resume (Control which) throws InvalidControl, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::resume ( "
					+ which + " )");
		}

		/*
		 * We must now "forget" any current transaction information. This is
		 * because when we end this transaction we must be associated with no
		 * transaction.
		 */

		_theManager.purgeActions();

		if (which == null) // if null then return
		{
			ThreadAssociationControl.updateAssociation(null, TX_RESUMED);

			return;
		}

		/*
		 * Must duplicate because it is an 'in' parameter which we want to keep.
		 */

		org.omg.CosTransactions.Control cont = which;
		boolean invalidControl = false;

		/*
		 * Now recreate the hierarchy (if any) of this transaction, pushing each
		 * control onto the stack. The method pushAction will push BasicActions
		 * onto the thread stack if necessary, so we don't need to worry about
		 * it here.
		 */

		try
		{
			Coordinator coord = cont.get_coordinator();

			if (!coord.is_top_level_transaction())
			{
				/*
				 * Is the Control an ActionControl? If so then it has methods to
				 * allow us to get the parent directly. Otherwise, rely on the
				 * PropagationContext.
				 */

				ActionControl actControl = null;

				try
				{
					actControl = com.arjuna.ArjunaOTS.ActionControlHelper.narrow(cont);

					if (actControl == null)
						throw new BAD_PARAM();
				}
				catch (Exception e)
				{
					/*
					 * Not an ActionControl.
					 */

					actControl = null;
				}

				if (actControl != null)
				{
					invalidControl = _theManager.addActionControlHierarchy(actControl);
				}
				else
				{
					invalidControl = _theManager.addRemoteHierarchy(cont);
				}
			}

			coord = null;
		}
		catch (OBJECT_NOT_EXIST one)
		{
			//	    throw new InvalidControl();
		}
		catch (UNKNOWN ue) // JacORB 1.4.5 bug
		{
		}
		catch (org.omg.CORBA.OBJ_ADAPTER oae) // JacORB 2.0 beta 2 bug
		{
		}
		catch (SystemException sysEx)
		{
			throw new InvalidControl();
		}
		catch (UserException usrEx)
		{
			throw new InvalidControl();
		}
		catch (NullPointerException npx)
		{
			throw new InvalidControl();
		}
		catch (Exception ex)
		{
			throw new BAD_OPERATION("CurrentImple.resume: " + ex.toString());
		}

		/*
		 * Now put the new transaction on the top of the list.
		 */

		try
		{
			if (!invalidControl)
			{
				ControlWrapper wrap = new ControlWrapper(cont);

				ThreadAssociationControl.updateAssociation(wrap, TX_RESUMED);

				_theManager.pushAction(wrap);
			}
		}
		catch (NullPointerException npx)
		{
			invalidControl = true;
		}

		cont = null;

		if (invalidControl)
			throw new InvalidControl();
	}

	/**
	 * Returns the internal context manager implementation. Applications should
	 * not use this method.
	 * 
	 * @since JTS 2.1.
	 */

	public final ContextManager contextManager ()
	{
		return _theManager;
	}

	public void resumeImple (ControlImple which) throws InvalidControl,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::resumeImple ( "
					+ which + " )");
		}

		/*
		 * We must now "forget" any current transaction information. This is
		 * because when we end this transaction we must be associated with no
		 * transaction.
		 */

		_theManager.purgeActions();

		if (which == null) // if null then return
		{
			ThreadAssociationControl.updateAssociation(null, TX_RESUMED);

			return;
		}

		boolean invalidControl = _theManager.addControlImpleHierarchy(which);

		/*
		 * Now put the new transaction on the top of the list.
		 */

		try
		{
			if (!invalidControl)
			{
				ControlWrapper wrap = new ControlWrapper(which);

				ThreadAssociationControl.updateAssociation(wrap, TX_RESUMED);

				_theManager.pushAction(wrap);
			}
		}
		catch (NullPointerException npx)
		{
			invalidControl = true;
		}

		if (invalidControl)
			throw new InvalidControl();
	}

	public void resumeWrapper (ControlWrapper which) throws InvalidControl,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::resumeWrapper ( "
					+ which + " )");
		}

		if (which.isLocal())
			resumeImple(which.getImple());
		else
			resume(which.getControl());
	}

	public ControlWrapper suspendWrapper () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple::suspendWrapper ()");
		}

		ControlWrapper actPtr = _theManager.popAction();

		if (actPtr == null)
		{
			ThreadAssociationControl.updateAssociation(null, TX_SUSPENDED);

			return null;
		}
		else
		{
			ThreadAssociationControl.updateAssociation(actPtr, TX_SUSPENDED);

			/*
			 * Purge the remaining controls from the thread context. If the
			 * controls are remote and proxies then we delete them here, since
			 * we must recreate them next time we want to use them anyway.
			 */

			_theManager.purgeActions();

			return actPtr;
		}
	}

	public ControlWrapper getControlWrapper () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "CurrentImple.getControlWrapper ()");
		}

		return _theManager.current();
	}

	protected static ContextManager _theManager = null;

	private static ThreadLocal otsTransactionTimeout = new ThreadLocal(); // thread
																		  // specific

}
