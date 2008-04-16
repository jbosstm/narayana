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
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: JDBCServiceImpl02.java,v 1.2 2003/06/26 11:43:50 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery11Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCServiceImpl02.java,v 1.2 2003/06/26 11:43:50 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCServiceImpl02.java,v 1.2 2003/06/26 11:43:50 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery11.*;
import org.omg.CORBA.IntHolder;

import java.sql.*;
import java.util.Properties;

public class JDBCServiceImpl02 implements AfterCrashServiceOperations
{
	public JDBCServiceImpl02(String rowName, String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass)
			throws InvocationException
	{
		_dbUser = databaseUser;
		try
		{
			_rowName = rowName;

			if (databaseDynamicClass != null)
			{
				Properties databaseProperties = new Properties();

				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);

				_connection = DriverManager.getConnection(databaseURL, databaseProperties);
			}
			else
			{
				_connection = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			}

		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl02.JDBCServiceImpl02: " + exception);
			throw new InvocationException();
		}
	}

	public void finalize()
			throws Throwable
	{
		try
		{
			if (_connection != null)
			{
				_connection.close();
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl02.finalize: " + exception);
			throw exception;
		}
	}

	public void get(IntHolder value)
			throws InvocationException
	{
		try
		{
			Statement statement = _connection.createStatement();
/*
            ResultSet resultSet = statement.executeQuery("SELECT Value FROM " + _dbUser +"_Service WHERE Name = \'" + _rowName + "\'");
            resultSet.next();
            value.value = resultSet.getInt("Value");
            if (resultSet.next())
                throw new Exception();
*/

			System.err.println("JDBCServiceImpl02.get: " + "SELECT Value FROM " + _dbUser + "_Service WHERE Name = \'" + _rowName + "\'");

			ResultSet resultSet = statement.executeQuery("SELECT Value FROM " + _dbUser + "_Service WHERE Name = \'" + _rowName + "\'");

			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

			String columnTypeName = resultSetMetaData.getColumnTypeName(1);
			System.err.println("JDBCServiceImpl02.get: columnTypeName 1: " + columnTypeName);

			resultSet.next();
			value.value = resultSet.getInt("Value");

			System.err.println("JDBCServiceImpl02.select: value " + value.value);

			if (resultSet.next())
			{
				System.err.println("JDBCServiceImpl02.select: must have got another row");
				value.value = resultSet.getInt("Value");
				System.err.println("JDBCServiceImpl02.select: value " + value.value);
				throw new Exception();
			}

			resultSet.close();
			statement.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl02.select: " + exception);
			throw new InvocationException();
		}
	}

	private String _rowName;
	private Connection _connection;
	private String _dbUser;
}
