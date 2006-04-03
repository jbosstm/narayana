/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: ServerOSITopLevelAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict.*;
import com.arjuna.ats.internal.jts.interposition.resources.osi.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

public class ServerOSITopLevelAction extends ServerStrictTopLevelAction
{
    
    /*
     * The ServerTopLevelAction is responsible for registering
     * this resource with its parent.
     */

public ServerOSITopLevelAction (ServerControl control,
				    boolean doRegister)
    {
	super(control, doRegister);

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::ServerOSITopLevelAction ( ServerControl, "+doRegister+" )");
	}
    }

/*
 * Will only be called by the remote top-level transaction.
 */

public org.omg.CosTransactions.Vote prepare () throws HeuristicMixed, HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::prepare for "+_theUid);
	}

	/*
	 * First remove entry for this transaction otid
	 * from map. Have to do it here as we are going
	 * to be deleted by the base class!
	 */
    
	OTIDMap.remove(get_uid());

	return super.prepare();
    }

public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::rollback for "+_theUid);
	}

	OTIDMap.remove(get_uid());
    
	super.rollback();
    }

public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::commit for "+_theUid);
	}

	OTIDMap.remove(get_uid());
    
	super.commit();
    }

public void forget () throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::forget for "+_theUid);
	}

	OTIDMap.remove(get_uid());

	super.forget();
    }

/*
 * Just because commit_one_phase is called by the coordinator does not
 * mean that we can use it - we may have many locally registered resources.
 */

public void commit_one_phase () throws HeuristicHazard, SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ServerOSITopLevelAction::commit_one_phase for "+_theUid);
	}

	OTIDMap.remove(get_uid());

	super.commit_one_phase();
    }

public String type ()
    {
	return "/Resources/Arjuna/ServerTopLevelAction/ServerOSITopLevelAction";
    }
    
}
