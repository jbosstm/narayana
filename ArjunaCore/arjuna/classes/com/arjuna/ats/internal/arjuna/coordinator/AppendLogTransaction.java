/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.TopLevelAction;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AppendLogTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 4.1.
 */

/**
 * Needs further consideration and then completion.
 */

// may no longer be needed due to TxLog work.

public class AppendLogTransaction extends TopLevelAction
{

    public final boolean setLoggedTransaction (AppendLogTransaction previous)
    {
	if (_previous == null)
	{
	    _previous = previous;

	    super.add(new com.arjuna.ats.internal.arjuna.abstractrecords.DisposeRecord(previous.getStore(), previous));

	    return true;
	}
	else
	    return false;
    }

    public String type ()
    {
	return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/AppendLogTransaction";
    }

    protected void updateState ()
    {
	if (_previous == null)
	    super.savedIntentionList = false;
	
	super.updateState();
    }
    
    private AppendLogTransaction _previous;
    
}