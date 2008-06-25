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
 * $Id: Nested8.java,v 1.1 2003/01/07 10:33:59 nmcl Exp $
 */

package com.arjuna.wscf.tests.junit.model.as.twophase;

import com.arjuna.wscf.tests.DemoParticipant;
import com.arjuna.wscf.tests.FailureParticipant;

import com.arjuna.mw.wscf.UserCoordinator;
import com.arjuna.mw.wscf.UserCoordinatorFactory;

import com.arjuna.mw.wscf.model.as.CoordinatorManager;
import com.arjuna.mw.wscf.model.as.CoordinatorManagerFactory;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.common.*;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.mw.wsas.completionstatus.Success;

import com.arjuna.mw.wsas.context.ContextManager;

import com.arjuna.mw.wsas.exceptions.NoActivityException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Nested8.java,v 1.1 2003/01/07 10:33:59 nmcl Exp $
 * @since 1.0.
 */

public class Nested8
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserCoordinator ua = UserCoordinatorFactory.userCoordinator();
	
	    ua.start();

	    System.out.println("Started: "+ua.activityName());

	    CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();
	    
	    cm.addParticipant(new DemoParticipant(true), Priorities.PARTICIPANT, null);

	    ua.start();

	    System.out.println("Started: "+ua.activityName());
	    
	    ua.setCompletionStatus(Success.instance());
	    
	    ContextManager manager = new ContextManager();
	    com.arjuna.mw.wsas.context.Context[] contexts = manager.contexts();

	    if (contexts != null)
	    {
		for (int i = 0; i < contexts.length; i++)
		    System.out.println(contexts[i]);
	    }

	    cm.addParticipant(new FailureParticipant(false, FailureParticipant.TOPLEVEL_CANCEL), Priorities.PARTICIPANT, null);

	    Outcome res = ua.end();

	    if (res instanceof CoordinationOutcome)
	    {
		CoordinationOutcome out = (CoordinationOutcome) res;
		
		if (out.result() == TwoPhaseResult.CONFIRMED)
		    passed = true;
		else
		    System.out.println("Result is: "+TwoPhaseResult.stringForm(out.result()));
	    }
	    else
		System.out.println("Outcome is: "+res);

	    res = ua.end();

	    if (res instanceof CoordinationOutcome)
	    {
		CoordinationOutcome out = (CoordinationOutcome) res;
		
		if (out.result() == TwoPhaseResult.CANCELLED)
		    passed = passed && true;
		else
		    System.out.println("Result is: "+TwoPhaseResult.stringForm(out.result()));
	    }
	    else
		System.out.println("Outcome is: "+res);	    
	}
	catch (NoActivityException ex)
	{
	    passed = true;
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
	
	if (passed)
	    System.out.println("\nPassed.");
	else
	    System.out.println("\nFailed.");
    }

}
