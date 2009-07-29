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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExpiredAssumedCompleteScanner.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import org.omg.CosTransactions.*;

import java.util.*;
import java.text.*;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner ;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment ;
import com.arjuna.ats.arjuna.state.InputObjectState ;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

/**
 * Implementation of {@link com.arjuna.CosRecovery.ExpiryScanner} for removing
 * relics of transactions that have been assumed complete. Instances identify the
 * particular object type to be scanned for.
 * <p>Expiry time is determined by property ASSUMED_COMPLETE_EXPIRY_TIME.
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_1 [com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_1] - ExpiredAssumedCompleteScanner created, with expiry time of {0}  seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_2 [com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_2] - ExpiredAssumedCompleteScanner - scanning to remove items from before {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_3 [com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_3] - Removing old assumed complete transaction {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_4 [com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_4] - Expiry scan interval set to {0} seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_5 [com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_5] - {0}  has inappropriate value ({1})
 */

public class ExpiredAssumedCompleteScanner implements ExpiryScanner
{
    private ExpiredAssumedCompleteScanner ()
    {
	// unused
    }

    protected ExpiredAssumedCompleteScanner (String typeName, ObjectStore objectStore)
    {

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					     FacilityCode.FAC_CRASH_RECOVERY, 
					     "com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_1",
					     new Object[]{Integer.toString(_expiryTime)});
	    }
	
	_objectStore  = objectStore;
	_typeName = typeName;
	
    }

    public void scan ()
    {

	// calculate the time before which items will be removed
	Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000);


	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					      FacilityCode.FAC_CRASH_RECOVERY, 
					      "com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_2",
					      new Object[]{_timeFormat.format(oldestSurviving)});
	    }

	try
	{

	    InputObjectState uids = new InputObjectState();
	    
	    // find the uids of all the contact items
	    if (_objectStore.allObjUids(_typeName, uids))
	    {
		Uid theUid = new Uid(Uid.nullUid());

		boolean endOfUids = false;

		while (!endOfUids)
		{
		    // extract a uid
		    theUid.unpack(uids);

		    if (theUid.equals(Uid.nullUid()))
			endOfUids = true;
		    else
		    {
			Uid newUid = new Uid(theUid);
			RecoveringTransaction aTransaction = null;
			if (_typeName == AssumedCompleteTransaction.typeName()) {
			    aTransaction = new AssumedCompleteTransaction(newUid);
			} else if (_typeName == AssumedCompleteServerTransaction.typeName()) {
			    aTransaction = new AssumedCompleteServerTransaction(newUid);
			} 
			// ignore imaginable logic error of it being neither
			if (aTransaction != null) 
			{
			    Date timeLastActive = aTransaction.getLastActiveTime();
			    if (timeLastActive != null && timeLastActive.before(oldestSurviving)) 
			    {
			
				if (jtsLogger.loggerI18N.isInfoEnabled())
				    {
					jtsLogger.loggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3", new Object[]{newUid});
				    }
				
				_objectStore.remove_committed(newUid, _typeName);
			    }
			}
		    }
		}
	    }
	}
	catch (Exception e)
	{
	    // end of uids!
	}
    }
    /**
     * @returns false if the expiry time is zero (i.e. zero means do not expire)
     */
    public boolean toBeUsed()
    {
	return _expiryTime != 0;
    }

    private String	 _typeName;
    private ObjectStore _objectStore;
    private static int _expiryTime = 240 *60*60; // default is 240 hours
    private static SimpleDateFormat    _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    static
    {
        _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerExpiryTime() * 60 * 60;

        if (jtsLogger.loggerI18N.isDebugEnabled())
        {
            jtsLogger.loggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                    FacilityCode.FAC_CRASH_RECOVERY,
                    "com.arjuna.ats.internal.arjuna.recovery.ExpiredAssumedCompleteScanner_4",
                    new Object[]{Integer.toString(_expiryTime)});
        }
    }
}
