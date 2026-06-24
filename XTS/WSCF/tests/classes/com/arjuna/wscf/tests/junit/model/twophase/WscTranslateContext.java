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
 * $Id: WscTranslateContext.java,v 1.6.6.1 2005/11/22 10:34:08 kconner Exp $
 */

package com.arjuna.wscf.tests.junit.model.twophase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import org.w3c.dom.Element;

import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mw.wscf.model.twophase.UserCoordinatorFactory;
import com.arjuna.mw.wscf.model.twophase.api.UserCoordinator;
import com.arjuna.mw.wscf.utils.DomUtil;
import com.arjuna.wscf.tests.WSCFTestUtils;
import org.junit.Test;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WscTranslateContext.java,v 1.6.6.1 2005/11/22 10:34:08 kconner Exp $
 * @since 1.0.
 */

public class WscTranslateContext
{

    @Test
    public void testWscTranslateContext()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin("TwoPhaseHLS");

	    System.out.println("Started: "+ua.identifier()+"\n");

        ContextManager cxman = new ContextManager();
        Context context = cxman.context("TwoPhaseHLS");

        SOAPContext theContext = (SOAPContext)context;
        // this fails because the context toString method gets a NPE -- need a better test
        System.out.println(theContext);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.newDocument();
        org.w3c.dom.Element ctx = doc.createElement("Context-test");
        final Element contextElement = ((SOAPContext)theContext).serialiseToElement(ctx) ;
        
        org.w3c.dom.Element wscCtx = translate(contextElement);
	    
        System.out.println("\nNow got "+DomUtil.nodeAsString(wscCtx));

	    ua.cancel();
	}
	catch (Exception ex)
	{
	    WSCFTestUtils.cleanup(ua);
        throw ex;
    }
    }

    static private org.w3c.dom.Element translate (org.w3c.dom.Element ctx)
            throws Exception
    {
	    org.w3c.dom.Document doc = ctx.getOwnerDocument();
	    
	    org.w3c.dom.Element regServiceElement = doc.createElement("wscoor:RegistrationService");
	    org.w3c.dom.Element regAddressElement = doc.createElement("wsu:Address");

	    regAddressElement.appendChild(doc.createTextNode("http://www.arjuna.com?dummyRegistrationServiceAddress"));	    
	
	    regServiceElement.appendChild(regAddressElement);
	
	    ctx.appendChild(regServiceElement);
	    
	    return ctx;
    }
}
