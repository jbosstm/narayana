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
 * $Id: SOAPContextImple.java,v 1.8.4.1 2005/11/22 10:31:41 kconner Exp $
 */

package com.arjuna.mwlabs.wsas.context.soap;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mwlabs.wsas.util.XMLUtils;

/**
 * A default SOAPContext implementation.
 */

public class SOAPContextImple implements SOAPContext
{
    
    public SOAPContextImple ()
    {
	this("");
    }

    /**
     * @message com.arjuna.mwlabs.wsas.context.soap.SOAPContextImple_1 [com.arjuna.mwlabs.wsas.context.soap.SOAPContextImple_1] - SOAPContextImple ignoring: 
     */

    public SOAPContextImple (String id)
    {
    	try
    	{
    	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    	
    	    _doc = docBuilder.newDocument();
    	    
    	    _context = _doc.createElement(_contextName);
    
    	    if (addBasicContext(_context))
    	    {
        		ContextManager cxman = new ContextManager();
        		Context[] contexts = cxman.contexts();
        
        		org.w3c.dom.Element root = _doc.createElement(_hlsContext);
        	
        		if (contexts != null)
        		{
        		    for (int i = 0; i < contexts.length; i++)
        		    {
            			if (contexts[i] instanceof SOAPContext)
            			{
                            ((SOAPContext)contexts[i]).serialiseToElement(root) ;
            			}
            			else
            			{
            			    wsasLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsas.context.soap.SOAPContextImple_1",
            							  new Object[]{contexts[i]});
            			}
        		    }
        		}
        
        		_context.appendChild(root);
    	    }
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	    
    	    _context = null;
    	    _doc = null;
    	}
    }
    
    public void initialiseContext(Object param)
    {
    }
    
    /**
     * Serialise the SOAP context into a DOM node.
     * @param element The element to contain the serialisation.
     * @return the element added.
     */
    public Element serialiseToElement(final Element element)
    {
        element.appendChild(_context) ;
        return _context ;
    }

    public String identifier ()
    {
        return "SOAPContextImple";
    }

    public String toString ()
    {
    	return XMLUtils.writeToString(_context);
    }

    /**
     * @return <code>true</code> if a context was added, <code>false</code>
     * otherwise.
     */

    private final boolean addBasicContext (org.w3c.dom.Element root)
    {
    	ActivityHierarchy hier = null;
    	boolean added = false;
    	
    	try
    	{
    	    hier = UserActivityFactory.userActivity().currentActivity();
    	}
    	catch (SystemException ex)
    	{
    	    ex.printStackTrace();
    	}

    	if (hier != null)
    	{
    	    added = true;
    	    
    	    for (int i = 0; i < hier.size(); i++)
    	    {
        		org.w3c.dom.Element element = _doc.createElement(_contextElement);
        		org.w3c.dom.Element timeout = _doc.createElement("timeout");
        
        		timeout.appendChild(_doc.createTextNode(""+hier.activity(i).getTimeout()));
        		element.appendChild(timeout);
        
        		org.w3c.dom.Element type = _doc.createElement("type");
        		type.appendChild(_doc.createTextNode(_typeName));
        		element.appendChild(type);
        
        		org.w3c.dom.Element ctxId = _doc.createElement("ctxId");
        		ctxId.appendChild(_doc.createTextNode("urn:"+hier.activity(i).toString()));
        		element.appendChild(ctxId);
        
        		root.appendChild(element);
    	    }
    	}
    	
    	return added;
    }
    
    private org.w3c.dom.Element  _context;
    private org.w3c.dom.Document _doc;
    
    private static final String _typeName = "ArjunaTechnologies";
    private static final String _contextName = "context";
    private static final String _contextElement = "context-entry";
    private static final String _hlsContext = "extended-context-entry";

}
