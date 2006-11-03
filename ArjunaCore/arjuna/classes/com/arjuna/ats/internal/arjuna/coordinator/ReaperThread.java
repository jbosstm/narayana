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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ReaperThread.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

import com.arjuna.common.util.logging.*;

/**
 * Class to record transactions with non-zero timeout values, and
 * class to implement a transaction reaper thread which terminates
 * these transactions once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ReaperThread.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ReaperThread extends Thread
{

public ReaperThread (TransactionReaper arg)
    {
	reaperObject = arg;
	sleepPeriod = reaperObject.checkingPeriod();
	_shutdown = false;
    }
    
    /**
     * @message com.arjuna.ats.internal.arjuna.coordinator.ReaperThread_1 [com.arjuna.ats.internal.arjuna.coordinator.ReaperThread_1] - Thread {0} sleeping for {1}
     */

public void run ()
    {
    	if (tsLogger.arjLogger.debugAllowed())
    	{
    	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
    				     FacilityCode.FAC_ATOMIC_ACTION, "ReaperThread.run ()");
    	}
    	
    	for (;;)
    	{
    	    /*
    	     * Cannot assume we sleep for the entire period. We may
    	     * be interrupted. If we are, just run a check anyway and
    	     * ignore.
    	     */
    
            synchronized(reaperObject)
            {
        		sleepPeriod = reaperObject.checkingPeriod();
        
                if (sleepPeriod > 0)
                {
            		try
            		{
            		    if (tsLogger.arjLoggerI18N.isDebugEnabled())
            		    {
            		        tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
            						     FacilityCode.FAC_ATOMIC_ACTION,
            						     "com.arjuna.ats.internal.arjuna.coordinator.ReaperThread_1", 
            						     new Object[]{Thread.currentThread(),
            								  Long.toString(sleepPeriod)});
            		    }
            
            		    reaperObject.wait(sleepPeriod);
            		}
            		catch (InterruptedException e1) {}
                }
            }
    
    	    if (_shutdown)
    	        return;
    
    	    reaperObject.check();
    	}
    }

    public void shutdown ()
    {
	_shutdown = true;
    }

    private TransactionReaper reaperObject;
    private long              sleepPeriod;
    private boolean           _shutdown;

    
}
