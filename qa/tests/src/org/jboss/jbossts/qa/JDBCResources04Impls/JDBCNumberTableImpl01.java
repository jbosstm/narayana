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
// $Id: JDBCNumberTableImpl01.java,v 1.10 2004/06/14 09:10:05 swheater Exp $
//

package org.jboss.jbossts.qa.JDBCResources04Impls;

import org.jboss.jbossts.qa.JDBCResources04.*;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

import java.sql.*;
import java.util.Hashtable;
import java.util.Properties;

public class JDBCNumberTableImpl01 implements NumberTableOperations
{
	public JDBCNumberTableImpl01(String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass, int timeout)
			throws InvocationException
	{
		_dbUser = databaseUser;
		_databaseURL = databaseURL;
		_databasePassword = databasePassword;
		_databaseDynamicClass = databaseDynamicClass;
		_databaseTimeout = timeout;

		if (databaseDynamicClass != null)
		{
			_databaseProperties = new Properties();

			_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
			_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
			_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);
		}

		try
		{
			Connection connection = getConnection();
			Runtime.getRuntime().addShutdownHook(new JDBC01ShutdownThread());
			DatabaseMetaData dbmd = connection.getMetaData();
			if (dbmd.getDatabaseProductName().startsWith("Microsoft"))
			{
				System.err.println("SQLServer message");
				_useTimeout = true;
			}
			else if (dbmd.getDatabaseProductName().equals("DBMS:cloudscape"))
			{
				System.err.println("setting CLOUD message");
			}

			connection.close();
		}
		catch (Exception e)
		{
			System.err.println("JDBCNumberTableImpl01.JDBCNumberTableImpl01: " + e);
			throw new InvocationException();
		}
	}

	public void get(String name, IntHolder value, Control ctrl)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				System.err.println("-- get called --");
				connection = getConnection();
				statement = getConnection().createStatement();

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

					_connections.put(tx, connection);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl01.get: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("JDBCNumberTableImpl01.get: " + error);
				throw new InvocationException();
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
				try
				{
					interposition.unregisterTransaction();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.get: " + exception);
			throw new InvocationException();
		}
	}

	public void set(String name, int value, Control ctrl)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				connection = getConnection();
				statement = getConnection().createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

				System.err.println("UPDATE " + _dbUser + "_NumberTable SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");
				statement.executeUpdate("UPDATE " + _dbUser + "_NumberTable SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");

				try
				{
					javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
					javax.transaction.Transaction tx = (javax.transaction.Transaction) tm.getTransaction();

					_connections.put(tx, connection);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl01.set: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("JDBCNumberTableImpl01.set: " + error);
				throw new InvocationException();
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
				try
				{
					interposition.unregisterTransaction();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.set: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void increase(String name, Control ctrl)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				System.err.println("-- increase --");

				connection = getConnection();
				statement = getConnection().createStatement();

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

					_connections.put(tx, connection);
				}
				catch (Exception ex)
				{
					System.err.println(ex);
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl01.increase: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("JDBCNumberTableImpl01.increase: " + error);
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
				try
				{
					interposition.unregisterTransaction();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("JDBCNumberTableImpl01.increase: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	private Connection getConnection()
			throws Exception
	{
		if ("true".equals(System.getProperty("qa.debug")))
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
					conn = DriverManager.getConnection(_databaseURL, _dbUser, _databasePassword);
				}
			}

			if ("true".equals(System.getProperty("qa.debug")))
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
	private String _databaseURL;
	private String _dbUser;
	private String _databasePassword;
	private String _databaseDynamicClass;
	private int _databaseTimeout;
	private Properties _databaseProperties;
	private boolean _useTimeout = false;

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
				java.util.Enumeration connections = _connections.elements();
				while (connections.hasMoreElements())
				{
					((Connection) connections.nextElement()).close();
				}
				connections = null;
			}
			catch (Exception exception)
			{
				System.err.println("JDBCNumberTableImpl01.JDBC01ShutdownThread: " + exception);
				exception.printStackTrace(System.err);
			}
		}
	}
}
