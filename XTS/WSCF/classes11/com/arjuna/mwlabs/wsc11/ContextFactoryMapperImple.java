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
 * $Id: ContextFactoryMapperImple.java,v 1.12.4.1 2005/11/22 10:34:09 kconner Exp $
 */

package com.arjuna.mwlabs.wsc11;

import com.arjuna.mw.wscf.common.CoordinatorXSD;
import com.arjuna.mw.wscf11.protocols.ProtocolManager;
import com.arjuna.mw.wscf11.protocols.ProtocolRegistry;
import com.arjuna.wsc11.ContextFactoryMapper;
import com.arjuna.wsc11.ContextFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ContextFactoryMapperImple.java,v 1.11 2004/08/10 15:10:27
 *          kconner Exp $
 * @since 1.0.
 */

public class ContextFactoryMapperImple extends ContextFactoryMapper
{

	public ContextFactoryMapperImple()
	{
		_protocols = ProtocolRegistry.createManager();
	}

	public void addContextFactory (String coordinationTypeURI, ContextFactory contextFactory)
	{
		try
		{
			_protocols.addProtocol(convert(coordinationTypeURI), contextFactory);

			contextFactory.install(coordinationTypeURI);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void removeContextFactory (String coordinationTypeURI)
	{
		try
		{
			ContextFactory contextFactory = (ContextFactory) _protocols.removeProtocol(convert(coordinationTypeURI));

			contextFactory.uninstall(coordinationTypeURI);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/*
	 * The factory implementation must initialise the activity service with the
	 * correct HLS.
	 */

	public ContextFactory getContextFactory (String coordinationTypeURI)
	{
		try
		{
			org.w3c.dom.Document doc = convert(coordinationTypeURI);
			Object type = _protocols.getProtocolImplementation(convert(coordinationTypeURI));

			if (type instanceof String)
			{
				Class c = Class.forName((String) type);

				ContextFactory factory = (ContextFactory) c.newInstance();

				_protocols.replaceProtocol(doc, factory);

				return factory;
			}
			else
			{
				return (ContextFactory) type;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	public void setSubordinateContextFactoryMapper (ContextFactoryMapper subordinateContextFactoryMapper)
	{
	}

	public ContextFactoryMapper getSubordinateContextFactoryMapper ()
	{
		return null;
	}

	public void setDefaultContextFactory (ContextFactory defaultContextFactory)
	{
	}

	public ContextFactory getDefaultContextFactory ()
	{
		return null;
	}

	private org.w3c.dom.Document convert (String coordinationTypeURI)
			throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document doc = builder.newDocument();
		org.w3c.dom.Element rootElement = doc.createElement(_wscRootElement);
		org.w3c.dom.Element elem = doc.createElement(CoordinatorXSD.coordinatorType);

		elem.appendChild(doc.createTextNode(coordinationTypeURI));

		rootElement.appendChild(elem);

		doc.appendChild(rootElement);

		return doc;
	}

	private void writeObject (java.io.ObjectOutputStream objectOutputStream)
			throws java.io.IOException
	{
	}

	private void readObject (java.io.ObjectInputStream objectInputStream)
			throws java.io.IOException, ClassNotFoundException
	{
	}

	public static final String localName ()
	{
		// return "com/arjuna/mwlabs/wsc/contextfactorymapper";
		return "comarjunamwlabswsccontextfactorymapperws";
	}

	private ProtocolManager _protocols;

	private static final String _wscRootElement = "WS-C";

}