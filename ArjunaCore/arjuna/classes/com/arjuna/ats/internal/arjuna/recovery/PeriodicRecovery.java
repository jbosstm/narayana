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
 * $Id: PeriodicRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.lang.InterruptedException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.io.*;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.utils.Utility;

/**
 * Threaded object to perform the periodic recovery. Instantiated in
 * the RecoveryManager. The work is actually completed by the recovery
 * modules. These modules are dynamically loaded. The modules to load
 * are specified by properties beginning with "RecoveryExtension"
 * <P>
 * n.b. recovery scans may be performed by this object (it is a thread and may be started as a background task)
 * and by other ad hoc threads
 * @author
 * @version $Id: PeriodicRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

public class PeriodicRecovery extends Thread
{
   /***** public API *****/

    /**
     *  state values indicating whether or not some thread is currently scanning. used to define values of field
     * {@link PeriodicRecovery#_currentStatus}
     */
   public static enum Status
   {
       /**
        * state value indicating that no thread is scanning
        */
       INACTIVE,
       /**
        * state value indicating that some thread is scanning.
        * n.b. the scanning thread may not be the singleton PeriodicRecovery thread instance
        */
       SCANNING
   }

    /**
     * state values indicating operating mode of scanning process for ad hoc threads and controlling behaviour of
     * singleton periodic recovery thread. used to define values of field {@link PeriodicRecovery#_currentMode}
     *
     * n.b. {@link PeriodicRecovery#_currentStatus} may not transition to state SCANNING when
     * {@link PeriodicRecovery#_currentStatus} is in state SUSPENDED or TERMINATED. However, if a scan is in
     * progress when {@link PeriodicRecovery#_currentMode} transitions to state SUSPENDED or TERMINATED
     * {@link PeriodicRecovery#_currentStatus} may (temporarily) remain in state SCANNING before transitioning
     * to state INACTIVE.
     */
   public static enum Mode
   {
       /**
        * state value indicating that new scans may proceed
        */
       ENABLED,
       /**
        * state value indicating that new scans may not proceed and the periodic recovery thread should suspend
        */
       SUSPENDED,
       /**
        * state value indicating that new scans may not proceed and that the singleton
        * PeriodicRecovery thread instance should exit if it is still running
        */
       TERMINATED
   }

    /**
     *
     *
     * @param threaded
     * @param useListener  if true, start a socket based listener.
     */
    public PeriodicRecovery (boolean threaded, boolean useListener)
    {
        super("Periodic Recovery");
        
        initialise();

        // Load the recovery modules that actually do the work.

        loadModules();

        if (useListener)
        {
            try
            {
                _workerService = new WorkerService(this);

                _listener = new Listener(getServerSocket(), _workerService);
                _listener.setDaemon(true);

                tsLogger.i18NLogger.info_recovery_PeriodicRecovery_13(_socket.getInetAddress().getHostAddress(),
                            Integer.toString(_socket.getLocalPort()));
            }
            catch (Exception ex) {
                tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_9(ex);
            }
        }

        if (threaded)
        {
            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debug("PeriodicRecovery: starting background scanner thread");
            }
            start();
        }

        if(useListener && _listener != null)
        {
            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debug("PeriodicRecovery: starting listener worker thread");
            }
            _listener.start();
        }
    }

    /**
     * initiate termination of the periodic recovery thread and stop any subsequent scan requests from proceeding.
     *
     * this switches the recovery operation mode to TERMINATED. if a scan is in progress when this method is called
     * and has not yet started phase 2 of its scan it will be forced to return before completing phase 2.
     *
     * @param async false if the calling thread should wait for any in-progress scan to complete before returning
     */
   public void shutdown (boolean async)
   {
       // stop the lsitener from adding threads which can exercise the worker
       if (_listener != null) {
           _listener.stopListener();
       }

       synchronized (_stateLock) {
           if (getMode() != Mode.TERMINATED) {
               if (tsLogger.logger.isDebugEnabled()) {
                   tsLogger.logger.debug("PeriodicRecovery: Mode <== TERMINATED");
               }
               setMode(Mode.TERMINATED);
               _stateLock.notifyAll();
           }

           if (!async) {
               // synchronous, so we keep waiting until the currently active scan stops or scanning
               // changes to TERMINATED
               while (getStatus() == Status.SCANNING) {
                   try {
                       if (tsLogger.logger.isDebugEnabled()) {
                           tsLogger.logger.debug("PeriodicRecovery: shutdown waiting for scan to end");
                       }
                       _stateLock.wait();
                   } catch(InterruptedException ie) {
                       // just ignore and retest condition
                   }
               }
               if (tsLogger.logger.isDebugEnabled()) {
                   tsLogger.logger.debug("PeriodicRecovery: shutdown scan wait complete");
               }
           }
       }

       // if the shutdown is synchronous then make sure the periodic recovery thread really has stopped running
       if (!async && this.isAlive()) {
           try {
               this.join();
           } catch (InterruptedException e) {
               // ignore
           }
       }
   }

    /**
     * make all scanning operations suspend.
     *
     * This switches the recovery operation mode to SUSPENDED. Any attempt to start a new scan either by an ad hoc
     * threads or by the periodic recovery thread will suspend its thread until the mode changes. If a scan is in
     * progress when this method is called it will complete its scan without suspending.
     *
     * @param async false if the calling thread should wait for any in-progress scan to complete before returning
     */

   public void suspendScan (boolean async)
   {
       synchronized (_stateLock)
       {
           // only switch and kick everyone if we are currently ENABLED

           if (getMode() == Mode.ENABLED) {
               if (tsLogger.logger.isDebugEnabled()) {
                   tsLogger.logger.debug("PeriodicRecovery: Mode <== SUSPENDED");
               }
               setMode(Mode.SUSPENDED);
               _stateLock.notifyAll();
           }
           if (!async) {
               // synchronous, so we keep waiting until the currently active scan stops
               while (getStatus() == Status.SCANNING) {
                   try {
                       if (tsLogger.logger.isDebugEnabled()) {
                           tsLogger.logger.debug("PeriodicRecovery: suspendScan waiting for scan to end");
                       }
                       _stateLock.wait();
                   } catch(InterruptedException ie) {
                       // just ignore and retest condition
                   }
                   if (tsLogger.logger.isDebugEnabled()) {
                       tsLogger.logger.debug("PeriodicRecovery: suspendScan scan wait compelete");
                   }
               }
           }
       }
   }

    /**
     * resume scanning operations
     *
     * This switches the recovery operation mode from SUSPENDED to RESUMED. Any threads which suspended when
     * they tried to start a scan will be woken up by this transition.
     */
   public void resumeScan ()
   {
       synchronized (_stateLock)
       {
           if (getMode() == Mode.SUSPENDED) {
               if (tsLogger.logger.isDebugEnabled()) {
                   tsLogger.logger.debug("PeriodicRecovery: Mode <== ENABLED");
               }
               setMode(Mode.ENABLED);
               _stateLock.notifyAll();
           }
       }
   }

    /**
     *
     * @return a bound server socket corresponding to the recovery manager
     * @throws IOException if the host name is unknown or the endpoint has already been bound
     */
    public ServerSocket getServerSocket () throws IOException
    {
        synchronized (_socketLock)
        {
            if (_socket == null)
            {
                _socket = new ServerSocket(RecoveryManager.getRecoveryManagerPort(), Utility.BACKLOG, RecoveryManager.getRecoveryManagerHost());
                
                recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryPort(_socket.getLocalPort());
            }

            return _socket;
        }
    }

   /**
    * Implements the background thread which performs the periodic recovery
    */

   public void run ()
   {
       boolean finished = false;

       do
       {
           boolean workToDo = false;
           // ok, get to the point where we are ready to start a scan
           synchronized(_stateLock) {
               if (getStatus() == Status.SCANNING) {
                   // need to wait for some other scan to finish
                   if (tsLogger.logger.isDebugEnabled()) {
                       tsLogger.logger.debug("PeriodicRecovery: background thread waiting on other scan");
                   }
                   doScanningWait();
                   // we don't wait around if a worker scan request has just come in
                   if (getMode() == Mode.ENABLED && !_workerScanRequested) {
                       // the last guy just finished scanning so we ought to wait a bit rather than just
                       // pile straight in to do some work
                       if (tsLogger.logger.isDebugEnabled()) {
                           tsLogger.logger.debug("PeriodicRecovery: background thread backing off");
                       }
                       doPeriodicWait();
                       // if we got told to stop then do so
                       finished = (getMode() == Mode.TERMINATED);
                   }
               } else {
                   // status == INACTIVE so we can go ahead and scan if scanning is enabled
                   switch (getMode()) {
                       case ENABLED:
                           // ok grab our chance to be the scanning thread
                           if (tsLogger.logger.isDebugEnabled()) {
                               tsLogger.logger.debug("PeriodicRecovery: background thread Status <== SCANNING");
                           }
                           setStatus(Status.SCANNING);
                           // must kick any other waiting threads
                           _stateLock.notifyAll();
                           workToDo = true;
                           break;
                       case SUSPENDED:
                           // we need to wait while we are suspended
                           if (tsLogger.logger.isDebugEnabled()) {
                               tsLogger.logger.debug("PeriodicRecovery: background thread wait while SUSPENDED");
                           }
                           doSuspendedWait();
                           // we come out of here with the lock and either ENABLED or TERMINATED
                           finished = (getMode() == Mode.TERMINATED);
                           break;
                       case TERMINATED:
                           finished = true;
                           break;
                   }
               }
           }

           // its ok to start work if requested -- we cannot be stopped now by a mode change to SUSPEND
           // or TERMINATE until we get through phase 1 and maybe phase 2 if we are lucky

           if (workToDo) {
               // ok it is now this thread's turn to run a scan. before starting we check if there is a
               // worker waiting and reset the waiting flag. we will check again after the scan has
               // completed to see if a worker request has come in after starting this scan.
               // if so we avoid notifying the worker ensuring a requst is only confirmed when a
               // full scan has happened afetr the request was made

               boolean notifyRequired;
               synchronized(_stateLock) {
                   notifyRequired = _workerScanRequested;
                   _workerScanRequested = false;
               }

               // we are in state SCANNING so actually do the scan
               if (tsLogger.logger.isDebugEnabled()) {
                   tsLogger.logger.debug("PeriodicRecovery: background thread scanning");
               }
               doWorkInternal();
               // clear the SCANNING state now we have done
               synchronized(_stateLock) {
                   if (tsLogger.logger.isDebugEnabled()) {
                       tsLogger.logger.debug("PeriodicRecovery: background thread Status <== INACTIVE");
                   }
                   setStatus(Status.INACTIVE);
                   // must kick any other waiting threads
                   _stateLock.notifyAll();
                   
                   // check if we need to notify a listener worker that we just finished  a scan
                   if (notifyRequired && !_workerScanRequested) {
                       notifyWorker();
                   }

                   if (getMode() == Mode.ENABLED && !_workerScanRequested) {
                       // we managed a full scan and scanning is still enabled
                       // so wait a bit before the next attempt
                       if (tsLogger.logger.isDebugEnabled()) {
                           tsLogger.logger.debug("PeriodicRecovery: background thread backing off");
                       }
                       doPeriodicWait();
                   }
                   finished = (getMode() == Mode.TERMINATED);
               }
           }
       } while (!finished);

       // make sure the worker thread is not wedged waiting for a scan to complete

       synchronized(_stateLock) {
           if (_workerScanRequested) {
               notifyWorker();
           }
       }

       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("PeriodicRecovery: background thread exiting");
       }
   }

    /**
     * Perform a recovery scan on all registered modules.
     *
     * <b>Caveats:</b> if a scan is already in progress this method will wait for it to complete otherwise it will
     * perform its own scan before returning. If scanning is suspended this will require waiting for scanning
     * to resume.
     */

    public final void doWork ()
    {
        boolean workToDo = false;

        synchronized(_stateLock) {
            if (getMode() == Mode.SUSPENDED) {
                if (tsLogger.logger.isDebugEnabled()) {
                    tsLogger.logger.debug("PeriodicRecovery: ad hoc thread wait while SUSPENDED");
                }
                doSuspendedWait();
            }

            // no longer SUSPENDED --  retest in case we got TERMINATED

            if (getMode() == Mode.TERMINATED) {
                if (tsLogger.logger.isDebugEnabled()) {
                    tsLogger.logger.debug("PeriodicRecovery: ad hoc thread scan TERMINATED");
                }
            } else {

                // ok scanning must be enabled -- see if we can start a scan or whether we have to wait on another one

                if (getStatus() == Status.SCANNING) {
                    // just wait for the other scan to finish
                    if (tsLogger.logger.isDebugEnabled()) {
                        tsLogger.logger.debug("PeriodicRecovery: ad hoc thread waiting on other scan");
                    }
                    doScanningWait();
                } else {

                    // ok grab our chance to start a scan
                    setStatus(Status.SCANNING);
                    // must kick any other waiting threads
                    _stateLock.notifyAll();
                    if (tsLogger.logger.isDebugEnabled()) {
                        tsLogger.logger.debug("PeriodicRecovery: ad hoc thread Status <== SCANNING");
                    }
                    workToDo = true;
                }
            }
        }

        if (workToDo) {
            // ok it is now this thread's turn to run a scan. before starting we check if there is a
            // worker waiting and reset the waiting flag. we will check again after the scan has
            // completed to see if a worker request has come in after starting this scan.
            // if so we avoid notifying the worker ensuring a request is only confirmed when a
            // full scan has happened after the request was made

            boolean notifyRequired;

            synchronized(_stateLock) {
                notifyRequired = _workerScanRequested;
                _workerScanRequested = false;
            }


            // ok to start work -- we cannot be stopped now by a mode change to SUSPEND or TERMINATE
            // until we get through phase 1 and maybe phase 2 if we are lucky
            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debug("PeriodicRecovery: ad hoc thread scanning");
            }
            doWorkInternal();

            // clear the scan for some other thread to have a go
            synchronized(_stateLock) {
                if (tsLogger.logger.isDebugEnabled()) {
                    tsLogger.logger.debug("PeriodicRecovery: ad hoc thread Status <== INACTIVE");
                }
                setStatus(Status.INACTIVE);
                // must kick any other waiting threads
                _stateLock.notifyAll();

                // notify the worker if it was waiting before we started the scan otherwise just leave it to
                // be notified when the next scan finishes.
                if (notifyRequired && !_workerScanRequested) {
                    notifyWorker();
                }
            }
        }
    }

    /**
     * called by the listener worker to wake the periodic recovery thread and get it to start a scan if one
     * is not already in progress
     */

    public void wakeUp()
    {
        synchronized (_stateLock) {
            _workerScanRequested = true;
            // wake up the periodic recovery thread if no scan is in progress
            if (getStatus() != Status.SCANNING) {
                if (tsLogger.logger.isDebugEnabled()) {
                    tsLogger.logger.debug("PeriodicRecovery: listener worker interrupts background thread");
                }
                _stateLock.notifyAll();
            }
        }
    }

    /**
     * Add the specified module to the end of the recovery module list.
     * There is no way to specify relative ordering of recovery modules
     * with respect to modules loaded via the property file.
     *
     * @param module The module to append.
     */

    public final void addModule (RecoveryModule module)
    {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("PeriodicRecovery: adding module " + module.getClass().getName());
        }
        _recoveryModules.add(module);
    }

    /**
     * remove a recovery module from the recovery modules list
     * @param module the module to be removed
     * @param waitOnScan true if the remove operation should wait for any in-progress scan to complete
     */
    public final void removeModule (RecoveryModule module, boolean waitOnScan)
    {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("PeriodicRecovery: removing module " + module.getClass().getName());
        }

        if (waitOnScan) {
            // make sure any scan which might be using the module has completed
            synchronized (_stateLock) {
                    doScanningWait();
            }
        }
        
        // now remove it.
        
        _recoveryModules.remove(module);
    }
    
    /**
     * Remove all modules.
     * 
     * @param waitOnScan true if the remove operation should wait for any in-progress scan to complete.
     */
    
    public final void removeAllModules (boolean waitOnScan)
    {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("PeriodicRecovery: removing all modules.");
        }
        
        if (waitOnScan) {
            // make sure any scan which might be using the module has completed
            synchronized (_stateLock) {
                    doScanningWait();
            }
        }
        
        _recoveryModules.clear();
    }

    /**
     * return a copy of the current recovery modules list
     *
     * @return a copy of the the recovery modules list.
     */

    public final Vector<RecoveryModule> getModules ()
    {
        // return a copy of the modules list so that clients are not affected by dynamic modifications to the list
        // synchronize so that we don't copy in the middle of an add or remove

        synchronized (_recoveryModules) {
            return new Vector<RecoveryModule>(_recoveryModules);
        }
    }
    /*
     * debugging aid
     */

    public Listener getListener()
    {
        return _listener;
    }

    /***** private implementation *****/

    /**
     * fetch the current activity status either INACTIVE or SCANNING
     *
     * <b>Caveats:</b> must only be called while synchronized on {@link PeriodicRecovery#_stateLock}
     * @return INACTIVE if no scan is in progress or SCANNING if some thread is performing a scan
     */
    private Status getStatus ()
    {
        return _currentStatus;
    }

    /**
     * fetch the current recovery operation mode either ENABLED, SUSPENDED or TERMINATED
     *
     * <b>Caveats:</b> must only be called while synchronized on {@link PeriodicRecovery#_stateLock}
     * @return the current recovery operation mode
     */
    public Mode getMode ()
    {
        return _currentMode;
    }

    /**
     * set the current activity status
     * @param status the new status to be used
     */
    private void setStatus (Status status)
    {
        _currentStatus = status;
    }

    /**
     * set the current recovery operation mode
     * @param mode the new mode to be used
     */
    private void setMode (Mode mode)
    {
        _currentMode = mode;
    }

    /**
     * wait for the required backoff period or less if the scanning status or scan mode changes
     *
     * <b>Caveats:</b> this must only be called when synchronized on {@link PeriodicRecovery#_stateLock} and when
     * _currentStatus is SCANNING and _currentMode is ENABLED
     */
    private void doBackoffWait()
    {
        try {
            _stateLock.wait(_backoffPeriod * 1000);
        } catch (InterruptedException e) {
            // we can ignore this exception
        }
    }

    /**
     * wait for the required recovery period or less if the scanning status or scan mode changes
     *
     * <b>Caveats:</b> this must only be called when synchronized on {@link PeriodicRecovery#_stateLock} and when
     * _currentStatus is INACTIVE and _currentMode is ENABLED
     */
    private void doPeriodicWait()
    {
        try {
            _stateLock.wait(_recoveryPeriod * 1000);
        } catch (InterruptedException e) {
            // we can ignore this exception
        }
    }

    /**
     * wait until the we move out of SUSPENDED mode
     *
     * <b>Caveats:</b> this must only be called when synchronized on {@link PeriodicRecovery#_stateLock}
     */
    private void doSuspendedWait()
    {
        while (getMode() == Mode.SUSPENDED) {
            try {
                _stateLock.wait();
            } catch (InterruptedException e) {
                // we can ignore this exception
            }
        }
    }

    /**
     * wait until some other thread stops scanning
     *
     * <b>Caveats:</b> this must only be called when synchronized on {@link PeriodicRecovery#_stateLock} and when
     * _currentStatus is SCANNING
     */
    private void doScanningWait()
    {
        while (getStatus() == Status.SCANNING) {
            try {
                _stateLock.wait();
            } catch (InterruptedException e) {
                // we can ignore this exception
            }
        }
    }

    /**
     * start performing a scan continuing to completion unless we are terminating
     *
     * <b>Caveats:</b> this must only be called when _currentStatus is SCANNING. on return _currentStatus is always
     * still SCANNING
     */

    private void doWorkInternal()
    {
        // n.b. we only get here if status is SCANNING

        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("Periodic recovery first pass at "+_theTimestamper.format(new Date()));
        }

        // n.b. this works on a copy of the modules list so it is not affected by
        // dynamic updates in the middle of a scan, ensuring first+second pass happen
        // for the same stable set of modules.

        Vector copyOfModules = getModules();
        
        Enumeration modules = copyOfModules.elements();

        while (modules.hasMoreElements())
        {
            RecoveryModule m = (RecoveryModule) modules.nextElement();

            // we need to ensure we use the class loader context of the recovery module while we are executing
            // its methods

            ClassLoader cl = switchClassLoader(m);
            try {
            m.periodicWorkFirstPass();
            } finally {
                restoreClassLoader(cl);
            }

            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debug(" ");
            }
        }

        // take the lock again so we can do a backoff wait on it

        synchronized (_stateLock) {
            // we have to wait for a bit to avoid catching (too many)
            // transactions etc. that are really progressing quite happily

            doBackoffWait();

            // we carry on scanning even if scanning is SUSPENDED because the suspending thread
            // might be waiting on us to complete and we don't want to risk deadlocking it by waiting
            // here for a resume.
            // if we have been TERMINATED we bail out now
            // n.b. if we give up here the caller is responsible for clearing the active scan

            if (getMode() == Mode.TERMINATED) {
                if (tsLogger.logger.isDebugEnabled()) {
                    tsLogger.logger.debug("PeriodicRecovery: scan TERMINATED at phase 1");
                }
                return;
            }
        }

        // move on to phase 2

        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("Periodic recovery second pass at "+_theTimestamper.format(new Date()));
        }

        modules = copyOfModules.elements();

        while (modules.hasMoreElements())
        {
            RecoveryModule m = (RecoveryModule) modules.nextElement();

            ClassLoader cl = switchClassLoader(m);
            try {
            m.periodicWorkSecondPass();
            } finally {
                restoreClassLoader(cl);
            }

            if (tsLogger.logger.isDebugEnabled()) {
                tsLogger.logger.debug(" ");
            }
        }

        // n.b. the caller is responsible for clearing the active scan
    }

    /**
     * notify the listener worker that a scan has completed
     *
     * <b>Caveats:</b> this must only be called when synchronized on {@link PeriodicRecovery#_stateLock} at the point
     * where Status transitions from SCANNING to INACTIVE
     */

    private void notifyWorker()
    {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("PeriodicRecovery: scan thread signals listener worker");
        }
        if(_workerService != null)
        {
            _workerService.notifyDone();
        }
        _workerScanRequested = false;
    }

    /**
     * install the classloader associated with some specific recovery module as the current thread's class loader
     *
     * this avoids a problem where the background periodic recovery thread can see the same class as the recovery
     * module's class loader, specifically where a the recovery module resides in a sar (e.g. the XTS code).
     * If class with name "A" is loaded via the background thread class loader as A' and used to create instance
     * a' then a cast expression in th erecovery code of the form (A)a' will try to resolve a' against version
     * A'' loaded via the sar loader and get a class cast exception.
     *
     * @param rm the recovery module whose class loader is to be installed as the new thread class loader
     * @return the class loader currently installed as the thread class loader
     */

    private ClassLoader switchClassLoader(RecoveryModule rm)
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader cl = currentThread.getContextClassLoader();

        currentThread.setContextClassLoader(rm.getClass().getClassLoader());
        return cl;
    }

    /**
     * restore the current thread's classloader
     *
     * @param cl the class loader to be set as the current thread class loader
     */

    private void restoreClassLoader(ClassLoader cl)
    {
        Thread currentThread = Thread.currentThread();

        currentThread.setContextClassLoader(cl);
    }

    /**
     * Load recovery modules prior to starting to recovery. These are loaded in list iteration order.
     */
    private void loadModules ()
    {
        Vector<String> moduleNames = new Vector<String>(recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryExtensions());

        for(String moduleName : moduleNames) {
            loadModule(moduleName);
        }
    }

    /**
     * load a specific recovery module and add it to the recovery modules list
     *
     * @param className
     */
   private void loadModule (String className)
   {
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("Loading recovery module " +
                   className);
       }

      if (className == null) {
          tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_1();

          return;
      }
      else
      {
         try
         {
	     Class c = Thread.currentThread().getContextClassLoader().loadClass( className );

            try
            {
               RecoveryModule m = (RecoveryModule) c.newInstance();
               _recoveryModules.add(m);
            }
            catch (ClassCastException e) {
                tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_2(className);
            }
            catch (IllegalAccessException iae) {
                tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_3(iae);
            }
            catch (InstantiationException ie) {
                tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_4(ie);
            }

            c = null;
         }
         catch ( ClassNotFoundException cnfe ) {
             tsLogger.i18NLogger.warn_recovery_PeriodicRecovery_5(className);
         }
      }
   }

    /**
     * initialise the periodic recovery instance to a suitable initial state
     */
   private void initialise ()
    {
        setStatus(Status.INACTIVE);
        setMode(Mode.ENABLED);

        _recoveryPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getPeriodicRecoveryPeriod();

        if (_recoveryPeriod != _defaultRecoveryPeriod &&  tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("com.arjuna.ats.arjuna.recovery.PeriodicRecovery" +
                    ": Recovery period set to " + _recoveryPeriod + " seconds");
        }

        _backoffPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod();

        if (_backoffPeriod != _defaultBackoffPeriod && tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("PeriodicRecovery" +
                    ": Backoff period set to " + _backoffPeriod + " seconds");
        }
    }

   // this refers to the modules specified in the recovery manager
   // property file which are dynamically loaded.
   /**
    * list of instances of RecoiveryModule either loaded during startup as specified in the recovery manager
    * property file or added dynamically by calls to addModule
    */
   private final Vector<RecoveryModule> _recoveryModules = new Vector<RecoveryModule>();

   /**
    * time in seconds between the first and second pass in any given scan
    */
   private int _backoffPeriod = 0;

    /**
     * time in seconds for which the periodic recovery thread waits between scan attempts
     */
   private int _recoveryPeriod = 0;

    /**
     *  default value for _backoffPeriod if not specified via RecoveryEnvironmentBean
     */
    public static final int _defaultBackoffPeriod = 10;

    /**
     *  default value for _recoveryPeriod if not specified via RecoveryEnvironmentBean
     */
    public static final int _defaultRecoveryPeriod = 120;

    /**
     * lock controlling access to {@link PeriodicRecovery#_currentStatus}, {@link PeriodicRecovery#_currentMode} and
     * {@link PeriodicRecovery#_workerScanRequested}
     */
   private final Object _stateLock = new Object();

    /**
     * activity status indicating whether we IDLING or some thread is SCANNING
     */
   private Status _currentStatus;

    /**
     * operating mode indicating whether scanning is ENABLED, SUSPENDED or TERMINATED
     */
   private Mode _currentMode;

    /**
     *  flag indicating whether the listener has prodded the recovery thread
     */
    private boolean _workerScanRequested = false;

    /**
     * format for printing dates in log messages
     */
    private SimpleDateFormat _theTimestamper = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * socket used by listener worker thread
     */
    private ServerSocket _socket = null;
    private final Object _socketLock = new Object();

    /**
     * listener thread running worker service
     */
    private Listener _listener = null;

    /**
     * the worker service which handles requests via the listener socket
     */
    private WorkerService _workerService = null;

   /*
    * Read the system properties to set the configurable options
    *
    * Note: if we start and stop the service then changes to the timeouts
    * won't be reflected. We will need to modify this eventually.
    */

   static
   {
   }

}








