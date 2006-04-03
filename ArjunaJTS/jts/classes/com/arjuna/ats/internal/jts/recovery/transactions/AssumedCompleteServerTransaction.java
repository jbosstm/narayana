/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AssumedCompleteServerTransaction.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.state.*;

import org.omg.CosTransactions.*;
import java.util.Date;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import org.omg.CORBA.SystemException;

/**
 * Transaction relic of a committed transaction that did not get committed responses from
 * all resources/subordinates.
 *
 * Recovery will not be attempted unless a replay completion is received, in which case it
 * is reactivated.
 * <P>
 * Several of the methods of OTS_Transaction could be simplified for an 
 * AssumedCompleteServerTransaction (e.g. the status must be committed), but they are kept the
 * same to simplify maintenance
 * <P>
 * @author Peter Furniss (peter.furniss@arjuna.com)
 * @version $Id: AssumedCompleteServerTransaction.java 2342 2006-03-30 13:06:17Z  $ 
 *
 * @message com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction_1 [com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction_1] - AssumedCompleteServerTransaction {0} created
 */
public class AssumedCompleteServerTransaction extends RecoveredServerTransaction
{
    public AssumedCompleteServerTransaction ( Uid actionUid )
    {
	super(actionUid,ourTypeName);
	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction_1", new Object[]{get_uid()});
	    }
    }

    
/**
 *  the original process must be deceased if we are assumed complete
 */
public Status getOriginalStatus()
{
    return Status.StatusNoTransaction;
}

public String type ()
    {
	return AssumedCompleteServerTransaction.typeName();
    }

  /**
   * typeName differs from original to force the ActionStore to 
   * keep AssumedCompleteServerTransactions separate
   */
public static String typeName ()
    {
	return ourTypeName;
    }

public String toString ()
    {
	return "AssumedCompleteServerTransaction < "+get_uid()+" >";
    }

/**
 * This T is already assumed complete, so return false
 */
public boolean assumeComplete()
    {
	return false;
    }

public Date getLastActiveTime()
{
    return _lastActiveTime;
}

public boolean restore_state (InputObjectState objectState, int ot)
{
    // do the other stuff
    boolean result = super.restore_state(objectState,ot);
    
    if (result) {
	try {
	    long oldtime = objectState.unpackLong();
	    _lastActiveTime = new Date(oldtime);
	} catch (java.io.IOException ex) {
	    // can assume the assumptionTime is missing - make it now
	    _lastActiveTime = new Date();
	}
    }
    return result;
}

public boolean save_state (OutputObjectState objectState, int ot)
{
    // do the other stuff
    boolean result = super.save_state(objectState,ot);
    
    if (result ) {
	// a re-write means we have just been active
	_lastActiveTime = new Date();
	try {
	    objectState.packLong(_lastActiveTime.getTime());
	} catch (java.io.IOException ex) {
	}
    }
    return result;

}

private Date _lastActiveTime;

private static String ourTypeName = "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteServerTransaction";
    
}
