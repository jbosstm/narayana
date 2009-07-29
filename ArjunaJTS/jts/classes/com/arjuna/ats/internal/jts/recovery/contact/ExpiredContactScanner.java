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

import org.omg.CosTransactions.*;

import java.util.*;
import java.text.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.recovery.*;

import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;


/**
 * This class is a plug-in module for the recovery manager.  This
 * class is responsible for the removing contact items that are too old
 *
 * @message com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_1 [com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_1] ExpiredContactScanner created, with expiry time of {0} seconds
 * @message com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_2 [com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_2] ExpiredContactScanner - scanning to remove items from before {0}
 * @message com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_3 [com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_3] Removing old contact item {0}
 * @message com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_4 [com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_4] Expiry scan interval set to {0} seconds
 * @message com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_5 [com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_5] {0} has inappropriate value {1}
 */
public class ExpiredContactScanner implements ExpiryScanner
{
    public ExpiredContactScanner ()
    {

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					   FacilityCode.FAC_CRASH_RECOVERY,
					   "com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_1", 
					   new Object[]{Integer.toString(_expiryTime)});
	    }
	_objectStore  = FactoryContactItem.getStore();
	_itemTypeName = FactoryContactItem.getTypeName();
    
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void scan ()
    {

	// calculate the time before which items will be removed
	Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000);

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					   FacilityCode.FAC_CRASH_RECOVERY,
					   "com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_2", 
					   new Object[]{_timeFormat.format(oldestSurviving)});
	    }
	try
	{

	    InputObjectState uids = new InputObjectState();
	    
	    // find the uids of all the contact items
	    if (_objectStore.allObjUids(_itemTypeName, uids))
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
			
			FactoryContactItem anItem = FactoryContactItem.recreate(newUid);
			if (anItem != null) 
			{
			    Date timeOfDeath = anItem.getDeadTime();
			    if (timeOfDeath != null && timeOfDeath.before(oldestSurviving)) 
			    {
				jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_3", new Object[]{newUid});
				_objectStore.remove_committed(newUid, _itemTypeName);
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
    private ObjectStore _objectStore;
    private static int _expiryTime = 12 *60*60; // default is 12 hours
    private static SimpleDateFormat    _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    static
    {
        _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerExpiryTime() * 60 * 60;

        if (jtsLogger.loggerI18N.isDebugEnabled())
        {
            jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
                    FacilityCode.FAC_CRASH_RECOVERY,
                    "com.arjuna.ats.internal.jts.recovery.ExpiredContactScanner_4",
                    new Object[]{Integer.toString(_expiryTime)});
        }
    }

}
