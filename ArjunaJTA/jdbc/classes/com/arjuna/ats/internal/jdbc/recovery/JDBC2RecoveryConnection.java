/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBC2RecoveryConnection.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.recovery;

import com.arjuna.ats.internal.jdbc.ConnectionImple;

import java.util.Properties;
import java.sql.*;

/**
 * To perform recovery on arbitrary connections we may need to recreate those
 * connections.
 * 
 * The ArjunaJDBC2Connection class must not be used directly by applications,
 * hence the requirement for this class.
 * 
 * @since JTS 2.1.
 */

public class JDBC2RecoveryConnection extends ConnectionImple
{

	public JDBC2RecoveryConnection (String dbName, Properties info)
			throws SQLException
	{
		super(dbName, info);
	}

	public JDBC2RecoveryConnection (String dbName, String user, String passwd)
			throws SQLException
	{
		this(dbName, user, passwd, null);
	}

	public JDBC2RecoveryConnection (String dbName, String user, String passwd,
			String dynamic) throws SQLException
	{
		super(dbName, user, passwd, dynamic);
	}

}
