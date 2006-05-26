/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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

    public ProtocolLocator (String theType)
    {
	_className = theType.replace(_packageSeparator, _resourceSeparator);
	_xmlName = _className+".xml";
    }

    /**
     * @message com.arjuna.mwlabs.wscf.utils.ProtocolLocator_1 [com.arjuna.mwlabs.wscf.utils.ProtocolLocator_1] - not found
     * @message com.arjuna.mwlabs.wscf.utils.ProtocolLocator_2 [com.arjuna.mwlabs.wscf.utils.ProtocolLocator_2] - Failed to create: 
     */
     
    public final org.w3c.dom.Document getProtocol () throws FileNotFoundException, ProtocolLocationException
    {
	try
	{
	    URL url = ProtocolLocator.class.getResource(_resourceSeparator+_xmlName);

	    if (url == null)
	    {
		throw new FileNotFoundException(_xmlName+" "+wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.utils.ProtocolLocator_1"));
	    }
	    else
	    {
		DocumentBuilder builder = DomUtil.getDocumentBuilder();
		org.w3c.dom.Document doc = builder.parse(url.toExternalForm());
	    
		if (doc == null)
		{
		    throw new FileNotFoundException(wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.utils.ProtocolLocator_2")+" "+_xmlName);
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

    private String _className;
    private String _xmlName;
    
    private final static char _packageSeparator = '.';
    private final static char _resourceSeparator = '/';
    
}

