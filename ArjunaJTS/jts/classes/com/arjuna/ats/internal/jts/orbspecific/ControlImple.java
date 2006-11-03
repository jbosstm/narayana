/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: ControlImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.arjuna.ats.internal.jts.*;

import com.arjuna.ats.arjuna.coordinator.BasicAction;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import java.util.*;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;

/*
 * Although a transaction may have a timeout associated with it,
 * this can only happen for a top-level transaction. This, combined
 * with the fact that the default timeout is 0 means that many (most?)
 * transactions will not have a timeout. So, rather than increase the
 * size of all of the transaction objects, we keep the information
 * separate in the TransactionReaper. (Since it already needs to have this
 * information anyway this is no extra burden.) It also means that we can
 * support non-JBoss transactions: if we were to add a new method to the
 * control (get_timeout, say) then this would be Arjuna specific.
 */

/**
 * An implementation of CosTransactions::Control
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ControlImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ControlImple extends com.arjuna.ArjunaOTS.ActionControlPOA
{

	/**
	 * Create a new instance with the specified parent.
	 */

	public ControlImple (Control parentCon, ArjunaTransactionImple parentTran)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::ControlImple ( Control parentCon, "
					+ ((parentTran != null) ? parentTran.get_uid()
							: Uid.nullUid()) + " )");
		}

		_theTerminator = null;
		_theCoordinator = null;
		_parentControl = parentCon;
		_transactionHandle = new ArjunaTransactionImple(_parentControl,
				parentTran);
		_theUid = _transactionHandle.get_uid();
		_transactionImpl = null;
		_myControl = null;
		_destroyed = false;

		/*
		 * Pass a pointer to the control to the transaction so it knows what the
		 * control is. We use this for transaction comparison and
		 * thread-to-context management.
		 */

		_transactionHandle.setControlHandle(this);

		addControl();
	}

	public void finalize () throws Throwable
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple.finalize ()");
		}

		if (!_destroyed)
		{
			try
			{
				destroy();
			}
			catch (Exception e)
			{
			}
		}

		tidyup();

		/*
		 * Do this here rather than in tidyup so anyone else with a reference to
		 * this control can continue to determine the status of the transaction
		 * until the control is garbage collected.
		 */

		_theTerminator = null;
		_theCoordinator = null;

		_theUid = null;
	}

	/**
	 * Used for garbage collection so we can keep a list of controls and delete
	 * local ones.
	 */

	public Uid get_uid ()
	{
		return _theUid;
	}

	/**
	 * @return the transaction implementation.
	 */

	public final ArjunaTransactionImple getImplHandle ()
	{
		return _transactionHandle;
	}

	/**
	 * @return the CORBA Control object.
	 */

	public final synchronized Control getControl ()
	{
		/*
		 * If we have been committed then the reference will be null. There is
		 * no point in recreating it and in some cases (e.g., JacORB) this will
		 * in fact cause an exception to be thrown.
		 */

		if ((_myControl == null) && (!_destroyed))
		{
			ORBManager.getPOA().objectIsReady(this);

			_myControl = com.arjuna.ArjunaOTS.ActionControlHelper.narrow(ORBManager.getPOA().corbaReference(this));
		}

		/*
		 * In C++ we had to narrow to Control for some ORBs, despite the fact
		 * that an ArjunaControl is a Control. Does now seem to be necessary for
		 * Java.
		 * 
		 * return ControlHelper.narrow(_myControl);
		 */

		return _myControl;
	}

	public Terminator get_terminator () throws SystemException,
			org.omg.CosTransactions.Unavailable
	{
		if ((_transactionHandle != null) && (_theTerminator == null))
			createTransactionHandle();

		if (_theTerminator != null)
			return _theTerminator;
		else
			throw new Unavailable();
	}

	public Coordinator get_coordinator () throws SystemException,
			org.omg.CosTransactions.Unavailable
	{
		if ((_transactionHandle != null) && (_theCoordinator == null))
			createTransactionHandle();

		if (_theCoordinator != null)
			return _theCoordinator;
		else
			throw new Unavailable();
	}

	public void set_terminator (Terminator terminator) throws SystemException,
			org.omg.CosTransactions.Unavailable
	{
		throw new org.omg.CosTransactions.Unavailable();
	}

	public void set_coordinator (Coordinator coordinator)
			throws SystemException, org.omg.CosTransactions.Unavailable
	{
		throw new org.omg.CosTransactions.Unavailable();
	}

	public Control getParentControl () throws Unavailable, SystemException
	{
		if (_parentControl != null)
			return _parentControl;
		else
			return null;
	}

	/**
	 * destroy should only be called for remote Control objects. Destroy them
	 * locally by calling DESTROY_IMPL.
	 * 
	 * Since we assume that a factory will either be remote or local, we can
	 * destroy this object and rely upon the ORB to return an exception to
	 * subsequent clients which indicates they no longer have a valid reference.
	 * 
	 * @message com.arjuna.ats.internal.jts.orbspecific.destroyfailed could not
	 *          destroy object:
	 */

	public synchronized void destroy () throws ActiveTransaction,
			ActiveThreads, BadControl, Destroyed, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "Control::destroy called for "
					+ get_uid());
		}

		canDestroy();

		try
		{
			_destroyed = true;

			removeControl();

			/*
			 * If this is a proxy then there won't be a local transaction
			 * implementation.
			 */

			if (_transactionHandle != null)
			{
				_transactionHandle.setControlHandle(null); // for gc
				_transactionHandle = null;
			}

			/*
			 * We do a lazy connect to the ORB, so we may never have to do a
			 * disconnect either.
			 */

			if (_myControl != null)
			{
				ORBManager.getPOA().shutdownObject(this);
				_myControl = null;
			}

			tidyup();
		}
		catch (Exception e)
		{
			e.printStackTrace();

			throw new BAD_OPERATION(
					"ControlImple "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.destroyfailed")
							+ e);
		}
	}

	public ControlImple getParentImple ()
	{
		BasicAction parent = ((_transactionHandle != null) ? _transactionHandle.parent()
				: null);

		if (parent != null)
		{
			try
			{
				synchronized (ControlImple.allControls)
				{
					return (ControlImple) ControlImple.allControls.get(parent.get_uid());
				}
			}
			catch (Exception ex)
			{
				return null;
			}
		}
		else
			return null;
	}

	public String toString ()
	{
		return "ControlImple < " + get_uid() + " >";
	}

	public boolean equals (java.lang.Object obj)
	{
		if (obj instanceof ControlImple)
		{
			if (((ControlImple) obj).get_uid().equals(get_uid()))
				return true;
		}

		return false;
	}

	protected synchronized void canDestroy () throws ActiveTransaction,
			ActiveThreads, BadControl, Destroyed, SystemException
	{
		canDestroy(true);
	}

	/**
	 * Generally we do not want to destroy the transaction if it is doing some
	 * work, or other threads are still registered with it. However, for some
	 * situations (e.g., the transaction reaper) we must terminate the
	 * transaction regardless.
	 */

	protected synchronized void canDestroy (boolean force)
			throws ActiveTransaction, ActiveThreads, BadControl, Destroyed,
			SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "Control::canDestroy ( "
					+ force + " ) called for " + get_uid());
		}

		if (_destroyed)
			throw new Destroyed();

		if (_transactionHandle != null) // not a proxy control.
		{
			if ((_transactionHandle.activeThreads() != 0) && (!force))
			{
				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::canDestroy for "
							+ get_uid()
							+ " - transaction has "
							+ _transactionHandle.activeThreads()
							+ " active threads.");
				}

				throw new ActiveThreads();
			}

			boolean active = false;

			try
			{
				if ((force)
						|| ((_transactionHandle.status() == ActionStatus.CREATED)
								|| (_transactionHandle.status() == ActionStatus.ABORTED) || (_transactionHandle.status() == ActionStatus.COMMITTED)))
				{
					active = false;
				}
				else
					active = true; // might be committing, aborting, etc.
			}
			catch (Exception e)
			{
				active = true;
			}

			if (active)
			{
				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "Control::canDestroy for "
							+ get_uid() + " - transaction active.");
				}

				throw new ActiveTransaction();
			}
		} // it is always safe to delete proxies.

		/*
		 * Got here, so it is either ok to destroy or the caller wants to force
		 * the destruction regardless.
		 */
	}

	/**
	 * This is used for implicit context propagation, and for Current.resume on
	 * remote transactions. In both cases we need to create a local control
	 * given a remove coordinator and terminator, but we can't create a
	 * transaction handle.
	 */

	protected ControlImple (Coordinator coordinator, Terminator terminator)
	{
		this(coordinator, terminator, null, null);
	}

	protected ControlImple (Coordinator coordinator, Terminator terminator, Uid uid)
	{
		this(coordinator, terminator, null, uid);
	}

	protected ControlImple (Coordinator coordinator, Terminator terminator, Control parentControl, Uid uid)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::ControlImple (Coordinator, Terminator, Control, "
					+ uid + " )");
		}

		_theTerminator = terminator;
		_theCoordinator = coordinator;
		_parentControl = parentControl;
		_transactionHandle = null;
		_transactionImpl = null;
		_myControl = null;
		_destroyed = false;

		if (uid == null)
		{
			UidCoordinator uidCoord = Helper.getUidCoordinator(coordinator);

			if (uidCoord != null)
			{
				try
				{
					_theUid = Helper.getUid(uidCoord);
				}
				catch (Exception e)
				{
					/*
					 * Not an JBoss transaction, so allocate any Uid.
					 */

					_theUid = new Uid();
				}

				uidCoord = null;
			}
			else
				_theUid = new Uid();
		}
		else
			_theUid = uid;

		duplicateTransactionHandle(coordinator, terminator);

		addControl();
	}

	/**
	 * Protected constructor for inheritance. The derived classes are
	 * responsible for setting everything up, including adding the control to
	 * the list of controls and assigning the Uid variable.
	 */

	protected ControlImple ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::ControlImple ()");
		}

		_theTerminator = null;
		_theCoordinator = null;
		_parentControl = null;
		_transactionHandle = null;
		_theUid = Uid.nullUid();
		_transactionImpl = null;
		_myControl = null;
		_destroyed = false;
	}

	protected final void createTransactionHandle ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::createTransactionHandle ()");
		}

		/* Create/bind the 2 IDL interfaces to the same implementation */

		_transactionImpl = new com.arjuna.ArjunaOTS.ArjunaTransactionPOATie(
				_transactionHandle);

		ORBManager.getPOA().objectIsReady(_transactionImpl);

		ArjunaTransaction transactionReference = com.arjuna.ArjunaOTS.ArjunaTransactionHelper.narrow(ORBManager.getPOA().corbaReference(_transactionImpl));

		_theCoordinator = com.arjuna.ArjunaOTS.UidCoordinatorHelper.narrow(transactionReference);
		_theTerminator = org.omg.CosTransactions.TerminatorHelper.narrow(transactionReference);

		transactionReference = null;
	}

	protected final void duplicateTransactionHandle (Coordinator coord, Terminator term)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::duplicateTransactionHandle ()");
		}

		_theCoordinator = coord;
		_theTerminator = term;
	}

	/**
	 * Transaction needs to call these methods to enable garbage collection to
	 * occur.
	 */

	protected boolean addControl ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::addControl ()");
		}

		try
		{
			synchronized (ControlImple.allControls)
			{
				ControlImple.allControls.put(get_uid(), this);
			}
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	protected boolean removeControl ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::removeControl ()");
		}

		try
		{
			synchronized (ControlImple.allControls)
			{
				ControlImple.allControls.remove(get_uid());
			}
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	/**
	 * No need to protect with mutex since only called from destroy (and
	 * destructor), which is protected with a mutex. Do not call directly
	 * without synchronizing.
	 * 
	 * @message com.arjuna.ats.internal.jts.orbspecific.tidyfail {0} attempt to
	 *          clean up failed with: {1}
	 */

	protected final void tidyup ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ControlImple::tidyup ()");
		}

		_myControl = null;
		_parentControl = null;

		try
		{
			if (_transactionImpl != null)
			{
				ORBManager.getPOA().shutdownObject(_transactionImpl);

				_transactionHandle = null;
				_transactionImpl = null;
			}

		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.threaderror", new Object[]
				{ "ControlImple.tidyup", e });
			}
		}
	}

	/*
	 * Make private, with public accessor.
	 */

	public static Hashtable allControls = new Hashtable();

	protected Terminator _theTerminator;

	protected Coordinator _theCoordinator;

	protected Control _parentControl;

	protected ArjunaTransactionImple _transactionHandle;

	protected Uid _theUid;

	protected com.arjuna.ArjunaOTS.ActionControl _myControl;

	protected com.arjuna.ArjunaOTS.ArjunaTransactionPOATie _transactionImpl;

	protected boolean _destroyed;

}
