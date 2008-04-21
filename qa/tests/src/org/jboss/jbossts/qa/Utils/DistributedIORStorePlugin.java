/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.jbossts.qa.Utils;

import org.jboss.dtf.testframework.nameservice.NameNotBound;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;

import java.rmi.Naming;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DistributedIORStorePlugin.java,v 1.3 2003/07/30 12:16:20 jcoleman Exp $
 */

public class DistributedIORStorePlugin implements ServerIORStorePlugin
{
	private final static String NAME_SERVICE_URI_PROPERTY = "org.jboss.jbossts.qa.Utils.nameserviceuri";

	private NameServiceInterface _nameService = null;

	public void initialise() throws Exception
	{
		String nameServiceURI = System.getProperty(NAME_SERVICE_URI_PROPERTY);

		if (nameServiceURI == null)
		{
			throw new Exception("Name service uri not specified '" + NAME_SERVICE_URI_PROPERTY + "'");
		}

		_nameService = (NameServiceInterface) Naming.lookup(nameServiceURI);
	}

	public void storeIOR(String serverName, String serverIOR) throws Exception
	{
		_nameService.rebindReference(serverName, serverIOR);
	}

	public void removeIOR(String serverName) throws Exception
	{
		_nameService.unbindReference(serverName);
	}

	public String loadIOR(String serverName) throws Exception
	{
		try
		{
			return (String) _nameService.lookup(serverName);
		}
		catch (NameNotBound e)
		{
			return (null);
		}
	}

	public void remove()
	{
		// Do nothing
	}
}
