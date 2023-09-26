/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery11Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.CrashRecovery11.*;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
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

            String tableName = JDBCProfileStore.getTableName(_dbUser, "Service");

            ResultSet resultSet = statement.executeQuery("SELECT Value FROM " + tableName +" WHERE Name = '" + _rowName + "'");
            
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