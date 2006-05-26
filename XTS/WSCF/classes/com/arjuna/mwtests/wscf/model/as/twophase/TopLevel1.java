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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TopLevel1.java,v 1.2.22.1 2005/11/22 10:34:15 kconner Exp $
 */

package com.arjuna.mwtests.wscf.model.as.twophase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wscf.UserCoordinator;
import com.arjuna.mw.wscf.UserCoordinatorFactory;
import com.arjuna.mw.wscf.model.as.CoordinatorManager;
import com.arjuna.mw.wscf.model.as.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.as.coordinator.twophase.common.Priorities;
import com.arjuna.mw.wscf.model.twophase.common.TwoPhaseResult;
import com.arjuna.mw.wscf.model.twophase.outcomes.CoordinationOutcome;
import com.arjuna.mwtests.wscf.common.DemoParticipant;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TopLevel1.java,v 1.2.22.1 2005/11/22 10:34:15 kconner Exp $
 * @since 1.0.
 */

public class TopLevel1
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserCoordinator ua = UserCoordinatorFactory.userCoordinator();
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    org.w3c.dom.Document doc = builder.newDocument();
	    org.w3c.dom.Element root = doc.createElement("TopLevel1-test");
	
	    ua.start();

	    System.out.println("Started: "+ua.activityName()+"\n");

	    DeploymentContext manager = DeploymentContextFactory.deploymentContext();
	    Context theContext = manager.context();

        ((SOAPContext)theContext).serialiseToElement(root) ;
	    doc.appendChild(root);

	    System.out.println(com.arjuna.mw.wscf.utils.DomUtil.nodeAsString(doc));

	    CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();
	    
	    cm.addParticipant(new DemoParticipant(), Priorities.PARTICIPANT, null);

	    System.out.println("\nEnding coordination.");
	    
	    Outcome res = ua.end();

	    if (res instanceof CoordinationOutcome)
	    {
		CoordinationOutcome out = (CoordinationOutcome) res;
		
		if (out.result() == TwoPhaseResult.CANCELLED)
		    passed = true;
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
