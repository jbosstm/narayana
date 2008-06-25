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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Configuration.java,v 1.6 2005/05/19 12:13:17 nmcl Exp $
 */

package com.arjuna.mw.wsas.utils;

import com.arjuna.mw.wsas.logging.wsasLogger;

import java.net.URL;

import java.io.FileNotFoundException;

import javax.xml.parsers.*;

/**
 */

public class Configuration
{

    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";

    /**
     * @message com.arjuna.mw.wsas.utils.Configuration_1 [com.arjuna.mw.wsas.utils.Configuration_1] -  not found.
     */

    public static final void initialise (String fileName) throws Exception
    {
	/*
	 * Locate the configuration file.
	 */

	URL url = Configuration.class.getResource(fileName);

	if (url == null)
	{
	    throw new FileNotFoundException(fileName);
	    
	    //	    throw new FileNotFoundException(fileName+wsasLogger.log_mesg.getString("com.arjuna.mw.wsas.utils.Configuration_1"));
	}
	else
	    initialise(url);
    }

    /**
     * @message com.arjuna.mw.wsas.utils.Configuration_2 [com.arjuna.mw.wsas.utils.Configuration_2] -  Failed to create doc 
     */

    public static final void initialise (URL url) throws Exception
    {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();
	org.w3c.dom.Document doc = builder.parse(url.toExternalForm());
	    
	if (doc == null)
	{
	    throw new FileNotFoundException(wsasLogger.log_mesg.getString("com.arjuna.mw.wsas.utils.Configuration_2")+" "+url);
	}
	
	org.w3c.dom.NodeList children = getProperties(doc.getDocumentElement());

	if (children != null)
	{
	    for (int i = 0; i < children.getLength(); i++)
	    {
		org.w3c.dom.Node item = children.item(i);
	    
		if (item.getNodeName().equals(PROPERTY))
		{
		    org.w3c.dom.Element type = (org.w3c.dom.Element) item;
		    
		    String name = type.getAttribute("name");
		    String value = type.getAttribute("value");
		
		    if ((name != null) && (value != null))
			System.setProperty(name, value);
		}
	    }
	}
    }

    private static final org.w3c.dom.NodeList getProperties (org.w3c.dom.Node elem)
    {
	org.w3c.dom.NodeList children = elem.getChildNodes();

	for (int i = 0; i < children.getLength(); i++)
	{
	    org.w3c.dom.Node item = children.item(i);

	    if (item.getNodeName().equals(PROPERTIES))
	    {
		return item.getChildNodes();
	    }
	    else
	    {
		org.w3c.dom.NodeList descendants = getProperties(item);
		
		if (descendants != null)
		    return descendants;
	    }
	}

	return null;
    }

}
