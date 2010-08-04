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
 * $Id: ArjunaContextImple.java,v 1.6.6.1 2005/11/22 10:36:16 kconner Exp $
 */

package com.arjuna.mwlabs.wst11.ba.context;

import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;
import com.arjuna.mw.wsas.UserActivityFactory;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.context.Context;
import com.arjuna.mw.wsas.context.ContextManager;
import com.arjuna.mw.wsas.context.soap.SOAPContext;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.utils.DomUtil;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.BACoordinator;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;

/**
 * On demand this class creates the SOAP context information necessary to
 * propagate the hierarchy of coordinators associated with the current
 * thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ArjunaContextImple.java,v 1.6.6.1 2005/11/22 10:36:16 kconner Exp $
 */

public class ArjunaContextImple implements SOAPContext
{

    public ArjunaContextImple()
    {
        _context = null;
    }

    public ArjunaContextImple(BACoordinator currentCoordinator)
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
    	    BACoordinator currentCoordinator = (BACoordinator) param;

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
    	    throw new IllegalArgumentException();
    	}
    }

    /**
     * @return the context document object.
     */
    private synchronized Element context()
    {
        // TODO - work out which bits of this we can do using JAXB
        // TODO - sort out expires etc

        if (_context == null)
        {
            DocumentBuilder builder = DomUtil.getDocumentBuilder();
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

                Element identifier = doc.createElement("wsu:"+_identifier);
                identifier.appendChild(doc.createTextNode(_identifierValues[0]));

                _context.appendChild(identifier);

                Element expires = doc.createElement("wsu:"+_expires);
                expires.appendChild(doc.createTextNode(Integer.toString(_expiresValues[0])));

                _context.appendChild(expires);

                Element coordinationType = doc.createElement("wscoor:" + _coordinationType);
                coordinationType.appendChild(doc.createTextNode(BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME));

                _context.appendChild(coordinationType);

                /*
                 * Now let's do the optional stuff.
                 */
                final int depth = _identifierValues.length ;
                if (depth > 1)
                {
                    Element extensionRoot = doc.createElement("arjuna:"+_contextName);

                    for(int count = 1; count < depth ; count++)
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
        return _context ;
    }

    public String identifier ()
    {
        return ArjunaContextImple.class.getName();
    }

    public String getTransactionIdentifier()
    {
        return _identifierValues[0] ;
    }

    public int getTransactionExpires()
    {
        return _expiresValues[0] ;
    }

    public String toString ()
    {
        return DomUtil.nodeAsString(_context);
    }

    public static ArjunaContextImple getContext()
    {
        ContextManager cxman = new ContextManager();
        Context context = cxman.context("Sagas11HLS");

        if (context instanceof ArjunaContextImple)
        {
            return (ArjunaContextImple)context ;
        }
        else
        {
            wstxLogger.i18NLogger.warn_mwlabs_wst11_ba_context_ArjunaContextImple_1(context.toString());
        }

        return null ;
    }

    private Element _context;
    private String[] _identifierValues ;
    private int[] _expiresValues ;

    private static final String _wscoorNamespace = CoordinationConstants.WSCOOR_NAMESPACE;
    private static final String _wsuNamespace = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    private static final String _arjunaNamespace = "http://arjuna.com/schemas/wsc/2003/01/extension";

    private static final String _contextName = CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT;
    private static final String _identifier = CoordinationConstants.WSCOOR_ELEMENT_IDENTIFIER;
    private static final String _expires = CoordinationConstants.WSCOOR_ELEMENT_EXPIRES;
    private static final String _coordinationType = CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_TYPE;

}