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
 * $Id: ProtocolLocator.java,v 1.8 2005/05/19 12:13:39 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.utils;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mwlabs.wscf.exceptions.ProtocolLocationException;

import com.arjuna.mw.wscf.utils.DomUtil;

import javax.xml.parsers.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

/**
 * Locates and loads a specified XML definition of a coordination protocol.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolLocator.java,v 1.8 2005/05/19 12:13:39 nmcl Exp $
 * @since 1.0.
 */

public class ProtocolLocator
{

    public ProtocolLocator (Class clazz)
    {
    _clazz = clazz;
    _className = clazz.getName().replace(_packageSeparator, _resourceSeparator);
	_xmlName = _className+".xml";
    }

     
    public final org.w3c.dom.Document getProtocol () throws FileNotFoundException, ProtocolLocationException
    {
	try
	{
	    URL url = _clazz.getResource(_resourceSeparator+_xmlName);

	    if (url == null)
	    {
		throw new FileNotFoundException(_xmlName + " " + wscfLogger.i18NLogger.get_utils_ProtocolLocator_1());
	    }
	    else
	    {
		DocumentBuilder builder = DomUtil.getDocumentBuilder();
		org.w3c.dom.Document doc = builder.parse(url.toExternalForm());
	    
		if (doc == null)
		{
		    throw new FileNotFoundException(wscfLogger.i18NLogger.get_utils_ProtocolLocator_2() + " "+_xmlName);
		}
		else
		    return doc;
	    }
	}
	catch (FileNotFoundException ex)
	{
	    throw ex;
	}
	catch (IOException ex)
	{
	    throw new FileNotFoundException(ex.toString());
	}
	catch (Exception ex)
	{
	    throw new ProtocolLocationException(ex.toString());
	}
    }

    /**
     * we load the protocol document as a resource via the associated class assuming that they are packaged in
     * same archive using the same path and base file name but with extension xml in place of class.
     */
    private Class _clazz;
    private String _className;
    private String _xmlName;
    
    private final static char _packageSeparator = '.';
    private final static char _resourceSeparator = '/';
    
}

