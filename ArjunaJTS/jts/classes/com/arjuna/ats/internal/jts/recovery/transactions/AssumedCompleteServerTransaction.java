/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */




package com.arjuna.ats.internal.jts.recovery.transactions;

import java.util.Date;

import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jts.logging.jtsLogger;

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
 */

public class AssumedCompleteServerTransaction extends RecoveredServerTransaction
{
    public AssumedCompleteServerTransaction ( Uid actionUid )
    {
	super(actionUid,ourTypeName);
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("AssumedCompleteServerTransaction "+get_uid()+" created");
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