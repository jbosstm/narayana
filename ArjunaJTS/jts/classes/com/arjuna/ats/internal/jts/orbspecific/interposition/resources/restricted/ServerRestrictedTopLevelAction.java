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
 * $Id: ServerRestrictedTopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.internal.arjuna.template.*;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.interposition.*;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

import java.util.List;

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

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerRestrictedTopLevelAction::ServerRestrictedTopLevelAction ( "+
					       ((myControl != null) ? myControl.get_uid() : Uid.nullUid())+" )");
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

    /**
     * @message com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.contxfound_3 {0} - found concurrent transactions!
     * @message com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.contx_4 {0} Concurrent children found for restricted interposition!
     */
    public final synchronized ServerRestrictedNestedAction child ()
    {
        ServerRestrictedNestedAction toReturn = null;
        List<ServerNestedAction> children = getChildren();

        // There should be only one child!
        if (children.size() != 1)
        {
            if (jtsLogger.loggerI18N.isWarnEnabled())
            {
                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.contxfound_3",
                        new Object[] {"ServerRestrictedTopLevelAction.child"});
            }

            throw new com.arjuna.ats.jts.exceptions.TxError(jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.contx_4"));
        }
        else
        {
            toReturn = (ServerRestrictedNestedAction) children.remove(0);
        }

        return toReturn;
    }

public String type ()
    {
	return "/Resources/Arjuna/ServerTopLevelAction/ServerRestrictedTopLevelAction";
    }

}


