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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArjunaContextImple.java,v 1.10.4.1 2005/11/22 10:34:12 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.twophase.context;

import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.utils.*;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator;

import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;

import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import javax.xml.parsers.*;

import org.w3c.dom.Element;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * On demand this class creates the SOAP context information necessary to
 * propagate the hierarchy of coordinators associated with the current
 * thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ArjunaContextImple.java,v 1.10.4.1 2005/11/22 10:34:12 kconner Exp $
 */

public class ArjunaContextImple implements SOAPContext
{

    public ArjunaContextImple ()
    {
        _context = null;
    }
    
    public ArjunaContextImple (ACCoordinator currentCoordinator)
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
            ACCoordinator currentCoordinator = (ACCoordinator) param;

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
                /*
                 * Do the manditory stuff first.
                 */

                ActionHierarchy txHier = currentCoordinator.getHierarchy();
                final int depth = txHier.depth() ;
                _identifierValues = new String[depth] ;
                _expiresValues = new int[depth] ;
                
                _identifierValues[0] = txHier.getDeepestActionUid().stringForm() ;
                _expiresValues[0] = hier.activity(hier.size()-1).getTimeout() ;
                
                /*
                 * Now let's do the optional stuff.
                 */

                for(int count = 1, index = 0 ; count < depth ; count++, index++)
                {
                    _identifierValues[count] = txHier.getActionUid(index).stringForm() ;
                    _expiresValues[count] = hier.activity(index).getTimeout() ;
                }
            }
        }
        catch (ClassCastException ex)
        {
            throw new java.lang.IllegalArgumentException();
        }
    }
    
    private synchronized Element context()
    {
        if (_context == null)
        {
            try
            {
                DocumentBuilder builder = com.arjuna.mw.wscf.utils.DomUtil.getDocumentBuilder();
                org.w3c.dom.Document doc = builder.newDocument();

                _context = doc.createElement("wscoor:"+_contextName);
            
                _context.setAttribute("xmlns:wsu", _wsuNamespace);
                _context.setAttribute("xmlns:wscoor", _wscoorNamespace);
                _context.setAttribute("xmlns:arjuna", _arjunaNamespace);

                if (_identifierValues != null)
                {
                    /*
                     * Do the manditory stuff first.
                     */
    
                    org.w3c.dom.Element identifier = doc.createElement("wsu:"+_identifier);
                    identifier.appendChild(doc.createTextNode(_identifierValues[0]));
                    
                    _context.appendChild(identifier);
    
                    org.w3c.dom.Element expires = doc.createElement("wsu:"+_expires);
                    expires.appendChild(doc.createTextNode(Integer.toString(_expiresValues[0])));
    
                    _context.appendChild(expires);
    
                    org.w3c.dom.Element coordinationType = doc.createElement("wscoor:CoordinationType");
                    coordinationType.appendChild(doc.createTextNode(_coordinationTypeURI));
    
                    _context.appendChild(coordinationType);
                    
                    /*
                     * Now let's do the optional stuff.
                     */
                    final int depth = _identifierValues.length ;
                    if (depth > 1)
                    {
                        org.w3c.dom.Element extensionRoot = doc.createElement("arjuna:"+_contextName);
        
                        for(int count = 1 ; count < depth ; count++)
                        {
                            identifier = doc.createElement("arjuna:"+_identifier);
                            identifier.appendChild(doc.createTextNode(_identifierValues[count]));
            
                            extensionRoot.appendChild(identifier);
                            
                            expires = doc.createElement("arjuna:"+_expires);
                            expires.appendChild(doc.createTextNode(Integer.toString(_expiresValues[count])));
            
                            extensionRoot.appendChild(expires);
                        }
    
                        _context.appendChild(extensionRoot);
                    }
                }
            }
            catch (ClassCastException ex)
            {
                throw new java.lang.IllegalArgumentException();
            }
        }
        return _context ;
    }
    
    public String getTransactionIdentifier(final int index)
    {
        return _identifierValues[index] ;
    }
    
    public int getTransactionExpires(final int index)
    {
        return _expiresValues[index] ;
    }
    
    public int getDepth()
    {
        return (_identifierValues == null ? 0 : _identifierValues.length) ;
    }

    public String identifier ()
    {
        return ArjunaContextImple.class.getName();
    }

    public String toString ()
    {
        return DomUtil.nodeAsString(context());
    }
    
    /**
     * @message com.arjuna.mwlabs.wsc.model.twophase.context.ArjunaContextImple_1 [com.arjuna.mwlabs.wsc.model.twophase.context.ArjunaContextImple_1] - ignoring context: 
     */
    public static ArjunaContextImple getContext()
    {
        ContextManager cxman = new ContextManager();
        Context[] contexts = cxman.contexts();

        for (int i = 0; i < contexts.length; i++)
        {
            if (contexts[i].identifier().equals(com.arjuna.mwlabs.wsc.model.twophase.context.ArjunaContextImple.class.getName()))
            {
                if (contexts[i] instanceof ArjunaContextImple)
                {
                    return (ArjunaContextImple)contexts[i] ;
                }
                else
                {
                    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsc.model.twophase.context.ArjunaContextImple_1",
                                  new Object[]{contexts[i]});
                }
                break;
            }
        }
        return null ;
    }

    private org.w3c.dom.Element _context;
    private String[] _identifierValues ;
    private int[] _expiresValues ;

    private static final String _wscoorNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor";
    private static final String _wsuNamespace = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    private static final String _arjunaNamespace = "http://arjuna.com/schemas/wsc/2003/01/extension";
    
    private static final String _contextName = "CoordinationContext";
    private static final String _identifier = "Identifier";
    private static final String _expires = "Expires";
    private static final String _coordinationType = "CoordinationType";

    public static final String _coordinationTypeURI = "urn:arjuna:tx-two-phase-commit";

}
