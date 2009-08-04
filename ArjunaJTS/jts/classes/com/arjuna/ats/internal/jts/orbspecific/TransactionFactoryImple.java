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
 * $Id: TransactionFactoryImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific;

import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.*;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.arjuna.ats.internal.jts.utils.TxStoreLog;

import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ArjunaOTS.*;

import org.omg.CosTransactions.*;

import java.util.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.NO_MEMORY;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import java.lang.OutOfMemoryError;

/**
 * An implementation of ArjunaOTS::ArjunaFactory.
 * 
 * Problem: garbage collection! If a user keeps a reference to a Control, say,
 * then we will delete the implementation object when the action terminates.
 * However, the user's reference is still valid, only the thing it points to is
 * no longer there. In the remote case this is ok as the Orb will raise an
 * exception. In the local case, however, the program is likely to crash when it
 * tries to dereference freed memory! There's nothing we can do about this
 * (unless we decide never to garbage collect!) apart from warn against using
 * Control, Coordinator, and Terminator explicitly - if you go via Current then
 * everything's ok.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TransactionFactoryImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * 
 * @message com.arjuna.ats.internal.jts.orbspecific.tficaught {0} for {1} caught
 *          exception: {2}
 * @message com.arjuna.ats.internal.jts.orbspecific.otiderror is not a valid
 *          unique identifier!
 */

