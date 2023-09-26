/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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