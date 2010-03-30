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
 * $Id: ExplicitInterposition.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import com.arjuna.ArjunaOTS.InterpositionFailed;

import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.SystemException;

/**
 * This class is responsible for doing interposition in the case where implicit
 * context propagation is not possible.
 * 
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: ExplicitInterposition.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * 
 * @message com.arjuna.ats.jts.excalledagain {0} called multiple times.
 */

public class ExplicitInterposition
{

	/**
	 * Create a new instance and remember any current transaction that may be
	 * associated with the invoking thread so that it can be restored once
	 * interposition has finished.
	 */

	public ExplicitInterposition ()
	{
		this(true);
	}

	/**
	 * Create a new instance. Depending upon the value of the boolean parameter,
	 * remember any current transaction that may be associated with the invoking
	 * thread so that it can be restored once interposition has finished.
	 */

	public ExplicitInterposition (boolean remember)
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::ExplicitInterposition ( "
                    + remember + " )");
        }

		_remember = remember;
		_inUse = false;
		_oldControl = null;
	}

	/**
	 * Create a new instance and interpose with the specified transaction.
	 * Depending upon the value of the boolean parameter, remember any current
	 * transaction that may be associated with the invoking thread so that it
	 * can be restored once interposition has finished.
	 */

	public ExplicitInterposition (Control action, boolean remember)
			throws InterpositionFailed, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::ExplicitInterposition ( Control action, "
                    + remember + " )");
        }

		_remember = remember;
		_inUse = false;
		_oldControl = null;

		registerTransaction(action);
	}

	/**
	 * Create a new instance and interpose with the specified transaction.
	 * Depending upon the value of the boolean parameter, remember any current
	 * transaction that may be associated with the invoking thread so that it
	 * can be restored once interposition has finished.
	 */

	public ExplicitInterposition (PropagationContext ctx, boolean remember)
			throws InterpositionFailed, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::ExplicitInterposition ( PropagationContext ctx, "
                    + remember + " )");
        }

		_remember = remember;
		_inUse = false;
		_oldControl = null;

		registerTransaction(ctx);
	}

	/**
	 * @message com.arjuna.ats.jts.exunregfail {0} - could not unregister from
	 *          transaction!
	 */

	public void finalize ()
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition.finalize ()");
        }

		if (_inUse)
		{
			try
			{
				unregisterTransaction();
			}
			catch (Exception e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.exunregfail", new Object[]
					{ "ExplicitInterposition.finalize" });
				}
			}
		}
	}

	/**
	 * Perform interposition with the specified transaction.
	 */

	public final synchronized void registerTransaction (Control action)
			throws InterpositionFailed, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::registerTransaction ( Control action )");
        }

		if (_inUse)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.excalledagain", new Object[]
				{ "ExplicitInterposition.unregisterTransaction" });
			}

			throw new InterpositionFailed();
		}

		boolean registerNull = false;

		if (action != null)
		{
			try
			{
				Coordinator coord = action.get_coordinator();

				if (coord != null)
				{
					PropagationContext ctx = coord.get_txcontext();

					if (ctx != null)
					{
						try
						{
							registerTransaction(ctx);
							ctx = null;
						}
						catch (Exception e)
						{
							e.printStackTrace();

							ctx = null;

							throw new InterpositionFailed(e.toString());
						}
					}
					else
						registerNull = true;

					coord = null;
				}
				else
					registerNull = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();

				throw new InterpositionFailed(e.toString());
			}
		}
		else
		{
			_inUse = true;
			registerNull = true;
		}

		if (registerNull)
		{
			try
			{
				OTSImpleManager.current().resume((Control) null);
			}
			catch (Exception e)
			{
				e.printStackTrace();

				throw new InterpositionFailed(e.toString());
			}
		}
	}

	/**
	 * We need this explicit method because we cannot rely upon the object
	 * "destructor" being called by the time the method which instantiated the
	 * interposition class ends!
	 */

	public synchronized void unregisterTransaction () throws SystemException
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::unregisterTransaction ()");
        }

		if (!_inUse)
			throw new INVALID_TRANSACTION(ExceptionCodes.INVALID_ACTION,
					CompletionStatus.COMPLETED_NO);
		else
		{
			try
			{
				Control control = OTSImpleManager.current().suspend();

				control = null;

				if (_remember && (_oldControl != null))
				{
					try
					{
						OTSImpleManager.current().resumeWrapper(_oldControl);
						_oldControl = null;
					}
					catch (Exception exp)
					{
						throw new UNKNOWN(exp.toString());
					}
				}
			}
			catch (UNKNOWN exp)
			{
				throw exp;
			}
			catch (SystemException ex)
			{
				_inUse = false;

				throw ex;
			}
			catch (Exception e)
			{
				_inUse = false;

				throw new UNKNOWN(e.toString());
			}

			_inUse = false;
		}
	}

	/**
	 * @message com.arjuna.ats.jts.eicaughtexception {0} caught unexpected
	 *          exception: {1}
	 */

	private final synchronized void registerTransaction (PropagationContext ctx)
			throws InterpositionFailed, SystemException
	{
		if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("ExplicitInterposition::registerTransaction ( PropagationContext ctx )");
        }

		if (_inUse)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.excalledagain", new Object[]
				{ "ExplicitInterposition.registerTransaction" });
			}

			throw new InterpositionFailed();
		}

		if ((ctx == null) || (ctx.current.coord == null)) // invalid
			throw new INVALID_TRANSACTION(ExceptionCodes.INVALID_ACTION,
					CompletionStatus.COMPLETED_NO);

		_inUse = true;

		TransactionFactoryImple _localFactory = OTSImpleManager.factory();

		try
		{
			ControlImple cont = _localFactory.recreateLocal(ctx);
			CurrentImple current = OTSImpleManager.current();

			/*
			 * If this thread is associated with any transactions, then they
			 * will be lost in favour of this new hierarchy, unless we have
			 * remembered them explicitly.
			 */

			if (_remember)
			{
				try
				{
					_oldControl = current.suspendWrapper();
				}
				catch (Exception e)
				{
					throw new InterpositionFailed();
				}
			}

			current.resumeImple(cont);

			//	    current.resume(cont.getControl());

			cont = null;
		}
		catch (InterpositionFailed ex)
		{
			throw ex;
		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.eicaughtexception", new Object[]
				{ "ExplicitInterposition.registerTransaction(PropagationContext)", e });
			}

			throw new InterpositionFailed();
		}
	}

	private boolean _remember;

	private boolean _inUse;

	private ControlWrapper _oldControl;

}
