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
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.recovery.RecoveryConfiguration;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.internal.arjuna.Implementations;

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
         * @message com.arjuna.ats.internal.arjuna.recovery.socketready
         *          [com.arjuna.ats.internal.arjuna.recovery.socketready]
         *          RecoveryManagerImple is ready on port {0}
     * @message com.arjuna.ats.internal.arjuna.recovery.localready
         *          [com.arjuna.ats.internal.arjuna.recovery.localready]
         *          RecoveryManagerImple is ready. Socket listener is turned off.
     * @message com.arjuna.ats.internal.arjuna.recovery.fail
         *          [com.arjuna.ats.internal.arjuna.recovery.fail]
         *          RecoveryManagerImple: cannot bind to socket on address {0} and port {1}
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


        // by default we use a socket based listener, but it can be turned off if not required.
        boolean useListener = true;
        if("NO".equalsIgnoreCase(arjPropertyManager.propertyManager.getProperty(Environment.RECOVERY_MANAGER_LISTENER))) {
            useListener = false;
        }
        
        /*
                 * Check whether there is a recovery daemon running - only allow one per
                 * object store
                 *
                 * Note: this does not actually check if a recovery manager is running for the same ObjectStore,
                 * only if one is on the same port as our configuration. Thus it's not particularly robust.
                 * TODO: add a lock file to the ObjectStore as a belt and braces approach?
                 *
                 * This check works by trying to bind the server socket, so don't do it if we are running local only
                 * (yup, that means there is a greater chance of winding up with more than one recovery manager if
                 * we are running without a listener. See comment on robustness and file locking.)
                 */

                if (useListener && isRecoveryManagerEndPointInUse())
                {
            if (tsLogger.arjLoggerI18N.isFatalEnabled())
            {
                try
                {
                    tsLogger.arjLoggerI18N.fatal(
                            "com.arjuna.ats.internal.arjuna.recovery.fail",
                            new Object[] {
                                    RecoveryManager.getRecoveryManagerHost(), RecoveryManager.getRecoveryManagerPort()
                            }
                    );
                }
                catch (Throwable t)
                {
                    tsLogger.arjLoggerI18N.fatal(
                            "com.arjuna.ats.internal.arjuna.recovery.fail",
                            new Object[] {
                                    "unknown", "unknown"
                            }
                    );
                }
            }

            throw new FatalError("Recovery manager already active (or recovery port and address are in use)!");
                }

                // start the expiry scanners

                // start the activator recovery loader

                _recActivatorLoader = new RecActivatorLoader();

                // start the expiry scanners

                ExpiredEntryMonitor.startUp();

                // start the periodic recovery thread
                // (don't start this until just about to go on to the other stuff)

                _periodicRecovery = new PeriodicRecovery(threaded, useListener);

                try
                {
                        if (tsLogger.arjLogger.isInfoEnabled())
                        {
                                if(useListener)
                {
                    tsLogger.arjLoggerI18N.info(
                            "com.arjuna.ats.internal.arjuna.recovery.socketready",
                            new Object[] { PeriodicRecovery.
                                    getServerSocket().getLocalPort() });
                }
                else
                {
                    tsLogger.arjLoggerI18N.info(
                            "com.arjuna.ats.internal.arjuna.recovery.localready");
                }
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
                _periodicRecovery.doWork();
        }

        public final void addModule (RecoveryModule module)
        {
                _periodicRecovery.addModule(module);
        }

    public final void removeModule (RecoveryModule module, boolean waitOnScan)
    {
        _periodicRecovery.removeModule(module, waitOnScan);
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

    /**
     * stop the recovery manager
     * @param async false means wait for any recovery scan in progress to complete
     */
        public void stop (boolean async)
        {
                _periodicRecovery.shutdown(async);

                // TODO why?

                // ExpiredEntryMonitor.shutdown();
        }

        /**
         * Suspend the recovery manager. If the recovery manager is in the process of
         * doing recovery scans then it will be suspended afterwards, in order to
         * preserve data integrity.
         *
         * @param async false means wait for the recovery manager to finish any scans before returning.
         */

        public void suspendScan (boolean async)
        {
            _periodicRecovery.suspendScan(async);
        }

        public void resumeScan ()
        {
            _periodicRecovery.resumeScan();
        }

        public void finalize ()
        {
                stop(true);
        }
        
        public void waitForTermination ()
        {
            try
            {
                _periodicRecovery.join();
            }
            catch (final Exception ex)
            {
            }
        }

    /**
     * Test whether the recovery manager (RM) port and address are available - if not assume that another
     * recovery manager is already active.
     *
     * Ideally this method needs to discover whether or not another RM is already monitoring the object store
     *
     * @return true if the RM port and address are in use
     */
    private final boolean isRecoveryManagerEndPointInUse ()
        {
        try
        {
            /*
             * attempt to create the server socket. If an exception is thrown then some other
             * process is using the RM endpoint
             */
            PeriodicRecovery.getServerSocket();

            return false;
        }
        catch (Throwable e)
        {
            return true;
        }
        }

}
