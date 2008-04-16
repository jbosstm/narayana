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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExampleXAConnectionRecovery.java,v 1.1 2004/10/13 15:45:47 nmcl Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery13Impls;

import com.arjuna.ats.jta.recovery.XAConnectionRecovery;

import javax.sql.XAConnection;
import java.sql.SQLException;

public class ExampleXAConnectionRecovery implements XAConnectionRecovery
{

	public XAConnection getConnection() throws SQLException
	{
		count = 1;

		return new ExampleXAConnection();
	}

	/**
	 * Initialise with all properties required to create the resource(s).
	 *
	 * @param String p An arbitrary string from which initialization data
	 *               is obtained.
	 * @return <code>true</code> if initialization happened successfully,
	 *         <code>false</code> otherwise.
	 */

	public boolean initialise(String p) throws SQLException
	{
		return true;
	}

	/**
	 * Iterate through all of the resources this instance provides
	 * access to.
	 *
	 * @return <code>true</code> if this instance can provide more
	 *         resources, <code>false</code> otherwise.
	 */

	public boolean hasMoreConnections()
	{
		boolean toReturn = false;

		if (count != 1)
		{
			toReturn = true;
		}

		// reset for next recovery scan

		count = 0;

		return toReturn;
	}

	private int count = 0;

}