public class TransactionFactoryImple extends
		com.arjuna.ArjunaOTS.ArjunaFactoryPOA
{

	public TransactionFactoryImple ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::TransactionFactoryImple ()");
		}

		_factoryRef = getReference();
	}

	public TransactionFactoryImple (String name)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::TransactionFactoryImple ( "
					+ name + " )");
		}

		_factoryRef = getReference();
	}

	public final synchronized TransactionFactory getReference ()
	{
		if (_factoryRef == null)
		{
			ORBManager.getPOA().objectIsReady(this);

			_factoryRef = org.omg.CosTransactions.TransactionFactoryHelper.narrow(ORBManager.getPOA().corbaReference(this));
		}

		return _factoryRef;
	}

	/**
	 * Assume that a value of 0 at the client means the same at the server!
	 */

	public Control create (int time_out) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::create ( "
					+ time_out + " )");
		}

		ControlImple tranControl = createLocal(time_out);

		return tranControl.getControl();
	}

	/**
	 * This creates a local instance of a transaction control, but does not
	 * register it with the ORB. Either call its getControl method directly, or
	 * use the create method of the factory.
	 */

	public ControlImple createLocal (int time_out) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::createLocal ( "
					+ time_out + " )");
		}

		try
		{
			ControlImple tranControl = new ControlImple((Control) null,
					(ArjunaTransactionImple) null);
			int theTimeout = time_out;

			if (theTimeout == 0)
			    theTimeout = TxControl.getDefaultTimeout();

			if (theTimeout > 0)
			{
				/*
				 * Currently we do not remove controls from the list once they
				 * have terminated. We should to save time and space!
				 */

				TransactionReaper reaper = TransactionReaper.transactionReaper();

				if (reaper == null)
					reaper = TransactionReaper.create();

				reaper.insert(new ControlWrapper((ControlImple) tranControl), theTimeout);
			}

			return tranControl;
		}
		catch (OutOfMemoryError e)
		{
			/*
			 * Rather than try again after running gc simply return and let the
			 * user deal with it. May help with memory!
			 */

			System.gc();

			throw new NO_MEMORY(0, CompletionStatus.COMPLETED_NO);
		}
	}

	/**
	 * In Arjuna we can do low-cost nested aborts at clients which do not
	 * involve telling servers. The server finds out the next time a call is
	 * made when it checks the hierarchy.
	 */

	public ControlImple recreateLocal (PropagationContext ctx)
			throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::recreateLocal ()");
		}

		if (ctx.current.coord == null) // nothing to use!!
			return null;

		/*
		 * Now take the propagation context and create a local proxy for it
		 * which matches.
		 * 
		 * We maintain a proxy for each top-level action. If we already have a
		 * proxy for this action, then pass it the context and let it figure out
		 * what the hierarchy should be - we may already have created it, or
		 * have most of it.
		 */

		/*
		 * For each type of transaction we know about, we maintain a creator
		 * function. This allows us to remain implementation neutral, while at
		 * the same time retaining flexibility: to support a new transaction
		 * type simple register a new creator function.
		 */

		return creators.recreateLocal(ctx, ctx.current.otid.formatID);
	}

	public Control recreate (PropagationContext ctx) throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::recreate ()");
		}

		return recreateLocal(ctx).getControl();
	}

	/**
	 * Non-idl methods, but we put them here because they are related to the
	 * work the factory does.
	 */

	public static Control create_subtransaction (Control control, ArjunaTransactionImple parent)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::create_subtransaction ( "
					+ control
					+ ", "
					+ ((parent != null) ? parent.get_uid() : Uid.nullUid())
					+ " )");
		}

		try
		{
			ControlImple subTranControl = new ControlImple(control, parent);

			return subTranControl.getControl();
		}
		catch (OutOfMemoryError e)
		{
			System.gc();

			throw new NO_MEMORY(0, CompletionStatus.COMPLETED_NO);
		}
	}

	public static Control createProxy (Coordinator coordinator, Terminator terminator)
	{
		return TransactionFactoryImple.createProxy(coordinator, terminator, null);
	}

	public static Control createProxy (Coordinator coordinator, Terminator terminator, Control parentControl)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::createProxy ( "
					+ coordinator
					+ ", "
					+ terminator
					+ ", "
					+ parentControl
					+ " )");
		}

		/*
		 * Different from the C++ version in that we can cache proxy
		 * implementations and reuse them, since we rely upon the Java garbage
		 * collection facility to remove them when all outstanding references
		 * have gone.
		 */

		/*
		 * If not an Arjuna UidCoordinator then we may have a memory leak since
		 * there is no way to remove this control.
		 */

		UidCoordinator uidCoord = Helper.getUidCoordinator(coordinator);
		Uid theUid = null;

		if (uidCoord != null)
		{
			try
			{
				theUid = Helper.getUid(uidCoord);

				/*
				 * allServerControls contains only the proxy implementations.
				 */

				if (ServerControl.allServerControls != null)
				{
					synchronized (ServerControl.allServerControls)
					{
						ControlImple c = (ControlImple) ServerControl.allServerControls.get(theUid);

						if (c != null)
							return c.getControl();
					}
				}
			}
			catch (Exception e)
			{
				/*
				 * Not a JBoss transaction, so allocate any Uid.
				 */

				theUid = new Uid();
			}

			uidCoord = null;
		}
		else
			theUid = new Uid();

		ControlImple proxy = new ControlImple(coordinator, terminator,
				parentControl, theUid);

		return proxy.getControl();
	}

	public static Control createPropagatedControl (Coordinator coord)
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::createPropagatedControl ( "
					+ coord + " )");
		}

		ControlImple proxyControl = new ControlImple(coord, null);

		return proxyControl.getControl();
	}

	/**
	 * Now methods to return the identities of the currently running
	 * transactions, and those which have terminated but left entries in the
	 * object store.
	 * 
	 * WARNING: these methods should be used sparingly since they *must* lock
	 * the transaction database while examining it, and this will prevent any
	 * new transactions from being created/started.
	 * 
	 * @param the
	 *            type of transaction (active, unresolved) to get data on.
	 * @since JTS 2.1.
	 */

	public org.omg.CosTransactions.otid_t[] numberOfTransactions (com.arjuna.ArjunaOTS.TransactionType t)
			throws Inactive, NoTransaction, SystemException
	{
		switch (t.value())
		{
		case com.arjuna.ArjunaOTS.TransactionType._TransactionTypeActive:
			return activeTransactions();
		case com.arjuna.ArjunaOTS.TransactionType._TransactionTypeUnresolved:
			return unresolvedTransactions();
		default:
			throw new BAD_OPERATION();
		}
	}

	/**
	 * @return the list of child transactions.
	 */

	public org.omg.CosTransactions.otid_t[] getChildTransactions (otid_t parent)
			throws Inactive, NoTransaction, SystemException
	{
		Uid u = Utility.otidToUid(parent);
		org.omg.CosTransactions.otid_t[] ctx = null;

		if (u == null)
			throw new BAD_PARAM(
					"otid_t "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.otiderror"));
		else
		{
			BasicAction act = ActionManager.manager().get(u);

			if (act == null)
				throw new NoTransaction();
			else
			{
				if (act.status() == ActionStatus.RUNNING)
				{
					Object[] children = act.childTransactions();
					int size = ((children == null) ? 0 : children.length);

					if (size > 0)
					{
						ctx = new org.omg.CosTransactions.otid_t[size];

						for (int i = 0; i < size; i++)
						{
							ctx[i] = Utility.uidToOtid((Uid) children[i]);
						}
					}
				}
				else
					throw new Inactive();
			}
		}

		return ctx;
	}

	/**
	 * @return the status of a transaction when all we have is its unique name.
	 *         The transaction must be in the local list.
	 * @since JTS 2.1.2.
	 */

	public org.omg.CosTransactions.Status getCurrentStatus (otid_t txid)
			throws SystemException
	{
		Uid u = Utility.otidToUid(txid);

		if (u == null)
			throw new BAD_PARAM(
					"otid_t "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.otiderror"));
		else
			return getCurrentStatus(u);
	}

	/**
	 * @return the status of a transaction when all we have is its unique name.
	 *         The transaction must be in the local list.
	 * @since JTS 2.1.
	 */

	public org.omg.CosTransactions.Status getCurrentStatus (Uid u)
			throws SystemException
	{
		if (!u.valid())
			throw new BAD_PARAM();
		else
		{
			try
			{
				ControlImple ctx = null;

				synchronized (ControlImple.allControls)
				{
					ctx = (ControlImple) ControlImple.allControls.get(u);
				}

				if (ctx != null)
					return ctx.getImplHandle().get_status();
				else
				{
					/*
					 * If there is a persistent representation for this
					 * transaction, then return that status. Otherwise check
					 * whether this is a server transaction.
					 */

					org.omg.CosTransactions.Status s = getOSStatus(u);

					if ((s == org.omg.CosTransactions.Status.StatusUnknown)
							|| (s == org.omg.CosTransactions.Status.StatusNoTransaction))
					{
						return ServerFactory.getCurrentStatus(u); // check it's
																  // not a
																  // server
																  // transaction
					}
					else
						return s;
				}
			}
			catch (Exception e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.tficaught", new Object[]
					{ "TransactionFactoryImple.getCurrentStatus", u, e });
				}

				return org.omg.CosTransactions.Status.StatusUnknown;
			}
		}
	}

	/**
	 * @return the status of a transaction when all we have is its unique name.
	 *         If the transaction is not in the local list then we look in the
	 *         ObjectStore.
	 * @since JTS 2.1.2.
	 */

	public org.omg.CosTransactions.Status getStatus (otid_t txid)
			throws NoTransaction, SystemException
	{
		Uid u = Utility.otidToUid(txid);

		if (u == null)
			throw new BAD_PARAM(
					"otid_t "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.otiderror"));
		else
			return getStatus(u);
	}

	/**
	 * @return the status of a transaction when all we have is its unique name.
	 *         If the transaction is not in the local list then we look in the
	 *         ObjectStore.
	 * @since JTS 2.1.
	 */

	public org.omg.CosTransactions.Status getStatus (Uid u)
			throws NoTransaction, SystemException
	{
		org.omg.CosTransactions.Status s = org.omg.CosTransactions.Status.StatusUnknown;

		try
		{
			s = getCurrentStatus(u);
		}
		catch (SystemException e2)
		{
			throw e2;
		}
		catch (Exception e3)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.tficaught", new Object[]
				{ "TransactionFactoryImple.getStatus", u, e3 });
			}

			return org.omg.CosTransactions.Status.StatusUnknown;
		}

		/*
		 * If status is unknown, then transaction is cannot be active (even if
		 * it is a server transaction). So, check the object store.
		 */

		if ((s == org.omg.CosTransactions.Status.StatusUnknown)
				|| (s == org.omg.CosTransactions.Status.StatusNoTransaction))
		{
			return getOSStatus(u);
		}
		else
			return s;
	}

	/**
	 * @return the status of the transaction as recorded in the object store.
	 * @since JTS 2.1.1.
	 */

	public org.omg.CosTransactions.Status getOSStatus (Uid u)
			throws NoTransaction, SystemException
	{
		org.omg.CosTransactions.Status s = org.omg.CosTransactions.Status.StatusUnknown;

		if (!u.valid())
			throw new BAD_PARAM();
		else
		{
			// if here then it is not active, so look in the object store

			ObjectStore store = com.arjuna.ats.arjuna.coordinator.TxControl.getStore();

			try
			{
				/*
				 * Do we need to search server transactions too? Possibly not,
				 * since an interposed coordinator can never always say with
				 * certainty what the status is of the root coordinator.
				 */

				int status = store.currentState(u, ArjunaTransactionImple.typeName());

				switch (status)
				{
				case ObjectStore.OS_UNKNOWN: // means no state present, so check
											 // if server transaction
					return ServerFactory.getOSStatus(u);
				case ObjectStore.OS_COMMITTED:
					return org.omg.CosTransactions.Status.StatusCommitted;
				case ObjectStore.OS_UNCOMMITTED:
					return org.omg.CosTransactions.Status.StatusPrepared;
				case ObjectStore.OS_HIDDEN:
				case ObjectStore.OS_COMMITTED_HIDDEN:
				case ObjectStore.OS_UNCOMMITTED_HIDDEN:
					return org.omg.CosTransactions.Status.StatusPrepared;
				default:
					return ServerFactory.getStatus(u);
				}
			}
			catch (Exception e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.tficaught", new Object[]
					{ "TransactionFactoryImple.getStatus", u, e });
				}

				return org.omg.CosTransactions.Status.StatusUnknown;
			}
		}
	}

	/*
	 * @return information on the transactions known by this object.
	 * 
	 * @since JTS 2.1.
	 */

	public GlobalTransactionInfo getGlobalInfo () throws SystemException
	{
		GlobalTransactionInfo info = new GlobalTransactionInfo();

		info.totalNumberOfTransactions = com.arjuna.ats.arjuna.coordinator.TxStats.numberOfTransactions();
		info.numberOfCommittedTransactions = com.arjuna.ats.arjuna.coordinator.TxStats.numberOfCommittedTransactions();
		info.numberOfAbortedTransactions = com.arjuna.ats.arjuna.coordinator.TxStats.numberOfAbortedTransactions();

		if (info.totalNumberOfTransactions > 0)
			info.averageLifetime = (float) (TransactionReaper.transactionLifetime() / info.totalNumberOfTransactions);
		else
			info.averageLifetime = (float) 0.0;

		info.numberOfHeuristics = com.arjuna.ats.arjuna.coordinator.TxStats.numberOfHeuristics();

		TransactionReaper reaper = TransactionReaper.transactionReaper();

		if (reaper == null)
			info.reaperTimeout = 0;
		else
			info.reaperTimeout = (int) reaper.checkingPeriod();

		info.defaultTimeout = TxControl.getDefaultTimeout();

		return info;
	}

	/**
	 * @return information on a specific transaction.
	 * @since JTS 2.1.2.
	 */

	public TransactionInfo getTransactionInfo (otid_t txid)
			throws org.omg.CosTransactions.NoTransaction, SystemException
	{
		Uid u = Utility.otidToUid(txid);

		if (u == null)
			throw new BAD_PARAM(
					"otid_t "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.otiderror"));
		else
			return getTransactionInfo(u);
	}

	/**
	 * @return information on a specific transaction.
	 * @since JTS 2.1.2.
	 * @message com.arjuna.ats.internal.jts.orbspecific.invaliduid Invalid Uid:
	 */

	public TransactionInfo getTransactionInfo (Uid u)
			throws org.omg.CosTransactions.NoTransaction, SystemException
	{
		if (!u.valid())
			throw new BAD_PARAM(
					jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.invaliduid"
							+ " " + u));
		else
		{
			try
			{
				synchronized (ControlImple.allControls)
				{
					ControlImple ctx = (ControlImple) ControlImple.allControls.get(u);

					if (ctx != null)
					{
						TransactionInfo info = new TransactionInfo();

						info.currentDepth = ctx.getImplHandle().getHierarchy().depth();

						TransactionReaper reaper = TransactionReaper.transactionReaper();

						/*
						 * If the reaper has not been created yet, then all
						 * transactions so far must have 0 timeout.
						 */

						if (reaper == null)
							info.timeout = 0;
						else
							info.timeout = (int) reaper.getTimeout(ctx);

						info.numberOfThreads = ctx.getImplHandle().activeThreads();

						return info;
					}
					else
						throw new NoTransaction();
				}
			}
			catch (NoTransaction ex)
			{
				throw ex;
			}
			catch (Exception e)
			{
				e.printStackTrace();

				throw new UNKNOWN();
			}
		}
	}

	/**
	 * @return the transaction Control.
	 * @since JTS 2.1.2.
	 */

	public org.omg.CosTransactions.Control getTransaction (otid_t txid)
			throws org.omg.CosTransactions.NoTransaction, SystemException
	{
		Uid u = Utility.otidToUid(txid);

		if (u == null)
			throw new BAD_PARAM(
					"otid_t "
							+ jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.otiderror"));
		else
			return getTransaction(u);
	}

	/**
	 * @return the transaction Control.
	 * @since JTS 2.1.2.
	 */

	public org.omg.CosTransactions.Control getTransaction (Uid u)
			throws org.omg.CosTransactions.NoTransaction, SystemException
	{
		if (!u.valid())
			throw new BAD_PARAM();
		else
		{
			try
			{
				synchronized (ControlImple.allControls)
				{
					ControlImple ctx = (ControlImple) ControlImple.allControls.get(u);

					if (ctx != null)
						return ctx.getControl();
					else
						throw new NoTransaction();
				}

			}
			catch (NoTransaction ex)
			{
				throw ex;
			}
			catch (Exception e)
			{
				e.printStackTrace();

				throw new UNKNOWN();
			}
		}
	}

	private final org.omg.CosTransactions.otid_t[] activeTransactions ()
			throws Inactive, NoTransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::activeTransactions ()");
		}

		if (ControlImple.allControls == null)
			throw new Inactive();
		else
		{
			synchronized (ControlImple.allControls)
			{
				if (ControlImple.allControls.size() == 0)
					throw new NoTransaction();
				else
				{
					org.omg.CosTransactions.otid_t[] ids = new org.omg.CosTransactions.otid_t[ControlImple.allControls.size()];

					Enumeration iter = ControlImple.allControls.elements();
					int i = 0;

					while (iter.hasMoreElements())
					{
						ControlImple cont = (ControlImple) iter.nextElement();

						if (cont != null)
						{
							ids[i] = Utility.uidToOtid(cont.get_uid().stringForm());
							i++;
						}
					}

					return ids;
				}
			}
		}
	}

	private final org.omg.CosTransactions.otid_t[] unresolvedTransactions ()
			throws Inactive, NoTransaction, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE, com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "TransactionFactoryImple::terminatedTransactions ()");
		}

		InputObjectState uids = new InputObjectState();

		if (!TxStoreLog.getTransactions(uids, ObjectStore.OS_COMMITTED_HIDDEN))
		{
			throw new NoTransaction();
		}
		else
		{
			Uid theUid = new Uid(Uid.nullUid());
			int count = 0;
			boolean finished = false;

			while (!finished)
			{
				try
				{
					theUid.unpack(uids);

					if (theUid == Uid.nullUid())
						finished = true;
					else
						count++;
				}
				catch (Exception e)
				{
					finished = true;
				}
			}

			org.omg.CosTransactions.otid_t[] ids = new org.omg.CosTransactions.otid_t[count];

			uids.reread();

			for (int i = 0; i < count; i++)
			{
				try
				{
					theUid.unpack(uids);

					ids[i] = Utility.uidToOtid(theUid.stringForm());
				}
				catch (Exception e)
				{
				}
			}

			return ids;
		}
	}

	private TransactionFactory _factoryRef;

	private static FactoryList creators = new FactoryList();

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.tfitimeout {0} - invalid
	 *          default transaction timeout {1}. Caught exception: {2}
	 */

	static
	{
        TxControl.setDefaultTimeout(jtsPropertyManager.getJTSEnvironmentBean().getDefaultTimeout());        
	}

}
