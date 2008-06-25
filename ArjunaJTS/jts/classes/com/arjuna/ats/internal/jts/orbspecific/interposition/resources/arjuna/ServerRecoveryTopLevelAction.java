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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerRecoveryTopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.UNKNOWN;

public class ServerRecoveryTopLevelAction extends ServerTopLevelAction
{

    public ServerRecoveryTopLevelAction (ServerControl control)
    {
	super();

	_theControl = control;

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				   com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerRecoveryTopLevelAction::ServerRecoveryTopLevelAction ( "+_theUid+" )");
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
