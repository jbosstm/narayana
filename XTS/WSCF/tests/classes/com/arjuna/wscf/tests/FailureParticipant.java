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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FailureParticipant.java,v 1.4 2004/03/15 13:25:14 nmcl Exp $
 */

package com.arjuna.wscf.tests;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wscf.model.as.coordinator.Participant;
import com.arjuna.mw.wscf.model.as.coordinator.Message;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.outcomes.*;
import com.arjuna.mw.wscf.model.as.coordinator.twophase.messages.*;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import java.util.Hashtable;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: FailureParticipant.java,v 1.4 2004/03/15 13:25:14 nmcl Exp $
 * @since 1.0.
 */

public class FailureParticipant implements Participant
{

    public static final int NESTED_PREPARE = 0;
    public static final int NESTED_CONFIRM = 1;
    public static final int NESTED_CANCEL = 2;
    public static final int TOPLEVEL_PREPARE = 3;
    public static final int TOPLEVEL_CONFIRM = 4;
    public static final int TOPLEVEL_CANCEL = 5;
    
    public FailureParticipant (int failure)
    {
	this(true, failure);
    }

    public FailureParticipant (boolean readonly, int failure)
    {
	_readonly = readonly;
	_id = new Uid();
	_failurePoint = failure;
    }
    
    public Outcome processMessage (Message notification) throws WrongStateException, ProtocolViolationException, SystemException
    {
	System.out.println("FailureParticipant.processMessage ( "+notification+" ) : "+_id);
	
	if ((notification instanceof NestedPrepare) || (notification instanceof TopLevelPrepare))
	{
	    if ((_failurePoint == NESTED_PREPARE) && (notification instanceof NestedPrepare))
	    {
		System.out.println("\nReturning cancel");

		return new VoteCancel();
	    }

	    if ((_failurePoint == TOPLEVEL_PREPARE) && (notification instanceof TopLevelPrepare))
	    {
		System.out.println("\nReturning cancel");

		return new VoteCancel();
	    }
	    
	    if (_readonly)
	    {
		System.out.println("\nReturning read-only");
	    
		return new VoteReadOnly();
	    }
	    else
	    {
		System.out.println("\nReturning commit");
	    
		return new VoteConfirm();
	    }
	}
	else
	{
	    if ((notification instanceof NestedConfirm) || (notification instanceof TopLevelConfirm))
	    {
		if ((_failurePoint == NESTED_CONFIRM) && (notification instanceof NestedConfirm))
		{
		    System.out.println("\nReturning finish-error");

		    return new CoordinationOutcome(TwoPhaseOutcome.FINISH_ERROR);
		}
		
		if ((_failurePoint == TOPLEVEL_CONFIRM) && (notification instanceof TopLevelConfirm))
		{
		    System.out.println("\nReturning finish-error");

		    return new CoordinationOutcome(TwoPhaseOutcome.FINISH_ERROR);
		}

		System.out.println("\nReturning finish-ok");

		return new CoordinationOutcome(TwoPhaseOutcome.FINISH_OK);
	    }
	    else
	    {
		if ((notification instanceof NestedCancel) || (notification instanceof TopLevelCancel))
		{
		    if ((_failurePoint == NESTED_CANCEL) && (notification instanceof NestedCancel))
		    {
			System.out.println("\nReturning finish-error");

			return new CoordinationOutcome(TwoPhaseOutcome.FINISH_ERROR);
		    }

		    if ((_failurePoint == TOPLEVEL_CANCEL) && (notification instanceof TopLevelCancel))
		    {
			System.out.println("\nReturning finish-error");

			return new CoordinationOutcome(TwoPhaseOutcome.FINISH_ERROR);
		    }

		    System.out.println("\nReturning finish-ok");

		    return new CoordinationOutcome(TwoPhaseOutcome.FINISH_OK);
		}
		else
		{
		    System.out.println("\nReturning finish-error");
		
		    return new CoordinationOutcome(TwoPhaseOutcome.FINISH_ERROR);
		}
	    }
	}
    }
    
    public String identity () throws SystemException
    {
	return "FailureParticipant: "+_id;
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
	return true;
    }

    private boolean _readonly;
    private Uid     _id;
    private int     _failurePoint;
    
}
