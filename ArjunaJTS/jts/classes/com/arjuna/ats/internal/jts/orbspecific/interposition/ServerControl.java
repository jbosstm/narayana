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
 * $Id: ServerControl.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.arjuna.coordinator.BasicAction;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.ats.arjuna.common.Uid;

import org.omg.CosTransactions.*;
import java.util.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;

/**
 * As with ControlImple, the transaction is maintained until the control object
 * is deleted.
 * 
 * This class maintains a handle on the current server-side transaction, and
 * also references to the original transaction it is "mirroring". This allows us
 * to have a single place to hold both sets of information which is accessible
 * to interposed resources, synchronizations, and transactions.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ServerControl.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ServerControl extends ControlImple
{

	public ServerControl (Uid actUid, Control parentCon, ArjunaTransactionImple parentTran, Coordinator realCoord, Terminator realTerm)
	{
		super();

		_realCoordinator = realCoord;
		_realTerminator = realTerm;

		_parentControl = parentCon;

		/*
		 * allControls only contains local controls. Have we seen this
		 * transaction before, i.e., is it a locally created transaction that we
		 * are re-importing?
		 */

		ControlImple cont = ((ControlImple.allControls != null) ? (ControlImple) ControlImple.allControls.get(actUid)
				: null);

		/*
		 * We could do optimisations based on whether this is a transaction we
		 * have locally created anyway and somehow it is being re-imported. If
		 * that is the case, then why create a new transaction to manage it -
		 * this will only add to the disk access? However, currently we cannot
		 * do this optimisation because:
		 * 
		 * (i) if the original control is being terminated from a remote
		 * process, it will not be able to force thread-to-transaction
		 * association (ArjunaTransactionImple et al don't do that.)
		 * 
		 * (ii) certain AIT records require thread-to-transaction association in
		 * order to work (e.g., LockRecord).
		 * 
		 * What this means is that if we do this optimisation and an application
		 * uses AIT, all AIT records will be added to the parent (original)
		 * transaction and not the interposed transaction (which does do
		 * thread-to-transaction association - or the resource does). Then when
		 * the transaction is terminated, these records will be processed and
		 * they will not be able to determine the current transaction.
		 */

		if (cont != null)
		{
			_isWrapper = true;
			_transactionHandle = cont.getImplHandle();

			Coordinator coord = null;
			Terminator term = null;

			try
			{
				coord = cont.get_coordinator();
				term = cont.get_terminator();
			}
			catch (Exception e)
			{
				e.printStackTrace();

				try
				{
					if (coord != null)
						coord.rollback_only();
				}
				catch (Exception ex)
				{
				}
			}

			super.duplicateTransactionHandle(coord, term);
		}
		else
		{
			_transactionHandle = new ServerTransaction(actUid, _parentControl,
					parentTran);
			_isWrapper = false;

			super.createTransactionHandle();

			/*
			 * Pass a pointer to the control to the transaction so it knows what
			 * the control is. We use this for transaction comparison and
			 * thread-to-context management.
			 */

			_transactionHandle.setControlHandle(this);
		}

		_theUid = _transactionHandle.get_uid();

		addControl();
	}

	public void finalize () throws Throwable
	{
		_realCoordinator = null;
		_realTerminator = null;

		super.finalize();
	}

	public final boolean isWrapper ()
	{
		return _isWrapper;
	}

	public Coordinator originalCoordinator ()
	{
		return _realCoordinator;
	}

	public Terminator originalTerminator ()
	{
		return _realTerminator;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.orbspecific.interposition.destfailed
	 *          {0} could not destroy object: {1}
	 */

	public synchronized void destroy () throws ActiveTransaction,
			ActiveThreads, BadControl, Destroyed, SystemException
	{
		if (super._destroyed)
			throw new Destroyed();

		/*
		 * We are about to delete ourself (!) so make sure we don't using
		 * anything on the stack after this point. This includes returning
		 * variables.
		 */

		try
		{
			if (_isWrapper)
			{
				_transactionHandle = null;
			}

			super.destroy();
		}
		catch (BAD_PARAM e)
		{
			// already destroyed
		}
		catch (Destroyed de)
		{
			// already destroyed
		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.interposition.destfailed", new Object[]
				{ "ServerControl", e });
			}
		}
	}

	public ServerControl (ServerTransaction stx)
	{
		super();

		_realCoordinator = null;
		_realTerminator = null;
		_isWrapper = false;

		_transactionHandle = stx;
		_theUid = stx.get_uid();

		createTransactionHandle();

		addControl();
	}

	public ControlImple getParentImple ()
	{
		BasicAction parent = _transactionHandle.parent();

		if (parent != null)
		{
			synchronized (ServerControl.allControls)
			{
				return (ControlImple) ServerControl.allServerControls.get(parent.get_uid());
			}
		}
		else
			return null;
	}

	public String toString ()
	{
		return "ServerControl < " + get_uid() + " >";
	}

	public final boolean forgetHeuristics ()
	{
		if (_transactionHandle != null)
			return _transactionHandle.forgetHeuristics();
		else
			return true;
	}

	protected boolean addControl ()
	{
		synchronized (ServerControl.allServerControls)
		{
			ServerControl.allServerControls.put(get_uid(), this);
		}

		return true;
	}

	protected boolean removeControl ()
	{
		try
		{
			synchronized (ServerControl.allServerControls)
			{
				ServerControl.allServerControls.remove(get_uid());
			}
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	/*
	 * Make private, with public accessor.
	 */

	public static Hashtable allServerControls = new Hashtable();

	private Coordinator _realCoordinator;
	private Terminator _realTerminator;
	private boolean _isWrapper;

}
