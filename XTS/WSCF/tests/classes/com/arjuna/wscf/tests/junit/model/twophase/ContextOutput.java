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
 * $Id: ContextOutput.java,v 1.2.22.1 2005/11/22 10:34:07 kconner Exp $
 */

package com.arjuna.wscf.tests.junit.model.twophase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mw.wscf.model.twophase.UserCoordinatorFactory;
import com.arjuna.mw.wscf.model.twophase.api.UserCoordinator;
import com.arjuna.wscf.tests.WSCFTestUtils;
import junit.framework.TestCase;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ContextOutput.java,v 1.2.22.1 2005/11/22 10:34:07 kconner Exp $
 * @since 1.0.
 */

public class ContextOutput extends TestCase
{

    public void testContextOutput()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

	try
	{
	    ua.begin();

	    System.out.println("Started: "+ua.identifier()+"\n");

	    DeploymentContext manager = DeploymentContextFactory.deploymentContext();
	    Context theContext = manager.context();

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    org.w3c.dom.Document doc = builder.newDocument();
	    org.w3c.dom.Element root = doc.createElement("Context-test");

        // this fails because the documents are different -- need a better test than this
        ((SOAPContext)theContext).serialiseToElement(root) ;
	    doc.appendChild(root);

        // this does not do a full recursive conversion to text format -- need a better test than this
        System.out.println(com.arjuna.mw.wscf.utils.DomUtil.nodeAsString(doc));
	    
	    ua.cancel();
	}
	catch (Exception ex)
	{
        WSCFTestUtils.cleanup(ua);

        throw ex;
    }
    }
}
