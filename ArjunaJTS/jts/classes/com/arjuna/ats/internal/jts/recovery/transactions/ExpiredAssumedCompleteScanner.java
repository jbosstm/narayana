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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Implementation of {@link com.arjuna.CosRecovery.ExpiryScanner} for removing
 * relics of transactions that have been assumed complete. Instances identify the
 * particular object type to be scanned for.
 * <p>Expiry time is determined by property ASSUMED_COMPLETE_EXPIRY_TIME.
 *
 */

public class ExpiredAssumedCompleteScanner implements ExpiryScanner
{
    private ExpiredAssumedCompleteScanner ()
    {
	// unused
    }

    protected ExpiredAssumedCompleteScanner (String typeName, RecoveryStore recoveryStore)
    {

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ExpiredAssumedCompleteScanner created, with expiry time of "+_expiryTime+" seconds");
    }
	
	_recoveryStore = recoveryStore;
	_typeName = typeName;
	
    }

    public void scan ()
    {

	// calculate the time before which items will be removed
	Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000);


	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ExpiredAssumedCompleteScanner - scanning to remove items from before "+_timeFormat.format(oldestSurviving));
    }

	try
	{

	    InputObjectState uids = new InputObjectState();
	    
	    // find the uids of all the contact items
	    if (_recoveryStore.allObjUids(_typeName, uids))
	    {
		Uid theUid = null;

		boolean endOfUids = false;

		while (!endOfUids)
		{
		    // extract a uid
		    theUid = UidHelper.unpackFrom(uids);

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

                    jtsLogger.i18NLogger.info_arjuna_recovery_ExpiredAssumedCompleteScanner_3(newUid);
				
				_recoveryStore.remove_committed(newUid, _typeName);
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
    private RecoveryStore _recoveryStore;
    private static final int _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean()
            .getTransactionStatusManagerExpiryTime() * 60 * 60;
    private static final SimpleDateFormat _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
}
