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
 * $Id: ProtocolEntry.java,v 1.5 2005/05/19 12:13:39 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.protocols;

import com.arjuna.mw.wscf.common.CoordinatorXSD;

import com.arjuna.mw.wscf.utils.DomUtil;

/**
 * When an XML coordination protocol definition is loaded, an instance of this
 * class is created to manage it within the registry.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolEntry.java,v 1.5 2005/05/19 12:13:39 nmcl Exp $
 * @since 1.0.
 */

public class ProtocolEntry
{

    public ProtocolEntry (org.w3c.dom.Document doc)
    {
        this(doc, null);
    }

    public ProtocolEntry (org.w3c.dom.Document doc, Object implementation)
    {
	_doc = doc;
	_implementation = implementation;

	org.w3c.dom.Element rootElement = _doc.getDocumentElement();
		    
	_protocolType = DomUtil.getTextNode(rootElement, CoordinatorXSD.coordinatorType).getNodeValue();

	org.w3c.dom.Text name = DomUtil.getTextNode(rootElement, CoordinatorXSD.coordinatorName);
	
	if (name != null)
	    _protocolName = name.getNodeValue();
	else
	    _protocolName = null;	
    }
    
    public final org.w3c.dom.Document document ()
    {
	return _doc;
    }
    
    public final Object implementationClass ()
    {
	return _implementation;
    }

    public final String protocolType ()
    {
	return _protocolType;
    }
    
    public final String protocolName ()
    {
	return _protocolName;
    }
    
    public boolean equals (Object obj)
    {
	if (obj == null)
	    return false;
	else
	{
	    if (obj == this)
		return true;
	    else
	    {
		if (obj instanceof ProtocolEntry)
		{
		    ProtocolEntry entry = (ProtocolEntry) obj;
		    org.w3c.dom.Element rootElement = entry.document().getDocumentElement();
		    String ptype = DomUtil.getTextNode(rootElement, CoordinatorXSD.coordinatorType).getNodeValue();

		    return _protocolType.equals(ptype);
		}
	    }
	    
	    return false;
	}
    }

    public int hashCode ()
    {
	return _protocolType.hashCode();
    }

    public String toString ()
    {
	return DomUtil.nodeAsString(_doc);
    }
    
    private org.w3c.dom.Document _doc;
    private Object               _implementation;
    private String               _protocolType;
    private String               _protocolName;
    
}

