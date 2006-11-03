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
 * $Id: TopLevel3.java,v 1.3.22.1 2005/11/22 10:34:12 kconner Exp $
 */

package com.arjuna.mwtests.wscf.model.as.jta;

import javax.transaction.xa.XAException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.completionstatus.Success;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wscf.UserCoordinator;
import com.arjuna.mw.wscf.UserCoordinatorFactory;
import com.arjuna.mw.wscf.model.as.CoordinatorManager;
import com.arjuna.mw.wscf.model.as.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes.XAOutcome;
import com.arjuna.mw.wscf.utils.DomUtil;
import com.arjuna.mwlabs.wscf.utils.ProtocolLocator;
import com.arjuna.mwtests.wscf.common.DemoXAParticipant;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TopLevel3.java,v 1.3.22.1 2005/11/22 10:34:12 kconner Exp $
 * @since 1.0.
 */

public class TopLevel3
{

    public static void main (String[] args)
    {
	boolean passed = false;
	String className = "com.arjuna.mwlabs.wscf.generic.coordinator.jta.JTAHLS";
	org.w3c.dom.Document implementationDoc = null;
	
	System.setProperty("com.arjuna.mw.wscf.protocolImplementation", className);
	
	try
	{
	    ProtocolLocator pl = new ProtocolLocator(className);

	    implementationDoc = pl.getProtocol();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    
	    System.exit(0);
	}
	
	try
	{
	    UserCoordinator ua = UserCoordinatorFactory.userCoordinator(implementationDoc);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    org.w3c.dom.Document doc = builder.newDocument();
	    org.w3c.dom.Element root = doc.createElement("TopLevel3-test");
	
	    ua.start();

	    System.out.println("Started: "+ua.activityName()+"\n");

	    DeploymentContext manager = DeploymentContextFactory.deploymentContext();
	    Context theContext = manager.context();

        ((SOAPContext)theContext).serialiseToElement(root) ;
	    doc.appendChild(root);

	    System.out.println(DomUtil.nodeAsString(doc));

	    CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager(implementationDoc);
	    
	    //	    cm.addParticipant(new DemoXAParticipant(), Priorities.PARTICIPANT, null);
	    cm.addParticipant(new DemoXAParticipant(false), 0, null);
	    cm.addParticipant(new DemoXAParticipant(false), 0, null);

	    System.out.println("\nEnding coordination.");
	    
	    ua.setCompletionStatus(Success.instance());
	    
	    Outcome res = ua.end();

	    if (res instanceof XAOutcome)
	    {
		XAOutcome out = (XAOutcome) res;
		
		if (out.data() == null)
		    passed = true;
		else
		    System.out.println("Result is: "+((XAException) out.data()));
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
