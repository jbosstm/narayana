/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict;

import org.omg.CosTransactions.Coordinator;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerTopLevelAction;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerStrictTopLevelAction extends ServerTopLevelAction
{
    /*
     * The base class is responsible for registering with
     * the parent transaction.
     */
    
public ServerStrictTopLevelAction (ServerControl control, boolean doRegister)
    {
	super(control);

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerStrictTopLevelAction::ServerStrictTopLevelAction (ServerControl, " + doRegister + " )");
    }

        _theResource = null;
	
	if (_theControl != null)
	{
	    _theResource = new org.omg.CosTransactions.ResourcePOATie(this);
	    
	    ORBManager.getPOA().objectIsReady(_theResource);
	    
	    /*
	     * Would like to be able to attach a thread filter
	     * to this object if process-filters aren't supported.
	     * However, currently this won't work as we can't have
	     * two different filter types working at the same
	     * time.
	     *
	     *		ATTACH_THREAD_FILTER_(_theResource);
	     */
	    
	    if (doRegister)
		interposeResource();
	}
    }

public boolean interposeResource ()
    {
	if (!_registered)
	{
	    _registered = true;

	    if ((_theResource != null) && (_theControl != null))
	    {
		Coordinator realCoordinator = _theControl.originalCoordinator();

		if (!(_valid = registerResource(realCoordinator))) {
//            jtsLogger.i18NLogger.warn_orbspecific_interposition_resources_strict_iptlfailed("ServerStrictNestedAction");

            /*
                * Failed to register. Valid is set, and the interposition
                * controller will now deal with this.
                */
        }

		realCoordinator = null;
	    }
	    else
		_valid = false;
	}

	return _valid;
    }

public String type ()
    {
	return "/Resources/Arjuna/ServerTopLevelAction/ServerStrictTopLevelAction";
    }

}