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
 * $Id: Context1.java,v 1.2.24.1 2005/11/22 10:31:41 kconner Exp $
 */

package com.arjuna.wsas.tests.junit.hls;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.ActivityManagerFactory;
import com.arjuna.mw.wsas.UserActivity;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mwlabs.wsas.util.XMLUtils;
import com.arjuna.wsas.tests.DemoHLS;
import com.arjuna.wsas.tests.WSASTestUtils;
import com.arjuna.wsas.tests.DemoSOAPContextImple;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Context1.java,v 1.2.24.1 2005/11/22 10:31:41 kconner Exp $
 * @since 1.0.
 */

public class Context1 extends TestCase
{

    public static void testContext1()
            throws Exception
    {
        UserActivity ua = UserActivityFactory.userActivity();
        DemoHLS demoHLS = new DemoHLS();
        HLS[] currentHLS = ActivityManagerFactory.activityManager().allHighLevelServices();

        for (HLS hls : currentHLS) {
            ActivityManagerFactory.activityManager().removeHLS(hls);
        }
    try
	{
	    ActivityManagerFactory.activityManager().addHLS(demoHLS);
	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	    org.w3c.dom.Document doc = docBuilder.newDocument();
	    org.w3c.dom.Element root = doc.createElement("Context1-test");
        doc.appendChild(root);
	    
	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName());

	    ua.start();
	    
	    System.out.println("Started: "+ua.activityName()+"\n");

        ContextManager contextManager = new ContextManager();
        Context[] contexts = contextManager.contexts();
        Context theContext = null;

        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] != null) {
                theContext = contexts[i];
                break;
            }
        }

        if (theContext == null) {
            fail("Demo context not found");
        }

        if (!(theContext instanceof DemoSOAPContextImple)) {
            fail("Demo context not found");
        }

        ((SOAPContext)theContext).serialiseToElement(root);
	    
        System.out.println("Context is " + root.getTextContent());

	    ua.end();

	    System.out.println("\nFinished child activity.\n");

        contexts = contextManager.contexts();
        theContext = null;

        for (int i = 0; i < contexts.length; i++) {
            if (contexts[i] != null) {
                theContext = contexts[i];
                break;
            }
        }

        if (theContext == null) {
            fail("Demo context not found");
        }

        if (!(theContext instanceof DemoSOAPContextImple)) {
            fail("Demo context not found");
        }

        doc = docBuilder.newDocument();
	    root = doc.createElement("Context1-test");
        doc.appendChild(root);


        ((SOAPContext)theContext).serialiseToElement(root);
	    
        System.out.println("Context is " + root.getTextContent());

	    ua.end();

	    System.out.println("\nFinished parent activity.\n");

	}
	catch (Exception ex)
	{
        WSASTestUtils.cleanup(ua);
        throw ex;
    } finally {
        try {
            for (HLS hls : currentHLS) {
                ActivityManagerFactory.activityManager().addHLS(hls);
            }
        } catch (Exception ex) {
            // ignore this
        }
        try {
            if (demoHLS != null) {
                ActivityManagerFactory.activityManager().removeHLS(demoHLS);
            }
        } catch (Exception ex) {
            // ignore this
        }
    }
    }
}
