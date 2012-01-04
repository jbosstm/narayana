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
 * $Id: ExpiredEntryMonitor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;

/**
 * Threaded object to run {@link ExpiryScanner} implementations to scan 
 * the action store to remove items deemed expired by some algorithm.
 * Performs a scan at interval defined by the property 
 * com.arjuna.ats.arjuna.recovery.expiryScanInterval (hours).
 * ExpiryScanner implementations are registered as properties beginning with
 * "com.arjuna.ats.arjuna.recovery.expiryScanner".
 * <P>
 * Singleton, instantiated in the RecoveryManager. 
 * <P>
 */

public class ExpiredEntryMonitor extends Thread
{
    /**
    *  Start the monitor thread, if the properties make it appropriate
    */

  public static synchronized boolean startUp()
  {
      // ensure singleton
      if ( _theInstance != null )
      {
	  return false;
      }

      // process system properties -- n.b. we only do this once!

      if (!initialised) {
          initialise();
      }
      
      if ( _scanIntervalSeconds == 0 )
      {
	  // no scanning wanted
	      
	  if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("Expiry scan zero - not scanning");
      }
	  
	  return false;
      }

      if ( _expiryScanners.size() == 0 )
      {
	  // nothing to do
	  
	  if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("No Expiry scanners loaded - not scanning");
      }
	  
	  return false;
      }
      
      // create, and thus launch the monitor

      _theInstance = new ExpiredEntryMonitor(_skipFirst);

      _theInstance.start();
      
      return true;
  }

    /**
     * terminate any currently active monitor thread, cancelling any further scans but waiting for the
     * thread to exit before returning
     */
  public synchronized static void shutdown()
  {
      if (_theInstance != null) {
          _theInstance.terminate();
          // now wait for it to finish
          try {
              _theInstance.join();
          } catch (InterruptedException e) {
              // ignore
          }
      }

      _theInstance = null;
  }

  private ExpiredEntryMonitor(boolean skipFirst)
  {
      super ("Transaction Expired Entry Monitor");
    if (tsLogger.logger.isDebugEnabled()) {
        tsLogger.logger.debug("ExpiredEntryMonitor - constructed");
    }
    _skipNext = skipFirst;
    _stop = false;

    this.setDaemon(true);
  }
    
  /**
   * performs periodic scans until a shutdwn is notified
   */
  public void run()
  {
    while( true )
    {
	    tsLogger.i18NLogger.info_recovery_ExpiredEntryMonitor_12(_theTimestamper.format(new Date()));
	
	if (_skipNext)
    {
        // make sure we skip at most one scan

        _skipNext = false;

         tsLogger.i18NLogger.info_recovery_ExpiredEntryMonitor_5();
    }
    else
	{
	    Enumeration scanners = _expiryScanners.elements();
	    
	    while ( scanners.hasMoreElements() )
	    {
		ExpiryScanner m = (ExpiryScanner)scanners.nextElement();

            // check for a shutdown request before starting a scan
            synchronized (this) {
                if (_stop) {
                    break;
                }
            }

            // ok go ahead and scan

		m.scan();
			
		if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("  ");
            // bit of space if detailing
        }
        }
	}

	// wait for a bit to avoid catching (too many) transactions etc. that
	// are really progressing quite happily

	try
	{
        // check for shutdown request before we sleep
        synchronized (this) {
        if (_stop) {
            break;
        }
	    wait( _scanIntervalSeconds * 1000 );
        // check if we were woken because of a shutdown
        if (_stop) {
            break;
        }
        }
	}
	catch ( InterruptedException e1 )
	{
        // we should only get shut down by a shutdown request so ignore interrupts
	}
    }
  }

  private synchronized void terminate()
  {
      _stop = true;
      notify();
  }

    private static void initialise()
    {
        /*
         * Read the system properties to set the configurable options
         */

        _scanIntervalSeconds = recoveryPropertyManager.getRecoveryEnvironmentBean().getExpiryScanInterval() * 60 * 60;

        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("Expiry scan interval set to "+Integer.toString(_scanIntervalSeconds)+" seconds");
        }

        if (_scanIntervalSeconds != 0)
        {

            // is it being used to skip the first time
            if ( _scanIntervalSeconds < 0 )
            {
                _skipFirst = true;
                _scanIntervalSeconds = - _scanIntervalSeconds;
            }

            loadScanners();
        }

        initialised = true;
    }

    private static void loadScanners()
    {
        _expiryScanners = new Vector();

        for(ExpiryScanner scanner : recoveryPropertyManager.getRecoveryEnvironmentBean().getExpiryScanners()) {
            if ( scanner.toBeUsed() ) {
                _expiryScanners.add( scanner );
            }
        }
    }

    /**
     * flag which causes the next scan to be skipped if it is true. this is set from _skipFirst when a
     * monitor is created and rest to false each time a scan is considered.
     */
    private boolean _skipNext;

    /**
     * flag which causes the monitor thread to stop running when it is set to true
     */
    private boolean _stop;

    /**
     * list of scanners to be invoked by the monitor thread in order to check for expired log entries
     */
    private static Vector _expiryScanners;

    /**
     * flag which guards processing of properties ensuirng it is only performed once
     */
    private static boolean initialised = false;

    /**
     * the default scanning interval if the property file does not supply one
     */
    private static int _scanIntervalSeconds = 12 * 60 * 60;

    /**
     * a date format used to log the time for a scan
     */
    private static SimpleDateFormat _theTimestamper = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * a flag which if true causes the scanner to perform a scan when it is first starts or if false skip this
     * first scan. it can be set to true by supplying a negative scan interval in the property file.
     */
    private static boolean _skipFirst = false;

    /**
     * the currently active monitor instance or null if no scanner is active
     */
    private static ExpiredEntryMonitor _theInstance = null;
}