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
 * $Id: ExpiredContactScanner.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.contact;

import java.util.*;
import java.text.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.recovery.*;

import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.jts.logging.jtsLogger;


/**
 * This class is a plug-in module for the recovery manager.  This
 * class is responsible for the removing contact items that are too old
 */
public class ExpiredContactScanner implements ExpiryScanner
{
    public ExpiredContactScanner ()
    {

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ExpiredContactScanner created, with expiry time of "+_expiryTime+" seconds");
    }
	_recoveryStore = StoreManager.getRecoveryStore();
	_itemTypeName = FactoryContactItem.getTypeName();
    
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void scan ()
    {

	// calculate the time before which items will be removed
	Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000);

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ExpiredContactScanner - scanning to remove items from before "+_timeFormat.format(oldestSurviving));
    }
	try
	{

	    InputObjectState uids = new InputObjectState();
	    
	    // find the uids of all the contact items
	    if (_recoveryStore.allObjUids(_itemTypeName, uids))
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
			
			FactoryContactItem anItem = FactoryContactItem.recreate(newUid);
			if (anItem != null) 
			{
			    Date timeOfDeath = anItem.getDeadTime();
			    if (timeOfDeath != null && timeOfDeath.before(oldestSurviving)) 
			    {
                    jtsLogger.i18NLogger.info_recovery_ExpiredContactScanner_3(newUid);
				_recoveryStore.remove_committed(newUid, _itemTypeName);
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
    
    public boolean toBeUsed()
    {
	return _expiryTime != 0;
    }

    private String	 _itemTypeName;
    private RecoveryStore _recoveryStore;
    private static int _expiryTime = 12 *60*60; // default is 12 hours
    private static SimpleDateFormat    _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    static
    {
        _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerExpiryTime() * 60 * 60;

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("Expiry scan interval set to "+_expiryTime+" seconds");
        }
    }

}
