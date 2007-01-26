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
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryManagerImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.io.IOException;
import java.util.Vector;

import com.arjuna.common.util.propertyservice.PropertyManagerFactory;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.recovery.RecoveryConfiguration;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.internal.arjuna.Implementations;
import com.arjuna.ats.internal.arjuna.utils.SocketProcessId;

/**
 * The RecoveryManagerImple - does the real work. Currently we can have only one
 * of these per node, so each instance checks it's the only one running. If it
 * isn't it will kill itself before doing any work.
 */

public class RecoveryManagerImple
{
	private PeriodicRecovery _periodicRecovery = null;

	private RecActivatorLoader _recActivatorLoader = null;

	/**
	 * Does the work of setting up crash recovery.
	 * 
	 * @param threaded
	 *            if <code>true</code> then the manager will start a separate
	 *            thread to run recovery periodically.
	 * 
	 * @message com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_1
	 *          [com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_1] -
	 *          property io exception {0}
	 * @message com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_2
	 *          [com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_2] -
	 *          socket io exception {0}
	 * @message com.arjuna.ats.internal.arjuna.recovery.ready
	 *          [com.arjuna.ats.internal.arjuna.recovery.ready]
	 *          RecoveryManagerImple is ready on port {0}
	 */

	public RecoveryManagerImple (boolean threaded)
	{
		String rmPropertyFile = RecoveryConfiguration
				.recoveryManagerPropertiesFile();

		try
		{
			arjPropertyManager.propertyManager = PropertyManagerFactory
					.getPropertyManager("com.arjuna.ats.propertymanager",
							"recoverymanager");
		}
		catch (Exception ex)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N
						.warn(
								"com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_1",
								new Object[] { ex });
			}
		}

		// force normal recovery trace on
		tsLogger.arjLogger.mergeFacilityCode(FacilityCode.FAC_RECOVERY_NORMAL);
		tsLogger.arjLoggerI18N
				.mergeFacilityCode(FacilityCode.FAC_RECOVERY_NORMAL);

		/*
		 * This next would force debugging on, but separate recovery mgr file
		 * makes this unnecessary.
		 */

		Implementations.initialise();

		/*
		 * Check whether there is a recovery daemon running - only allow one per
		 * machine (currently!)
		 */

		if (activeRecoveryManager())
		{
			throw new FatalError("Recovery manager already active!");
		}

		// start the expiry scanners

		// start the activator recovery loader

		_recActivatorLoader = new RecActivatorLoader();

		// start the expiry scanners

		ExpiredEntryMonitor.startUp();

		// start the periodic recovery thread
		// (don't start this until just about to go on to the other stuff)

		_periodicRecovery = new PeriodicRecovery(threaded);

		try
		{
			if (tsLogger.arjLogger.isInfoEnabled())
			{
				tsLogger.arjLoggerI18N.info(
						"com.arjuna.ats.internal.arjuna.recovery.ready",
						new Object[] { new Integer(_periodicRecovery
								.getServerSocket().getLocalPort()) });
			}
		}
		catch (IOException ex)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
				tsLogger.arjLoggerI18N
						.warn(
								"com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple_2",
								new Object[] { ex });
			}
		}
	}

	public final void scan ()
	{
		_periodicRecovery.doWork(false);
	}

	public final void addModule (RecoveryModule module)
	{
		_periodicRecovery.addModule(module);
	}

	public final Vector getModules ()
	{
		return _periodicRecovery.getModules();
	}

	public void start ()
	{
		if (!_periodicRecovery.isAlive())
		{
			_periodicRecovery.start();
		}
	}

	public void stop ()
	{
		_periodicRecovery.shutdown();

		// TODO why?

		// ExpiredEntryMonitor.shutdown();
	}

	public void finalize ()
	{
		stop();
	}

	private final boolean activeRecoveryManager ()
	{
		// we should be checking for the port in use or something!

		SocketProcessId socket = null;
		boolean active = false;

		try
		{
			socket = new SocketProcessId();

			if (socket.getpid() == -1) 
				active = true;
		}
		catch (FatalError ex)
		{
			// already active on that port

			active = true;
		}

		return active;
	}

}
