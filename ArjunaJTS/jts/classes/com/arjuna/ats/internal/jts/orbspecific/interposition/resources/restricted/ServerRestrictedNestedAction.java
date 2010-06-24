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
 * $Id: ServerRestrictedNestedAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted;

import com.arjuna.ats.jts.exceptions.TxError;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;

import java.util.List;

public class ServerRestrictedNestedAction extends ServerNestedAction
{

    /*
     * Create local transactions with same ids as remote.
     */

public ServerRestrictedNestedAction (ServerControl myControl)
    {
	super(myControl);

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("ServerRestrictedNestedAction::ServerRestrictedNestedAction ( " + _theUid + " )");
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
