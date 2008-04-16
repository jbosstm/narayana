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
// $Id: JDBCServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery09Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
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
 * $Id: JDBCServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.omg.CORBA.IntHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JDBCServiceImpl01 implements ServiceOperations
{
	public JDBCServiceImpl01(String rowName, String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass)
			throws InvocationException
	{
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

			Statement statement = _connection.createStatement();

			statement.executeUpdate("INSERT Service SET Value = \'0\' WHERE Name = \'TheEntry\'");

			statement.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl01.JDBCServiceImpl01: " + exception);
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
			System.err.println("JDBCServiceImpl01.finalize: " + exception);
			throw exception;
		}
	}

	public void set(int value)
			throws InvocationException
	{
		try
		{
			Statement statement = _connection.createStatement();

			statement.executeUpdate("UPDATE Service SET Value = \'" + value + "\' WHERE Name = \'" + _rowName + "\'");

			statement.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl01.set: " + exception);
			throw new InvocationException();
		}
	}

	public void get(IntHolder value)
			throws InvocationException
	{
		try
		{
			Statement statement = _connection.createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT Value FROM Service WHERE Name = \'" + _rowName + "\'");
			resultSet.next();
			value.value = resultSet.getInt("Value");
			if (resultSet.next())
			{
				throw new Exception();
			}

			resultSet.close();
			statement.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCServiceImpl01.select: " + exception);
			throw new InvocationException();
		}
	}

	private String _rowName;
	private Connection _connection;
}
