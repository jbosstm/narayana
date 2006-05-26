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
 * $Id: WscTranslateContext.java,v 1.6.6.1 2005/11/22 10:34:17 kconner Exp $
 */

package com.arjuna.mwtests.wscf.model.jta;

import com.arjuna.mw.wscf.model.xa.*;

import com.arjuna.mw.wscf.common.*;

import com.arjuna.mw.wsas.activity.*;

import javax.transaction.*;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.soap.SOAPContext;

import com.arjuna.mw.wsas.exceptions.NoActivityException;

import com.arjuna.mw.wscf.exceptions.*;

import com.arjuna.mw.wscf.utils.DomUtil;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WscTranslateContext.java,v 1.6.6.1 2005/11/22 10:34:17 kconner Exp $
 * @since 1.0.
 */

public class WscTranslateContext
{

    public static void main (String[] args)
    {
	boolean passed = false;
	
	try
	{
	    UserTransaction ut = UserTransactionFactory.userTransaction();
	    
	    ut.begin();

	    System.out.println("Started: "+ut+"\n");

	    DeploymentContext manager = DeploymentContextFactory.deploymentContext();
	    SOAPContext theContext = (SOAPContext) manager.context();

	    System.out.println(theContext);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.newDocument();
        org.w3c.dom.Element ctx = doc.createElement("Context-test");
        final Element context = ((SOAPContext)theContext).serialiseToElement(ctx) ;
        
	    org.w3c.dom.Element wscCtx = translate(context);
	    
	    System.out.println("\nNow got "+DomUtil.nodeAsString(wscCtx));

	    ut.rollback();

	    passed = true;
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();

	    passed = false;
	}
	
	if (passed)
	    System.out.println("\nPassed.");
	else
	    System.out.println("\nFailed.");
    }

    static private org.w3c.dom.Element translate (org.w3c.dom.Element ctx)
    {
	try
	{
	    org.w3c.dom.Document doc = ctx.getOwnerDocument();
	    
	    org.w3c.dom.Element regServiceElement = doc.createElement("wscoor:RegistrationService");
	    org.w3c.dom.Element regAddressElement = doc.createElement("wsu:Address");

	    regAddressElement.appendChild(doc.createTextNode("htpp://www.arjuna.com?dummyRegistrationServiceAddress"));
	
	    regServiceElement.appendChild(regAddressElement);
	
	    ctx.appendChild(regServiceElement);

	    return ctx;
	}
	catch (Exception ex)
	{
	    // TODO deal with correctly!

	    ex.printStackTrace();
	}
	
	return null;
    }

    private static final String _wscoorNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor";
    private static final String _wsuNamespace = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    private static final String _arjunaNamespace = "http://arjuna.com/schemas/wsc/2003/01/extension";
	
}
