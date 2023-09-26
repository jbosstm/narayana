/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
    @SuppressWarnings("unused")
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