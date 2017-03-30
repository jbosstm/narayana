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
 * $Id: ServerStrictTopLevelAction.java 2342 2006-03-30 13:06:17Z  $
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
