/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SynchronizationAction.java,v 1.3 2004/03/15 13:25:23 nmcl Exp $
 */

package com.arjuna.mwlabs.wstx.model.as.twophase.resource;

import com.arjuna.ats.arjuna.state.*;

import com.arjuna.mw.wstx.resource.Synchronization;

import com.arjuna.mw.wstx.common.*;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wscf.model.as.coordinator.Message;
import com.arjuna.mw.wscf.model.as.coordinator.twophase.messages.*;
import com.arjuna.mw.wscf.model.as.coordinator.twophase.outcomes.*;

import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wstx.exceptions.*;

import java.util.Hashtable;

/**
 * This class represents the synchronization-phase aware participants that are
 * enrolled with the transaction. It wraps the actual participants that have
 * pre- and post- two-phase operations on them and translated to/from the
 * low-level WSCF protocol.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SynchronizationAction.java,v 1.3 2004/03/15 13:25:23 nmcl Exp $
 * @since 1.0.
 */

public class SynchronizationAction implements com.arjuna.mw.wscf.model.as.coordinator.Participant
{

    public SynchronizationAction (Synchronization res)
    {
	_resource = res;
    }
    
    public Outcome processMessage (Message notification) throws WrongStateException, ProtocolViolationException, SystemException
    {
	if (_resource == null)
	    return new CoordinationOutcome(TwoPhaseResult.FINISH_ERROR);

	Outcome result = new CoordinationOutcome(TwoPhaseResult.FINISH_ERROR);

	if (notification instanceof BeforeCompletion)
	{
	    try
	    {
		_resource.beforeCompletion();

		return null;
	    }
	    catch (SystemException ex)
	    {
		throw ex;
	    }
	}

	if (notification instanceof AfterCompletion)
	{
	    try
	    {
		_resource.afterCompletion(((AfterCompletion) notification).status());

		return null;
	    }
	    catch (SystemException ex)
	    {
		throw ex;
	    }
	}

	return result;
    }

    /**
     * These methods are required so that the coordinator can serialise and
     * de-serialise information about the inferior during completion and
     * recovery.
     */

    public boolean packState (OutputObjectState os)
    {
	return true;
    }

    public boolean unpackState (InputObjectState os)
    {
	return false;
    }

    public String identity () throws SystemException
    {
	return "SynchronizationAction";
    }
    
    private Synchronization _resource;
    
}
