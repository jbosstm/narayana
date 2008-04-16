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
// $Id: JDBCNumberTableImpl01.java,v 1.6 2004/06/11 09:14:25 jcoleman Exp $
//

package org.jboss.jbossts.qa.JDBCResources03Impls;

import org.jboss.jbossts.qa.JDBCResources03.*;
import org.omg.CORBA.IntHolder;

import java.sql.*;
import java.util.Properties;

public class JDBCNumberTableImpl01 implements NumberTableOperations
{
	public JDBCNumberTableImpl01(String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass, int timeout)
			throws InvocationException
	{
		_dbUser = databaseUser;
		_databaseTimeout = timeout;
		_databaseURL = databaseURL;
		_dbUser = databaseUser;
		_databasePassword = databasePassword;
		_databaseDynamicClass = databaseDynamicClass;
		_databaseTimeout = timeout;

		try
		{
			_connection = getConnection();
			Runtime.getRuntime().addShutdownHook(new JDBC01ShutdownThread());
			DatabaseMetaData dbmd = _connection.getMetaData();
			if (dbmd.getDatabaseProductName().startsWith("Microsoft"))
			{
				System.err.println("SQLServer message");
				_useTimeout = true;
				_message = "was deadlocked on";
			}
			else if (dbmd.getDatabaseProductName().equals("DBMS:cloudscape"))
			{
				System.err.println("setting CLOUD message");
				_message = "A lock could not be obtained";
			}
			else if (dbmd.getDatabaseProductName().equals("FirstSQL/J"))
			{
				_useTimeout = true;
			}
		}
		catch (Exception e)
		{
			System.err.println("JDBCNumberTableImpl01.JDBCNumberTableImpl01: " + e);
			throw new InvocationException();
		}
	}

	public void get(String name, IntHolder value)
			throws InvocationException
	{
		Statement statement = null;
		ResultSet resultSet = null;

		try
		{
			System.err.println("-- get called --");
			statement = null;
			if (!_newConnection)
			{
				statement = _connection.createStatement();
			}
			else
			{
				statement = getConnection().createStatement();
			}

			_newConnection = false;

			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

			System.err.println("SELECT Value FROM " + _dbUser + "_NumberTable WHERE Name = \'" + name + "\'");
			resultSet = statement.executeQuery("SELECT Value FROM " + _dbUser + "_NumberTable WHERE Name = \'" + name + "\'");
			resultSet.next();
			value.value = resultSet.getInt("Value");
			if (resultSet.next())
			{
				throw new Exception();
			}
		}
		catch (java.sql.SQLException sqlException)
		{
			System.err.println("JDBCNumberTableImpl01.get: " + sqlException);
			// Check error message to see if it is a "can't serialize access" message
			String message = sqlException.getMessage();

			if ((message != null) && (message.indexOf("connection is already associated with a different transaction") != -1))
			{
				try
				{
					_newConnection = true;
					get(name, value);
				}
				catch (Exception e)
				{
					System.err.println("Extra exception: " + e);
				}
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.get: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
		finally
		{
			try
			{
				if (resultSet != null)
				{
					resultSet.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void set(String name, int value)
			throws InvocationException
	{
		Statement statement = null;

		try
		{
			System.err.println("-- set called --");
			statement = _connection.createStatement();
			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

			System.err.println("UPDATE " + _dbUser + "_NumberTable SET Value = " + value + " WHERE Name = \'" + name + "\'");
			statement.executeUpdate("UPDATE " + _dbUser + "_NumberTable SET Value = " + value + " WHERE Name = \'" + name + "\'");
		}
		catch (java.sql.SQLException sqlException)
		{
			System.err.println("JDBCNumberTableImpl01.set: " + sqlException);
			// Check error message to see if it is a "can't serialize access" message
			String message = sqlException.getMessage();

			if ((message != null) && (message.indexOf(_message) != -1))
			{
				throw new InvocationException(Reason.ReasonCantSerializeAccess);
			}
			throw new InvocationException(Reason.ReasonUnknown);
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.set: " + exception);
			exception.printStackTrace();
			throw new InvocationException(Reason.ReasonUnknown);
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void increase(String name)
			throws InvocationException
	{
		Statement statement = null;

		try
		{
			System.err.println("-- increase --");
			statement = null;
			if (!_newConnection)
			{
				statement = _connection.createStatement();
			}
			else
			{
				statement = getConnection().createStatement();
			}

			_newConnection = false;

			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

			System.err.println("UPDATE " + _dbUser + "_NumberTable SET Value = Value + 1 WHERE NAME = \'" + name + "\'");
			statement.executeUpdate("UPDATE " + _dbUser + "_NumberTable SET Value = Value + 1 WHERE NAME = \'" + name + "\'");
		}
		catch (java.sql.SQLException sqlException)
		{
			System.err.println("JDBCNumberTableImpl01.increase: " + sqlException);
			// Check error message to see if it is a "can't serialize access" message
			String message = sqlException.getMessage();

			if ((message != null) && (message.indexOf(_message) != -1))
			{
				throw new InvocationException(Reason.ReasonCantSerializeAccess);
			}
			if ((message != null) && (message.indexOf("connection is already associated with a different transaction") != -1))
			{
				try
				{
					_newConnection = true;
					increase(name);
				}
				catch (InvocationException ie)
				{
					System.err.println("Invoc exception pass this to client");
					if (ie.myreason == Reason.ReasonCantSerializeAccess)
					{
						throw new InvocationException(Reason.ReasonCantSerializeAccess);
					}
					else
					{
						throw new InvocationException(Reason.ReasonUnknown);
					}
				}
				catch (Exception e)
				{
					System.err.println("Extra exception: " + e);
				}
			}
			throw new InvocationException(Reason.ReasonUnknown);
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.increase: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
		finally
		{
			try
			{
				if (statement != null)
				{
					statement.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	private Connection getConnection()
			throws Exception
	{
		Connection connection = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}

		try
		{
			if (_databaseDynamicClass != null)
			{
				Properties databaseProperties = new Properties();

				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, _dbUser);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, _databasePassword);
				databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, _databaseDynamicClass);

				connection = DriverManager.getConnection(_databaseURL, databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _dbUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.getConnection: " + exception);
			throw new Exception("error in getConnection:" + exception);
		}
		return connection;
	}

	private Connection _connection;
	private String _databaseURL;
	private String _dbUser;
	private String _databasePassword;
	private String _databaseDynamicClass;
	private int _databaseTimeout;
	private boolean _useTimeout = false;
	private boolean _newConnection = false;
	private String _message = "can't serialize access";

	/*
		 * We can't guarantee that finalize() will be called,
		 * so we have a thread that will close the database connection.
		 */
	private class JDBC01ShutdownThread extends Thread
	{
		public void run()
		{
			System.err.println("JDBCNumberTableImpl01.JDBC01ShutdownThread: running");
			try
			{
				if (_connection != null)
				{
					_connection.close();
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl01.JDBC01ShutdownThread: " + exception);
				exception.printStackTrace(System.err);
			}
		}
	}
}
