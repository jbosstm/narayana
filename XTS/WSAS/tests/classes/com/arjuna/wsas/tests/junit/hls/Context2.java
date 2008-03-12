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
 * $Id: Context2.java,v 1.3.24.1 2005/11/22 10:31:41 kconner Exp $
 */

package com.arjuna.wsas.tests.junit.hls;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.ActivityManagerFactory;
import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mwlabs.wsas.util.XMLUtils;
import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.FailureHLS;
import com.arjuna.wsas.tests.WSASTestUtils;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Context2.java,v 1.3.24.1 2005/11/22 10:31:41 kconner Exp $
 * @since 1.0.
 */

public class Context2 extends TestCase
{

    public static void testContext2()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
        FailureHLS failureHLS = new FailureHLS();
    try
	{
	    ActivityManagerFactory.activityManager().addHLS(demoHLS);
	    ActivityManagerFactory.activityManager().addHLS(failureHLS);

	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	    org.w3c.dom.Document factory = docBuilder.newDocument();
	    org.w3c.dom.Element root = factory.createElement("Context2-test");
	    
	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName());

	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName()+"\n");

        // this no longer works because DeploymentContextFactory has changed
	    DeploymentContext manager = DeploymentContextFactory.deploymentContext();
	    Context theContext = manager.context();

        ((SOAPContext)theContext).serialiseToElement(root);
	    
	    org.w3c.dom.Document doc = docBuilder.newDocument();
	    doc.appendChild(root);

	    System.out.println(XMLUtils.writeToString(doc));
	    
	    ua.end();

	    System.out.println("\nFinished child activity.\n");

	    theContext = manager.context();

	    root = factory.createElement("Context2-test");

        ((SOAPContext)theContext).serialiseToElement(root);
	    
	    doc = docBuilder.newDocument();
	    doc.appendChild(root);
	    
	    System.out.println(XMLUtils.writeToString(doc));

	    ua.end();

	    System.out.println("\nFinished parent activity.\n");

	    theContext = manager.context();

	    root = factory.createElement("Context2-test");

        ((SOAPContext)theContext).serialiseToElement(root);
	    
	    doc = docBuilder.newDocument();
	    doc.appendChild(root);
	    
	    System.out.println(XMLUtils.writeToString(doc));
	}
    catch (Exception ex)
    {
        WSASTestUtils.cleanup(ua);
        throw ex;
    } finally {
        try {
            if (demoHLS != null) {
                ActivityManagerFactory.activityManager().removeHLS(demoHLS);
            }
        } catch (Exception ex) {
            // ignore this
        }
        try {
            if (failureHLS != null) {
                ActivityManagerFactory.activityManager().removeHLS(failureHLS);
            }
        } catch (Exception ex) {
            // ignore this
        }
    }
    }
}
