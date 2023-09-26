/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted;

import java.util.List;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerNestedAction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerTopLevelAction;
import com.arjuna.ats.jts.exceptions.TxError;
import com.arjuna.ats.jts.logging.jtsLogger;

/*
 * This looks like an atomic action, but is not actually derived from
 * BasicAction or Transaction. This is because of the way in which the
 * OTS creates and manipulates transactions.
 *
 * As with Transaction, we only create actions here, and do not associated
 * these contexts with any thread. We do the association later.
 *
 * If we were to do the creation via a suitably modified current interface
 * then the thread association would be done for us automatically, and we
 * would not have to call resume at all.
 *
 * This is a top-level action proxy.
 */

public class ServerRestrictedTopLevelAction extends ServerTopLevelAction
{

public ServerRestrictedTopLevelAction (ServerControl myControl)
    {
	super(myControl);

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerRestrictedTopLevelAction::ServerRestrictedTopLevelAction ( " +
                ((myControl != null) ? myControl.get_uid() : Uid.nullUid()) + " )");
    }
    }

public final synchronized ServerControl deepestControl ()
    {
	ServerRestrictedNestedAction myChild = child();

	if (myChild != null)
	    return myChild.deepestControl();
	else
	    return control();
    }

    public final synchronized ServerRestrictedNestedAction child ()
    {
        ServerRestrictedNestedAction toReturn = null;
        List<ServerNestedAction> children = getChildren();

        // There should be only one child!
        if (children.size() > 1) {
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_restricted_contxfound_3(
                    "ServerRestrictedTopLevelAction.child", Integer.toString(children.size()));

            throw new TxError(jtsLogger.i18NLogger.get_orbspecific_interposition_resources_restricted_contx_4(Integer.toString(children.size())));
        }
        else
        {
            if (children.size() == 1)
                toReturn = (ServerRestrictedNestedAction) children.remove(0);
        }

        return toReturn;
    }

public String type ()
    {
	return "/Resources/Arjuna/ServerTopLevelAction/ServerRestrictedTopLevelAction";
    }

}