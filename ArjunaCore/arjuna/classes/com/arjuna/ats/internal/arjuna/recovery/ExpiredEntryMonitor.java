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

import java.text.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

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
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1] - Expiry scan interval set to {0} seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2] - Expiry scan zero - not scanning
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3] - No Expiry scanners loaded - not scanning
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4] - ExpiredEntryMonitor - constructed
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5] - ExpiredEntryMonitor - no scans on first iteration
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6] - Loading expiry scanner {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7] - Attempt to load expiry scanner with null class name!
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8] - Loading expiry scanner {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9] - Expiry scanner {0} does not conform to ExpiryScanner interface
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10] - Loading expiry scanner: could not find class {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_11 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_11] - {0} has inappropriate value ({1})
 */

public class ExpiredEntryMonitor extends Thread
{
   /**
    *  Start the monitor thread, if the properties make it appropriate
    */

  public static boolean startUp()
  {
      // ensure singleton
      if ( _started )
      {
	  return false;
      }
      
      /*
       * Read the system properties to set the configurable options
       */
      
      String scanIntervalString = 
	  arjPropertyManager.propertyManager.getProperty( RecoveryEnvironment.EXPIRY_SCAN_INTERVAL );
      
      if ( scanIntervalString != null )
	  {
	      try
		  {
		      Integer scanIntervalInteger = new Integer(scanIntervalString);
		      // convert to seconds
		      _scanIntervalSeconds = scanIntervalInteger.intValue() * 60 * 60;
		      
		      if (tsLogger.arjLoggerI18N.debugAllowed())
		      {
			  tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							FacilityCode.FAC_CRASH_RECOVERY, 
							"com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1", 
							new Object[]{Integer.toString(_scanIntervalSeconds)});
		      }
		  }
	      catch ( NumberFormatException e )
	      {
		  if (tsLogger.arjLoggerI18N.isWarnEnabled())
		  {
		      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_11", 
						  new Object[]{RecoveryEnvironment.EXPIRY_SCAN_INTERVAL,
							       scanIntervalString});
		  }
	      }
	  }
      
      if ( _scanIntervalSeconds == 0 )
      {
	  // no scanning wanted
	      
	  if (tsLogger.arjLoggerI18N.debugAllowed())
	  {
	      tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					    FacilityCode.FAC_CRASH_RECOVERY, 
					    "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2");
	  }
	  
	  return false;
      }
      
      // is it being used to skip the first time
      if ( _scanIntervalSeconds < 0 )
      {
	  notSkipping = false;
	  _scanIntervalSeconds = - _scanIntervalSeconds;
      }
      
      loadScanners();
      
      if ( _expiryScanners.size() == 0 )
      {
	  // nothing to do
	  
	  if (tsLogger.arjLoggerI18N.isDebugEnabled())
	  {
	      tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					    FacilityCode.FAC_CRASH_RECOVERY, 
					    "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3");
	  }
	  
	  return false;
      }
      
      // create, and thus launch the monitor
      _theInstance = new ExpiredEntryMonitor();
      
      return _started;
  }
    
  public static void shutdown()
  {
      _started = false;
      _expiryScanners = new Vector();
      _scanIntervalSeconds = 12 * 60 * 60;
      notSkipping = true;
      
      _theInstance.interrupt();
      _theInstance = null;
  }

  private ExpiredEntryMonitor()
  {
    if (tsLogger.arjLoggerI18N.isDebugEnabled())
    {
	tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
				      FacilityCode.FAC_CRASH_RECOVERY, 
				      "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4");
    }
    
    _started = true;

    this.setDaemon(true);

    start();
  }
    
  /**
   * Start the background thread to perform the periodic scans
   */
  public void run()
  {
    while( true )
    {
	if (tsLogger.arjLogger.isInfoEnabled())
	{
	    tsLogger.arjLogger.info("\n  --- ExpiredEntryMonitor ----" + 
				      _theTimestamper.format(new Date()) + "----" );
	}
	
	if ( notSkipping )
	{
	    Enumeration scanners = _expiryScanners.elements();
	    
	    while ( scanners.hasMoreElements() )
	    {
		ExpiryScanner m = (ExpiryScanner)scanners.nextElement();

		m.scan();
			
		if (tsLogger.arjLogger.isDebugEnabled())
		{
		    tsLogger.arjLogger.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					      FacilityCode.FAC_CRASH_RECOVERY,"  "); 
		    // bit of space if detailing
		}
	    }
	}
	else
	{
	    if (tsLogger.arjLoggerI18N.isInfoEnabled())
	    {
		tsLogger.arjLoggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5");
	    }
	    
	    notSkipping = true;
	}
	
	// wait for a bit to avoid catching (too many) transactions etc. that
	// are really progressing quite happily

	try
	{
	    Thread.sleep( _scanIntervalSeconds * 1000 );
	}
	catch ( InterruptedException e1 )
	{
	    break;
	}
	
	if ( !_started )
            return;
    }
  }
    
  private static void loadScanners()
  {
    // search our properties
    Properties properties = arjPropertyManager.propertyManager.getProperties();
    
    if (properties != null)
    {
	Enumeration names = properties.propertyNames();
	
	while (names.hasMoreElements())
	{
	    String propertyName = (String) names.nextElement();
	    
	    if ( propertyName.startsWith(RecoveryEnvironment.SCANNER_PROPERTY_PREFIX) )
	    {
		loadScanner( properties.getProperty(propertyName) );
	    }
	}
    }
  }
    
  private static void loadScanner( String className )
  {
      if (tsLogger.arjLoggerI18N.isDebugEnabled())
      {
	  tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
					FacilityCode.FAC_CRASH_RECOVERY, 
					"com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
					new Object[]{className});
      }
      
      if (className == null)
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7");
	  
	  return;
      }
      else
      {
          try
	  {
	      Class c = Thread.currentThread().getContextClassLoader().loadClass( className );
		
		try
		{
		   ExpiryScanner m = (ExpiryScanner) c.newInstance();
		   
		   if ( m.toBeUsed() )
		   {
			   _expiryScanners.add( m );
		   }
		   else
		   {
		       if (tsLogger.arjLoggerI18N.isDebugEnabled())
		       {
			   tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
							FacilityCode.FAC_CRASH_RECOVERY, 
							"com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8",
							new Object[]{className});
		       }
		   }
		}
		catch (ClassCastException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9", 
							new Object[]{className});
			}
		}
		catch (IllegalAccessException e1)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
						    new Object[]{e1});
		    }
		}
		catch (InstantiationException e2)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
						    new Object[]{e2});
		    }
		}
		
		c = null;
	  }
	  catch (ClassNotFoundException e)
	  {
	      if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      {
		  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10", 
					      new Object[]{className});
	      }
	  }
      }
  }
    
    private static boolean _started = false;
    
    private static Vector _expiryScanners = new Vector();
    
    private static int _scanIntervalSeconds = 12 * 60 * 60;
    
    private static SimpleDateFormat _theTimestamper = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    
    private static boolean notSkipping = true;
    
    private static ExpiredEntryMonitor _theInstance = null;
    
}




