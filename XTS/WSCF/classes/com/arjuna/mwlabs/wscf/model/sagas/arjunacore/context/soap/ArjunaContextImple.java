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
 * $Id: ArjunaContextImple.java,v 1.3.4.1 2005/11/22 10:34:15 kconner Exp $
 */

package com.arjuna.mwlabs.wscf.model.sagas.arjunacore.context.soap;

import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator;

import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;

import com.arjuna.mw.wsas.context.soap.SOAPContext;

import com.arjuna.mw.wscf.utils.DomUtil;

import javax.xml.parsers.*;

import org.w3c.dom.Element;

/**
 * On demand this class creates the SOAP context information necessary to
 * propagate the hierarchy of coordinators associated with the current
 * thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ArjunaContextImple.java,v 1.3.4.1 2005/11/22 10:34:15 kconner Exp $
 */

public class ArjunaContextImple implements SOAPContext
{

    /**
     * Incomplete. Need things like the Coordinator URI!
     */

    // TODO make sure all SOAPContexts have a default constructor!

    public ArjunaContextImple ()
    {
    }
    
    public ArjunaContextImple (ACCoordinator currentCoordinator)
    {
	initialiseContext(currentCoordinator);
    }

    public void initialiseContext(Object param)
    {
	try
	{
	    ACCoordinator currentCoordinator = (ACCoordinator) param;
	    DocumentBuilder builder = DomUtil.getDocumentBuilder();
	    org.w3c.dom.Document doc = builder.newDocument();
	
	    _context = doc.createElement(_contextName);
	    _context.appendChild(doc.createComment("WARNING: example Arjuna coordinator hierarchy only!"));
	
	    if (currentCoordinator != null)
	    {
		ActionHierarchy hier = currentCoordinator.getHierarchy();
	
		for (int i = 0; i < hier.depth(); i++)
		{
		    org.w3c.dom.Element elem = doc.createElement(_coordName);
		
		    elem.appendChild(doc.createTextNode("http://arjuna.com?"+hier.getActionUid(i).stringForm()));
		
		    _context.appendChild(elem);
		}
	    }
	}
	catch (Exception ex)
	{
	    throw new java.lang.IllegalArgumentException(ex.toString());
	}
    }
    
    public Element serialiseToElement(final Element element)
    {
        element.appendChild(_context) ;
        return _context ;
    }

    public String identifier ()
    {
	return ArjunaContextImple.class.getName();
    }

    public String toString ()
    {
	return DomUtil.nodeAsString(_context);
    }

    private org.w3c.dom.Element _context = null;
    
    private static final String _contextName = "sagas-arjunacore-context";
    private static final String _coordName = "coordinator-name";

}
