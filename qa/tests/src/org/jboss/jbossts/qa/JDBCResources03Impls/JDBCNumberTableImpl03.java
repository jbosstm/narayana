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
// $Id: JDBCNumberTableImpl03.java,v 1.6 2004/06/11 09:14:25 jcoleman Exp $
//

package org.jboss.jbossts.qa.JDBCResources03Impls;

import org.jboss.jbossts.qa.JDBCResources03.*;
import org.omg.CORBA.IntHolder;

import java.sql.*;
import java.util.Hashtable;
import java.util.Properties;

public class JDBCNumberTableImpl03 implements NumberTableOperations
{
	public JDBCNumberTableImpl03(String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass, int timeout)
			throws InvocationException
	{
		_dbUser = databaseUser;
		_databaseTimeout = timeout;
		_databaseURL = databaseURL;
		_dbPassword = databasePassword;

		try
		{
			if (databaseDynamicClass != null)
			{
				_databaseProperties = new Properties();

				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);

				_connection = DriverManager.getConnection(databaseURL, _databaseProperties);
			}
			else
			{
				_connection = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
			}

			DatabaseMetaData dbmd = _connection.getMetaData();
			if (dbmd.getDatabaseProductName().startsWith("Microsoft"))
			{
				_useTimeout = true;
			}
			else if (dbmd.getDatabaseProductName().equals("FirstSQL/J"))
			{
				_useTimeout = true;
			}

			_connection.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl03.JDBCNumberTableImpl03: " + exception);
			throw new InvocationException();
		}
	}

	public void get(String name, IntHolder value)
			throws InvocationException
	{
		Statement statement = null;
		ResultSet resultSet = null;

		System.err.println("-- get called --");
		while (true)
		{
			try
			{
				Connection conn = getConnection();
				statement = conn.createStatement();
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

				try
				{
					javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
					javax.transaction.Transaction tx = (javax.transaction.Transaction) tm.getTransaction();

					_connections.put(tx, conn);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}

				return;
			}
			catch (SQLException ex)
			{
				System.err.println("JDBCNumberTableImpl03.get: " + ex);
				String message = ex.getMessage();

				if (message.indexOf("already associated") == -1)
				{
					throw new InvocationException(Reason.ReasonUnknown);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl03.get: " + exception);
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
	}

	public void set(String name, int value)
			throws InvocationException
	{
		Statement statement = null;

		System.err.println("-- set called --");
		while (true)
		{
			try
			{
				Connection conn = getConnection();
				statement = conn.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

				System.err.println("UPDATE " + _dbUser + "_NumberTable SET Value = " + value + " WHERE Name = \'" + name + "\'");
				statement.executeUpdate("UPDATE " + _dbUser + "_NumberTable SET Value = " + value + " WHERE Name = \'" + name + "\'");

				try
				{
					javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
					javax.transaction.Transaction tx = (javax.transaction.Transaction) tm.getTransaction();

					_connections.put(tx, conn);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}

				return;
			}
			catch (java.sql.SQLException sqlException)
			{
				System.err.println("JDBCNumberTableImpl03.set: " + sqlException);

				// Check error message to see if it is a "can't serialize access" message
				String message = sqlException.getMessage();

				if ((message != null) && (message.indexOf("can't serialize access") != -1))
				{
					throw new InvocationException(Reason.ReasonCantSerializeAccess);
				}
				else if ((message != null) && (message.indexOf("deadlock") != -1))
				{
					throw new InvocationException(Reason.ReasonCantSerializeAccess);
				}

				if (message.indexOf("already associated") == -1)
				{
					throw new InvocationException(Reason.ReasonUnknown);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl03.set: " + exception);
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
	}

	public void increase(String name)
			throws InvocationException
	{
		Statement statement = null;

		System.err.println("-- set called --");
		while (true)
		{
			try
			{
				Connection conn = getConnection();
				statement = conn.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

				System.err.println("UPDATE " + _dbUser + "_NumberTable SET Value = Value + 1 WHERE NAME = \'" + name + "\'");
				statement.executeUpdate("UPDATE " + _dbUser + "_NumberTable SET Value = Value + 1 WHERE NAME = \'" + name + "\'");

				try
				{
					javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
					javax.transaction.Transaction tx = (javax.transaction.Transaction) tm.getTransaction();

					_connections.put(tx, conn);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}

				return;
			}
			catch (java.sql.SQLException sqlException)
			{
				System.err.println("JDBCNumberTableImpl03.increase: " + sqlException);
				// Check error message to see if it is a "can't serialize access" message
				String message = sqlException.getMessage();

				if ((message != null) && (message.indexOf("can't serialize access") != -1))
				{
					throw new InvocationException(Reason.ReasonCantSerializeAccess);
				}
				else if ((message != null) && (message.indexOf("deadlock") != -1))
				{
					throw new InvocationException(Reason.ReasonCantSerializeAccess);
				}

				if (message.indexOf("already associated") == -1)
				{
					throw new InvocationException(Reason.ReasonUnknown);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl03.increase: " + exception);
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
	}

	private Connection getConnection() throws SQLException
	{
		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
			javax.transaction.Transaction tx = (javax.transaction.Transaction) tm.getTransaction();

			Connection conn = (Connection) _connections.get(tx);

			if (conn == null)
			{
				System.err.println("**creating connection");

				if (_databaseProperties != null)
				{
					conn = DriverManager.getConnection(_databaseURL, _databaseProperties);
				}
				else
				{
					conn = DriverManager.getConnection(_databaseURL, _dbUser, _dbPassword);
				}
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("conn = " + conn);
				System.err.println("Database URL = " + _databaseURL);
			}
			System.err.println("returning " + conn + " for " + tx);

			return conn;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.toString());
		}
	}

	private Hashtable _connections = new Hashtable();
	private Connection _connection;
	private String _dbUser;
	private int _databaseTimeout;
	private boolean _useTimeout = false;
	private Properties _databaseProperties;
	private String _databaseURL;
	private String _dbPassword;
}
