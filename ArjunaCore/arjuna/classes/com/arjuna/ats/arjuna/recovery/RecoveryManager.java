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
 * $Id: RecoveryManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;

class ScanThread extends Thread
{

    public ScanThread (RecoveryManagerImple theImple, RecoveryScan callback)
    {
	super("RecoveryManagerScanThread");

	_theImple = theImple;
	_callback = callback;

	setDaemon(true);
    }

    public void run ()
    {
	_theImple.scan();
	
	if (_callback != null)
	    _callback.completed();
    }
    
    private RecoveryManagerImple _theImple;
    private RecoveryScan         _callback;
}

/**
 * The RecoveryManager daemon.
 */

public class RecoveryManager
{
    /**
     * In this mode the recovery manager runs periodically but may
     * also be driven through messages or via the scan operation if
     * it is embedded.
     */

    public static final int INDIRECT_MANAGEMENT = 0;

    /**
     * In this mode the recovery manager does not run periodically and
     * will only work if driven through messages or via the scan
     * operation if it is embedded.
     */

    public static final int DIRECT_MANAGEMENT = 1;

    /**
     * Obtain a reference to the RecoveryManager singleton. If it hasn't
     * been created yet then it will be. The manager will be created in the
     * INDIRECT_MANAGEMENT mode.
     *
     * @throw IllegalArgumentException thrown if the manager has already been
     * created in a different mode to that requested.
     *
     * @return the manager.
     */
    
    public static synchronized final RecoveryManager manager () throws IllegalArgumentException
    {
	return manager(RecoveryManager.INDIRECT_MANAGEMENT);
    }
    
    /**
     * Obtain a reference to the RecoveryManager singleton. If it hasn't
     * been created yet then it will be. The manager can be created in a
     * management mode defined by the parameter.
     *
     * @param mode the management mode for the manager.
     *
     * @throw IllegalArgumentException thrown if the manager has already been
     * created in a different mode to that requested.
     *
     * @return the manager.
     */

    public static synchronized final RecoveryManager manager (int mode) throws IllegalArgumentException
    {
	if (_recoveryManager == null)
	    _recoveryManager = new RecoveryManager(mode);
	else
	{
	    if (_recoveryManager.mode() != mode)
		throw new IllegalArgumentException();
	}
	
	return _recoveryManager;
    }
    
    /**
     * Delay the start of the recovery manager thread when creating an indirect recovery manager.
     */
    public static synchronized void delayRecoveryManagerThread()
    {
        delayRecoveryManagerThread = true ;
    }

    /**
     * Force a recovery scan now. This is a blocking operation
     * and will only return once the recovery scan has completed.
     */

    public final void scan ()
    {
	_theImple.scan();
    }

    /**
     * Force a recovery scan now. This is a non-blocking operation
     * and will return immediately. Notification of completion of the
     * scan is done through the RecoveryScan object.
     *
     * @param RecoveryScan callback The callback mechanism used to
     * inform users that the scan has completed. If this is <code>null</code>
     * then no callback will happen and asynchronous scanning will occur.
     */

    public final void scan (RecoveryScan callback)
    {
	ScanThread st = new ScanThread(_theImple, callback);
	
	st.start();
    }
    
    /**
     * Stop the periodic recovery manager.
     */

    public final void stop ()
    {
	_theImple.stop();
    }
    
    /**
     * Start the recovery manager thread.
     */
    public void startRecoveryManagerThread()
    {
        _theImple.start() ;
    }

    /**
     * Add a recovery module to the system.
     *
     * @param RecoveryModule module The module to add.
     */

    public final void addModule (RecoveryModule module)
    {
	_theImple.addModule(module);
    }

    public final Vector getModules ()
    {
	return _theImple.getModules();
    }
    
    /**
     * Indicates what mode (INDIRECT_MANAGEMENT or DIRECT_MANAGEMENT)
     * the recovery manager is configured for.
     *
     * @return the management mode.
     */

    public final int mode ()
    {
	return _mode;
    }
    
    /**
     * Run the RecoveryManager. See Administration manual for details.
     */

    public static void main (String[] args)
    {
	boolean testMode = false;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: com.arjuna.ats.arjuna.recovery.RecoveryManager [-help] [-test] [-version]");
		System.exit(0);
	    }
	    if (args[i].compareTo("-version") == 0)
	    {
		System.out.println("Version " + com.arjuna.ats.arjuna.common.Configuration.version());
		System.exit(0);
	    }
	    if (args[i].compareTo("-test") == 0)
	    {
		testMode = true;
	    }
	}

	try
	{
	    manager();
	    
	    if (testMode)
		System.out.println("Ready");
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	}
    }

    private RecoveryManager (int mode)
    {
	if ((mode == RecoveryManager.INDIRECT_MANAGEMENT) && !delayRecoveryManagerThread)
	    _theImple = new RecoveryManagerImple(true);
	else
	    _theImple = new RecoveryManagerImple(false);

	_mode = mode;
    }
    
    private RecoveryManagerImple _theImple = null;
    private int _mode;
    
    private static RecoveryManager _recoveryManager = null;
    private static boolean delayRecoveryManagerThread ;
}
