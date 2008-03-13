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
 * $Id: DemoXAParticipant.java,v 1.4 2004/03/15 13:25:14 nmcl Exp $
 */

package com.arjuna.wscf.tests;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.mw.wsas.completionstatus.Failure;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wscf.model.as.coordinator.Participant;
import com.arjuna.mw.wscf.model.as.coordinator.Message;

import com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes.*;
import com.arjuna.mw.wscf.model.as.coordinator.xa.messages.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import javax.transaction.xa.*;

import java.util.Hashtable;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoXAParticipant.java,v 1.4 2004/03/15 13:25:14 nmcl Exp $
 * @since 1.0.
 */

public class DemoXAParticipant implements Participant
{

    public DemoXAParticipant ()
    {
	this(true);
    }

    public DemoXAParticipant (boolean readonly)
    {
	_readonly = readonly;
	_id = new Uid();
    }
    
    public Outcome processMessage (Message notification) throws WrongStateException, ProtocolViolationException, SystemException
    {
	System.out.println("DemoXAParticipant.processMessage ( "+notification+" ) : "+_id);
	
	if (notification instanceof XAPrepare)
	{
	    if (_readonly)
	    {
		System.out.println("\nReturning read-only to prepare");
	    
		return new XAPrepareOutcome(XAResource.XA_RDONLY);
	    }
	    else
	    {
		System.out.println("\nReturning commit to prepare");
	    
		return new XAPrepareOutcome(XAResource.XA_OK);
	    }
	}
	else
	{
	    if (notification instanceof XACommit)
	    {
		if (((XACommit) notification).onePhaseCommit())
		{
		    System.out.println("\nOne-phase commit ok");
			
		    return new XAOutcome();
		}
		else
		{
		    System.out.println("\nCommit ok");
			
		    return new XAOutcome();
		}
	    }
	    else
	    {
		if (notification instanceof XAStart)
		{
		    System.out.println("\nStarting ok");
		    
		    return new XAOutcome();
		}
		else
		{
		    if (notification instanceof XAEnd)
		    {
			System.out.println("\nEnding ok");
		    
			return new XAOutcome();
		    }
		    else
		    {
			if (notification instanceof XARollback)
			{
			    System.out.println("\nRolling back ok");
			
			    return new XAOutcome(Failure.instance());
			}
			else
			{
			    System.out.println("\nReturning finish-error for "+notification);
		
			    return new XAOutcome(new XAException(XAException.XAER_INVAL));
			}
		    }
		}
	    }
	}
    }
    
    public String identity () throws SystemException
    {
	return "DemoXAParticipant: "+_id;
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

}
