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
 * $Id: OTSImpleManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts;

import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.jts.common.Environment;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.orbportability.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.utils.Helper;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import java.io.PrintStream;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.UserException;
import org.omg.CORBA.ORBPackage.InvalidName;
import java.io.IOException;

/**
 * This class is responsible for managing the various implementations that are
 * common throughout the transaction service stack, e.g., CurrentImple and
 * TransactionFactoryImple. Applications should not use this class directly, but
 * should instead go via com.arjuna.ats.jts.OTSManager which gives a purely
 * CORBA view of things.
 * 
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: OTSImpleManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class OTSImpleManager
{

	/**
	 * @return the Current object implementation. This has the advantage of not
	 *         needing to register the object with the ORB, which can affect
	 *         performance.
	 */

	public static CurrentImple current () throws org.omg.CORBA.SystemException
	{
		init();

		_current.contextManager().associate();

		return OTSImpleManager._current;
	}

	/**
	 * @return the Current object implementation. This has the advantage of not
	 *         needing to register the object with the ORB, which can affect
	 *         performance.
	 */

	public static CurrentImple systemCurrent ()
			throws org.omg.CORBA.SystemException
	{
		init();

		return OTSImpleManager._current;
	}

	/**
	 * @return the Current object.
	 */

	public static org.omg.CosTransactions.Current get_current ()
			throws org.omg.CORBA.SystemException
	{
		init();

		_current.contextManager().associate();

		return OTSImpleManager._currentRef;
	}

	/**
	 * @return the TransactionFactory object implementation. This has the
	 *         advantage of not needing to register the object withm the ORB,
	 *         which can affect performance.
	 */

	public static TransactionFactoryImple factory ()
			throws org.omg.CORBA.SystemException
	{
		init();

		return _theFactory;
	}

	/**
	 * @return the TransactionFactory object.
	 */

	public static TransactionFactory get_factory ()
			throws org.omg.CORBA.SystemException
	{
		init();

		if (_theFactoryRef == null)
		{
			try
			{
				_theFactoryRef = _theFactory.getReference();
			}
			catch (Exception e)
			{
				throw new UNKNOWN();
			}
		}

		return _theFactoryRef;
	}

	/**
	 * Is a co-located TransactionFactory required?
	 */

	public static boolean localFactory ()
	{
		init();

		return (_theFactory != null);
	}

	/**
	 * Tidy-up the system prior to exiting.
	 */

	public static void purge ()
	{
		if (OTSImpleManager._currentRef != null)
		{
			OTSImpleManager._currentRef = null;
			OTSImpleManager._current = null;
		}

		if (OTSImpleManager._theFactoryRef != null)
		{
			ORBManager.getPOA().shutdownObject(OTSImpleManager._theFactory);
			OTSImpleManager._theFactory = null;
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jts.otsserverfailed Resolution of OTS
	 *          server failed - invalid name: {0}
	 * @message com.arjuna.ats.internal.jts.otsservererror Resolution of OTS
	 *          server failed: {0}
	 */

	private static final synchronized void init ()
	{
		if (_current == null)
		{
			if (OTSImpleManager._theFactory == null)
			{
				/*
				 * Only check once, when the factory is first created.
				 */

				String transactionManager = jtsPropertyManager.propertyManager.getProperty(Environment.TRANSACTION_MANAGER, "NO");
				int resolver = Services.CONFIGURATION_FILE;

				boolean requireTransactionManager = false;

				if (transactionManager.compareTo("YES") == 0)
				{
					requireTransactionManager = true;

					String resolveMechanism = jtsPropertyManager.propertyManager.getProperty(com.arjuna.orbportability.common.Environment.RESOLVE_SERVICE, "CONFIGURATION_FILE");

					if (resolveMechanism.compareTo("NAME_SERVICE") == 0)
						resolver = Services.NAME_SERVICE;
					else
					{
						if (resolveMechanism.compareTo("BIND_CONNECT") == 0)
							resolver = Services.BIND_CONNECT;
					}
				}

				if (requireTransactionManager)
				{
					try
					{
						if (resolver != Services.BIND_CONNECT)
						{
							String[] params = new String[1];

							params[0] = Services.otsKind;

							org.omg.CORBA.Object obj = ORBManager.getServices().getService(Services.transactionService, params, resolver);

							params = null;

							OTSImpleManager._theFactoryRef = org.omg.CosTransactions.TransactionFactoryHelper.narrow(obj);
						}
						else
						{
						}

						if (OTSImpleManager._theFactoryRef == null)
							throw new BAD_PARAM();
					}
					catch (InvalidName e1)
					{
						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.otsserverfailed", new Object[]
							{ e1 });
						}

						throw new com.arjuna.ats.arjuna.exceptions.FatalError(
								e1.toString());
					}
					catch (BAD_PARAM ex1)
					{
						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.otsserverfailed", new Object[]
							{ ex1 });
						}

						throw new com.arjuna.ats.arjuna.exceptions.FatalError(
								ex1.toString());
					}
					catch (IOException e2)
					{
						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.otsservererror", new Object[]
							{ e2 });
						}

						throw new com.arjuna.ats.arjuna.exceptions.FatalError(
								e2.toString());
					}
					catch (SystemException e3)
					{
						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.otsservererror", new Object[]
							{ e3 });
						}

						throw new com.arjuna.ats.arjuna.exceptions.FatalError(
								e3.toString());
					}
					catch (UserException e4)
					{
						if (jtsLogger.loggerI18N.isWarnEnabled())
						{
							jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.otsservererror", new Object[]
							{ e4 });
						}

						throw new com.arjuna.ats.arjuna.exceptions.FatalError(
								e4.toString());
					}
				}
				else
				{
					/* force to be local */

					OTSImpleManager._theFactory = new TransactionFactoryImple();
				}
			}

			if (OTSImpleManager._current == null)
			{
				try
				{
					OTSImpleManager._current = new CurrentImple();
					OTSImpleManager._currentRef = OTSImpleManager._current;
				}
				catch (Exception e)
				{
					OTSImpleManager._current = null;

					throw new com.arjuna.ats.arjuna.exceptions.FatalError(
							"OTSImpleManager.current: " + e.toString());
				}
			}
		}
	}

	private static TransactionFactoryImple _theFactory = null;

	private static TransactionFactory _theFactoryRef = null;

	private static CurrentImple _current = null;

	private static org.omg.CosTransactions.Current _currentRef = null;

	static
	{
		ORBManager.getPOA().addPreShutdown(new com.arjuna.ats.internal.jts.lifecycle.ShutdownOTS());

		if (!com.arjuna.ats.internal.jts.Implementations.added())
			com.arjuna.ats.internal.jts.Implementations.initialise();
	}

}
