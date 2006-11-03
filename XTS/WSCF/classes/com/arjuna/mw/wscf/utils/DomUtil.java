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
 * $Id: DomUtil.java,v 1.3 2005/05/19 12:13:28 nmcl Exp $
 */

package com.arjuna.mw.wscf.utils;

import javax.xml.parsers.*;

// TODO put back

//import org.apache.xml.serialize.DOMWriterImpl;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DomUtil.java,v 1.3 2005/05/19 12:13:28 nmcl Exp $
 * @since 1.0.
 */

public class DomUtil
{

    public static final org.w3c.dom.Text getTextNode (org.w3c.dom.Node element, String name)
    {
	org.w3c.dom.Node child = getNode(element, name);
	
	if (child != null)
	    return (org.w3c.dom.Text) child.getFirstChild();
	else
	    return null;
    }

    public static final org.w3c.dom.Node getNode (org.w3c.dom.Node element, String name)
    {
	org.w3c.dom.Node child = null;
	
	if (element != null)
	{
	    org.w3c.dom.NodeList children = element.getChildNodes();
	    
	    for (int i = 0; i < children.getLength(); i++)
	    {
		org.w3c.dom.Node item = children.item(i);
		
		if (item.getNodeName().equals(name))
		{
		    child = item;

		    break;
		}
		else
		{
		    if (item.hasChildNodes())
		    {
			child = getNode(item, name);
			
			if (child != null)
			    break;
		    }
		}
	    }
	}
	
	return child;
    }

    public static final String nodeAsString (org.w3c.dom.Node node)
    {
	// TODO put back

	//	DOMWriterImpl domWriter = new DOMWriterImpl();

	//	return domWriter.writeToString(node);

	return node.toString();
    }

    public static final DocumentBuilder getDocumentBuilder ()
    {
	try
	{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	    return factory.newDocumentBuilder();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
	
	return null;
    }

}
