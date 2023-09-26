/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery09Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
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