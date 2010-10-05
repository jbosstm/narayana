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

import java.util.Vector;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.common.util.ConfigurationInfo;

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
        if (_theImple != null)
        {
            _theImple.scan();

            if (_callback != null)
                _callback.completed();
        }
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
     * @throws IllegalArgumentException thrown if the manager has already been
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
     * @throws IllegalArgumentException thrown if the manager has already been
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
     *
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final void scan ()
    {
        checkState();

        _theImple.scan();
    }

    /**
     * Force a recovery scan now. This is a non-blocking operation
     * and will return immediately. Notification of completion of the
     * scan is done through the RecoveryScan object.
     *
     * @param callback callback The callback mechanism used to
     * inform users that the scan has completed. If this is <code>null</code>
     * then no callback will happen and asynchronous scanning will occur.
     *
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final void scan (RecoveryScan callback)
    {
        checkState();

	ScanThread st = new ScanThread(_theImple, callback);

	st.start();
    }

    /**
     * Terminate and cleanup the recovery manager. There is no going back from this. This is a
     * synchronous operation so return means that the recovery has completed.
     *
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final void terminate ()
    {
        terminate(false);
    }

    /**
     * Terminate and cleanup the recovery manager. There is no going back from this. Can be called
     * synchronous or asynchronously. If you have any intention of restarting the recovery manager
     * later then you MUST use the async=false option.
     *
     * @param async false means wait for any recovery scan in progress to complete.
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final synchronized void terminate (boolean async)
    {
        checkState();

        _theImple.stop(async);
        _theImple = null;
    }

    /**
     * If the recovery manager has been shutdown previously then recreate it in
     * the same mode as before. Otherwise ignore.
     */

    public final synchronized void initialize ()
    {
        if (_theImple == null)
        {
            if ((_mode == RecoveryManager.INDIRECT_MANAGEMENT) && !delayRecoveryManagerThread)
                _theImple = new RecoveryManagerImple(true);
            else
                _theImple = new RecoveryManagerImple(false);
        }
    }

    // does nothing when running embedded.

    /**
     * wait for the recovery thread to be shutdown. n.b. this will not return unless and until shutdown
     * is called.
     *
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public void waitForTermination ()
    {
        checkState();

        _theImple.waitForTermination();
        _theImple = null;
    }

    /**
     * Suspend the recovery manager. If the recovery manager is in the process of
     * doing recovery scans then it will be suspended afterwards, in order to
     * preserve data integrity.
     *
     * @param async false means wait for the recovery manager to finish any scans before returning.
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public void suspend (boolean async)
    {
        checkState();

        _theImple.suspendScan(async);
    }

    /**
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public void resume ()
    {
        checkState();

        _theImple.resumeScan();
    }

    /**
     * Start the recovery manager thread.
     *
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */
    public void startRecoveryManagerThread()
    {
        checkState();

        _theImple.start() ;
    }

    /**
     * Add a recovery module to the system.
     *
     * @param module module The module to add.
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final void addModule (RecoveryModule module)
    {
        checkState();

	_theImple.addModule(module);
    }

    /**
     * Remove a recovery module from the system.
     *
     * @param module The module to remove.
     * @param waitOnScan true if the remove operation should wait for any in-progress scan to complete
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final void removeModule (RecoveryModule module, boolean waitOnScan)
    {
        checkState();

	_theImple.removeModule(module, waitOnScan);
    }

    /**
     * Remove all modules.
     * 
     * WARNING: Use with extreme care as this will stop recovery from doing anything!
     */
    
    public final void removeAllModules (boolean waitOnScan)
    {
        checkState();
        
        _theImple.removeAllModules(waitOnScan);
    }
    
    /**
     * Obtain a snapshot list of available recovery modules.
     *
     * @return a snapshot list of the currently installed recovery modules
     * @throws IllegalStateException if the recovery manager has been shutdown.
     */

    public final Vector getModules ()
    {
        checkState();

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

    public static InetAddress getRecoveryManagerHost() throws UnknownHostException
    {
        String host = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress();

        return Utility.hostNameToInetAddress(host);
    }

    public static int getRecoveryManagerPort()
    {
        return recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();
    }

    /**
     * Obtain a client connection to the recovery manager
     *
     * @return a bound client socket connection to the recovery manager
     * @throws IOException
     */
    public static Socket getClientSocket () throws IOException
    {
        Socket socket = new Socket(getRecoveryManagerHost(), getRecoveryManagerPort());

        tsLogger.i18NLogger.info_recovery_RecoveryManager_4(socket.getInetAddress().getHostAddress(), Integer.toString(socket.getLocalPort()));

        return socket;
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
		System.out.println("Version " + ConfigurationInfo.getVersion());
		System.exit(0);
	    }
	    if (args[i].compareTo("-test") == 0)
	    {
		testMode = true;
	    }
	}

        try
        {
            RecoveryManager manager = null;

            try
            {
                if(testMode) {
                    // vicious kludge to sidestep ORB init issue.
                    // TODO: replace with something less cringeworthy.
                    Thread.sleep(2000);
                }

                manager = manager();
            }
            catch(java.lang.Error e)
            {
                if(testMode)
                {
                    // in some test cases the recovery manager is killed and restarted in quick succession.
                    // sometimes the O/S does not free up the port fast enough, so we can't reclaim it on restart.
                    // For test mode only, we therefore have a simple backoff-retry kludge:
                    System.err.println("Warning: got error '"+e.toString()+"' on startup, will retry in 5 seconds in the hope it is transient.");
                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch(InterruptedException interruptedException)
                    {
                        // do nothing
                    }
                    manager = manager();
                }
                else
                {
                    throw e;
                }
            }

            if (testMode)
            {
                System.out.println("Ready");
            }

            // this is never going to return because it only returns when shutdown is called and
            // there is nothing which is going to call shutdown. we probably aught  to provide a
            // clean way of terminating this process.

            manager.waitForTermination();

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

    private final void checkState ()
    {
        if (_theImple == null)
            throw new IllegalStateException();
    }

    private RecoveryManagerImple _theImple = null;
    private int _mode;

    private static RecoveryManager _recoveryManager = null;
    private static boolean delayRecoveryManagerThread ;
}
