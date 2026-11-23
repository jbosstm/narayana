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
 * $Id: DemoSOAPContextImple.java,v 1.5.4.1 2005/11/22 10:31:42 kconner Exp $
 */

package com.arjuna.wsas.tests;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.arjuna.mw.wsas.context.soap.SOAPContext;

/**
 */

public class DemoSOAPContextImple implements SOAPContext
{

    public DemoSOAPContextImple (String id)
    {
    	try
    	{
    	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    	
    	    org.w3c.dom.Document doc = docBuilder.newDocument();
            _context = doc.createElement(id);

            _context.appendChild(doc.createTextNode("urn:mycomputer.org:"+id+":foo:bar"));
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	    
    	    _context = null;
    	}
    }

    public void initialiseContext (Object param)
    {
    }
    
    /**
     * Serialise the SOAP context into a DOM node.
     * @param element The element to contain the serialisation.
     * @return the element added.
     */
    public Element serialiseToElement(final Element element)
    {
        Document document = element.getOwnerDocument();
        // copy the context structure into the document
        Element context = document.createElement(_context.getTagName());
        context.appendChild(document.createTextNode(_context.getTextContent()));
        element.appendChild(context);
        return context;
    }
    
    public String identifier ()
    {
	return "DemoSOAPContextImple";
    }

    public String toString ()
    {
        return _context.toString() ;
    }
    
    private org.w3c.dom.Element _context;

}
