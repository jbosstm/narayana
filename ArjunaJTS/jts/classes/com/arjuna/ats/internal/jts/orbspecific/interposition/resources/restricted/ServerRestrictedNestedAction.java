/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted;

import java.util.List;

import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerNestedAction;
import com.arjuna.ats.jts.exceptions.TxError;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerRestrictedNestedAction extends ServerNestedAction
{

    /*
     * Create local transactions with same ids as remote.
     */

public ServerRestrictedNestedAction (ServerControl myControl)
    {
	super(myControl);

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerRestrictedNestedAction::ServerRestrictedNestedAction ( " + _theUid + " )");
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
            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_restricted_contxfound_1(
                    "ServerRestrictedNestedAction.child", Integer.toString(children.size()));

            throw new TxError(jtsLogger.i18NLogger.get_orbspecific_interposition_resources_restricted_contx_1());
        }
        else
        {
            if (children.size() == 1)
                toReturn = (ServerRestrictedNestedAction) children.remove(0);
        }

        return toReturn;
    }

}