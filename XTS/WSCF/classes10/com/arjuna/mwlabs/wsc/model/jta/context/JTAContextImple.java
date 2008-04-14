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
 * $Id: JTAContextImple.java,v 1.10.4.1 2005/11/22 10:34:08 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta.context;

import com.arjuna.mw.wsas.*;
import com.arjuna.mw.wsas.activity.*;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;

import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.utils.DomUtil;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;

import javax.transaction.*;

import javax.xml.parsers.*;

import org.w3c.dom.Element;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * On demand this class creates the SOAP context information necessary to
 * propagate the hierarchy of coordinators associated with the current
 * thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: JTAContextImple.java,v 1.10.4.1 2005/11/22 10:34:08 kconner Exp $
 */

public class JTAContextImple implements SOAPContext
{

    public JTAContextImple ()
    {
        _context = null;
    }
    
    public JTAContextImple (Transaction currentCoordinator)
    {
    	_context = null;
    	
    	initialiseContext(currentCoordinator);
    }
    
    /**
     * Serialise the SOAP context into a DOM node.
     * @param element The element to contain the serialisation.
     * @return the element added.
     */
    public Element serialiseToElement(final Element element)
    {
        final Element context = context() ;
        element.appendChild(context) ;
        return context ;
    }

    public void initialiseContext(Object param)
    {
    	try
    	{
    	    final Transaction currentCoordinator = (Transaction) param;
            ActivityHierarchy hier = null;
            
            try
            {
                hier = UserActivityFactory.userActivity().currentActivity();
            }
            catch (SystemException ex)
            {
                ex.printStackTrace();
            }

            if ((currentCoordinator != null) && (hier != null))
            {
                _identifierValue = ((TransactionImple) currentCoordinator).get_uid().stringForm() ;
                _expiresValue = hier.activity(hier.size()-1).getTimeout() ;
            }
        }
        catch (final ClassCastException cce)
        {
            throw new java.lang.IllegalArgumentException();
        }
    }
    
    private synchronized Element context()
    {
        if (_context == null)
        {
    	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder builder = null;
    	
    	    try
    	    {
    	        builder = factory.newDocumentBuilder();
    	    }
    	    catch (Exception ex)
    	    {
    	        ex.printStackTrace();
    	    }
    
    	    org.w3c.dom.Document doc = builder.newDocument();
        
    	    _context = doc.createElement(_contextName);
        
            _context.setAttribute("xmlns:wsu", _wsuNamespace);
            _context.setAttribute("xmlns:wscoor", _wscoorNamespace);
            _context.setAttribute("xmlns:arjuna", _arjunaNamespace);
    
    	    if (_identifierValue != null)
    	    {
        		org.w3c.dom.Element identifier = doc.createElement(_identifier);
        		identifier.appendChild(doc.createTextNode(_identifierValue));
        		
        		_context.appendChild(identifier);
        	    
        		org.w3c.dom.Element expires = doc.createElement(_expires);
        		expires.appendChild(doc.createTextNode(Integer.toString(_expiresValue)));
        
        		_context.appendChild(expires);
        
        		org.w3c.dom.Element coordinationType = doc.createElement("wscoor:CoordinationType");
        		coordinationType.appendChild(doc.createTextNode(_coordinationTypeURI));
        	    
        		_context.appendChild(coordinationType);
    	    }
        }
        return _context ;
    }
    
    public String getTransactionIdentifier()
    {
        return _identifierValue ;
    }
    
    public int getTransactionExpires()
    {
        return _expiresValue ;
    }
    
    public String identifier ()
    {
        return JTAContextImple.class.getName();
    }

    public String toString ()
    {
        return DomUtil.nodeAsString(context());
    }

    /**
     * @message com.arjuna.mwlabs.wsc.model.jta.context.JTAContextImple_1 [com.arjuna.mwlabs.wsc.model.jta.context.JTAContextImple_1] - ignoring context: 
     */
    public static JTAContextImple getContext()
    {
        ContextManager cxman = new ContextManager();
        Context[] contexts = cxman.contexts();
    
        for (int i = 0; i < contexts.length; i++)
        {
            if (contexts[i].identifier().equals(com.arjuna.mwlabs.wsc.model.jta.context.JTAContextImple.class.getName()))
            {
                if (contexts[i] instanceof JTAContextImple)
                {
                    return (JTAContextImple)contexts[i] ;
                }
                else
                {
                    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsc.model.jta.context.SOAPContextImple_1",
                                  new Object[]{contexts[i]});
                }
                break;
            }
        }
        return null ;
    }

    private org.w3c.dom.Element _context;
    private String _identifierValue ;
    private int _expiresValue ;

    private static final String _wscoorNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor";
    private static final String _wsuNamespace = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    private static final String _arjunaNamespace = "http://arjuna.com/schemas/wsc/2003/01/extension";

    private static final String _contextName = "wscoor:CoordinationContext";
    private static final String _identifier = "wsu:Identifier";
    private static final String _expires = "wsu:Expires";
    private static final String _coordinationType = "CoordinationType";

    public static final String _coordinationTypeURI = "urn:arjuna:jta";

}
