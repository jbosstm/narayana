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
 * $Id: TopLevel1.java,v 1.3.22.1 2005/11/22 10:34:12 kconner Exp $
 */

package com.arjuna.wscf.tests.junit.model.as.jta;

import javax.transaction.xa.XAException;
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
import com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes.XAOutcome;
import com.arjuna.mw.wscf.utils.DomUtil;
import com.arjuna.mwlabs.wscf.utils.ProtocolLocator;
import com.arjuna.wscf.tests.DemoXAParticipant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TopLevel1.java,v 1.3.22.1 2005/11/22 10:34:12 kconner Exp $
 * @since 1.0.
 */

public class TopLevel1
{
    private static final String PROTOCOL_IMPLEMENTATION_PROPERTY = "com.arjuna.mw.wscf.protocolImplementation";
    private static final String testProtocolClassName = "com.arjuna.mwlabs.wscf.generic.coordinator.jta.JTAHLS";
    private String origProtocolClassName;

    public void setup()
    {
        origProtocolClassName = System.getProperty(PROTOCOL_IMPLEMENTATION_PROPERTY);

        System.setProperty(PROTOCOL_IMPLEMENTATION_PROPERTY, testProtocolClassName);
    }

    public void tearDown()
    {
        System.setProperty(PROTOCOL_IMPLEMENTATION_PROPERTY, origProtocolClassName);
    }

    public static void testTopLevel1()
            throws Exception
    {

	    org.w3c.dom.Document implementationDoc = null;
	
        Class clazz = TopLevel1.class.getClassLoader().loadClass(testProtocolClassName);
        ProtocolLocator pl = new ProtocolLocator(clazz);

        implementationDoc = pl.getProtocol();

	try
	{
	    UserCoordinator ua = UserCoordinatorFactory.userCoordinator(implementationDoc);
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

	    System.out.println(DomUtil.nodeAsString(doc));

	    CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager(implementationDoc);
	    
	    //	    cm.addParticipant(new DemoXAParticipant(), Priorities.PARTICIPANT, null);
	    cm.addParticipant(new DemoXAParticipant(), 0, null);

	    System.out.println("\nEnding coordination.");
	    
	    Outcome res = ua.end();

	    if (res instanceof XAOutcome)
	    {
            XAOutcome out = (XAOutcome) res;
		
            if (out.data() == null) {
                System.out.println("Result is "+((XAException) out.data()));
            } else {
                fail("XAOutcome has non-null data " + out.data());
            }
        } else {
            fail("Non-XAOutcome result " + res);
        }
    }
	catch (NoActivityException ex)
	{
	    // it's ok if we arrive here?
	}
    }
}
