/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna;

import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerRecoveryTopLevelAction extends ServerTopLevelAction
{

    public ServerRecoveryTopLevelAction (ServerControl control)
    {
	super();

	_theControl = control;

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ServerRecoveryTopLevelAction::ServerRecoveryTopLevelAction ( " + _theUid + " )");
    }
    }

    /*
     * Basically a null-op to prevent us trying to remove
     * this resource from the interposition list. Since it
     * gets created explicitly for crash recovery, it doesn't
     * get added to the list.
     */

    protected synchronized void destroyResource ()
    {
	if (!_destroyed)
	{
	    _destroyed = true;
	}
    }

}