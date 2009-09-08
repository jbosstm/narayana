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
// $Id: JDBCInfoTableImpl01.java,v 1.6 2004/04/21 12:45:01 jcoleman Exp $
//

package org.jboss.jbossts.qa.JDBCResources02Impls;

import org.jboss.jbossts.qa.JDBCResources02.*;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.omg.CORBA.StringHolder;
import org.omg.CosTransactions.Control;

import java.sql.*;
import java.util.Properties;

public class JDBCInfoTableImpl01 implements InfoTableOperations
{
	public JDBCInfoTableImpl01(String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass, int timeout)
			throws InvocationException
	{
		_dbUser = databaseUser;
		_databaseTimeout = timeout;

		if ("true".equals(System.getProperty("qa.debug")))
		{
			System.err.println("Setting up connection");
		}
		try
		{
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

			if ("true".equals(System.getProperty("qa.debug")))
			{
				System.err.println("connection = " + _connection);
				System.err.println("Database URL = " + databaseURL);
			}

			Runtime.getRuntime().addShutdownHook(new JDBC01ShutdownThread());
			DatabaseMetaData dbmd = _connection.getMetaData();
			if (dbmd.getDatabaseProductName().startsWith("Microsoft"))
			{
				_useTimeout = true;
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl01.JDBCInfoTableImpl01: " + exception);
			throw new InvocationException();
		}
	}

	public void insert(String name, String value, Control ctrl)
			throws InvocationException
	{
		Statement statement = null;

		try
		{
			System.err.println("01------------------ doing insert (" + name + "," + value + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			System.err.println("Control = " + ctrl);
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				statement = _connection.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

                String tableName = JDBCProfileStore.getTableName(_dbUser, "Infotable");
                
				System.err.println("INSERT INTO " + tableName+" VALUES(\'" + name + "\', \'" + value + "\')");
				statement.executeUpdate("INSERT INTO " + tableName + " VALUES(\'" + name + "\', \'" + value + "\')");
			}
			catch (Exception exception)
			{
				System.err.println("JDBCInfoTableImpl01.insert: " + exception);
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("JDBCInfoTableImpl01.insert: " + error);
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
					if ("true".equals(System.getProperty("qa.debug")))
					{
						System.err.println("Calling interposition.unregisterTransaction()");
					}
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
			System.err.println("JDBCInfoTableImpl01.insert: " + exception);
			throw new InvocationException();
		}
	}

	public void update(String name, String value, Control ctrl)
			throws InvocationException
	{
		Statement statement = null;

		try
		{
			System.err.println("01------------------ doing update (" + name + "," + value + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			System.err.println("Control = " + ctrl);
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				statement = _connection.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

                String tableName = JDBCProfileStore.getTableName(_dbUser, "Infotable");
                
				System.err.println("UPDATE " + tableName + " SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");
				statement.executeUpdate("UPDATE " + tableName + " SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");
			}
			catch (Exception exception)
			{
				System.err.println("JDBCInfoTableImpl01.update: " + exception);
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("JDBCInfoTableImpl01.update: " + error);
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
					if ("true".equals(System.getProperty("qa.debug")))
					{
						System.err.println("Calling interposition.unregisterTransaction()");
					}
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
			System.err.println("JDBCInfoTableImpl01.update: " + exception);
			throw new InvocationException();
		}
	}

	public void select(String name, StringHolder value, Control ctrl)
			throws InvocationException
	{
		Statement statement = null;
		ResultSet resultSet = null;

		try
		{
			System.err.println("01------------------ doing select (" + name + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			System.err.println("Control = " + ctrl);
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				statement = _connection.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

                String tableName = JDBCProfileStore.getTableName(_dbUser, "Infotable");
                
				System.err.println("SELECT Value FROM " + tableName + " WHERE Name = \'" + name + "\'");
				resultSet = statement.executeQuery("SELECT Value FROM " + tableName + " WHERE Name = \'" + name + "\'");
				resultSet.next();
				value.value = resultSet.getString("Value");
				if (resultSet.next())
				{
					throw new Exception();
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCInfoTableImpl01.select: " + exception);
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("JDBCInfoTableImpl01.select: " + error);
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
					if ("true".equals(System.getProperty("qa.debug")))
					{
						System.err.println("Calling interposition.unregisterTransaction()");
					}
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
			System.err.println("JDBCInfoTableImpl01.select: " + exception);
			throw new InvocationException();
		}
	}

	public void delete(String name, Control ctrl)
			throws InvocationException
	{
		Statement statement = null;

		try
		{
			System.err.println("01------------------ doing delete (" + name + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			System.err.println("Control = " + ctrl);
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();
			interposition.registerTransaction(ctrl);

			try
			{
				statement = _connection.createStatement();
				if (_useTimeout)
				{
					statement.setQueryTimeout(_databaseTimeout);
				}

                String tableName = JDBCProfileStore.getTableName(_dbUser, "Infotable");
                
				System.err.println("DELETE FROM " + tableName + " WHERE Name = \'" + name + "\'");
				statement.executeUpdate("DELETE FROM " + tableName + " WHERE Name = \'" + name + "\'");
			}
			catch (Exception exception)
			{
				System.err.println("JDBCInfoTableImpl01.delete: " + exception);
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("JDBCInfoTableImpl01.delete: " + error);
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
					if ("true".equals(System.getProperty("qa.debug")))
					{
						System.err.println("Calling interposition.unregisterTransaction()");
					}
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
			System.err.println("JDBCInfoTableImpl01.delete: " + exception);
			throw new InvocationException();
		}
	}

	private Connection _connection;
	private String _dbUser;
	private int _databaseTimeout;
	private boolean _useTimeout = false;

	/*
		 * We can't guarantee that finalize() will be called,
		 * so we have a thread that will close the database connection.
		 */
	private class JDBC01ShutdownThread extends Thread
	{
		public void run()
		{
			System.err.println("JDBCInfoTableImpl01.JDBC01ShutdownThread: running");
			try
			{
				if (_connection != null)
				{
					_connection.close();
				}
			}
			catch (Exception exception)
			{
				System.err.println("JDBCInfoTableImpl01.JDBC01ShutdownThread: " + exception);
				exception.printStackTrace(System.err);
			}
		}
	}
}
