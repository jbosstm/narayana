/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
    	
    	    Document doc = docBuilder.newDocument();
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
    
    private Element _context;

}
