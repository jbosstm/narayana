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
// $Id: JDBCInfoTableImpl02.java,v 1.6 2004/04/21 08:30:53 jcoleman Exp $
//

package org.jboss.jbossts.qa.JDBCResources01Impls;

import org.jboss.jbossts.qa.JDBCResources01.*;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.JDBCProfileStore;
import org.omg.CORBA.StringHolder;
import org.omg.CosTransactions.Status;

import java.sql.*;
import java.util.Properties;

public class JDBCInfoTableImpl02 implements InfoTableOperations
{
	public JDBCInfoTableImpl02(String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass, int timeout)
			throws InvocationException
	{
		_databaseUser = databaseUser;
		Connection connection = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			if (databaseDynamicClass != null)
			{
				_databaseURL = databaseURL;

				_databaseProperties = new Properties();
				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.userName, databaseUser);
				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.password, databasePassword);
				_databaseProperties.put(com.arjuna.ats.jdbc.TransactionalDriver.dynamicClass, databaseDynamicClass);
			}
			else
			{
				_databaseURL = databaseURL;
				_databaseUser = databaseUser;
				_databasePassword = databasePassword;
				_databaseProperties = null;
			}
			_databaseTimeout = timeout;

			//create first connection to get metadata
			if (_databaseProperties != null)
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}

			DatabaseMetaData dbmd = connection.getMetaData();
			if (dbmd.getDatabaseProductName().startsWith("Microsoft"))
			{
				_useTimeout = true;
			}

		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl02.JDBCInfoTableImpl02: " + exception);
			exception.printStackTrace(System.err);
			throw new InvocationException();
		}
		finally
		{
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Closing connection");
			}
			try
			{
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void insert(String name, String value)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			System.err.println("02------------------ doing insert (" + name + "," + value + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			if (_databaseProperties != null)
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}

			statement = connection.createStatement();
			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}
            
            String tableName = JDBCProfileStore.getTableName(_databaseUser, "Infotable");

			System.err.println("INSERT INTO " + tableName + " VALUES(\'" + name + "\', \'" + value + "\')");
			statement.executeUpdate("INSERT INTO " + tableName + " VALUES(\'" + name + "\', \'" + value + "\')");

		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl02.insert: " + exception);
			exception.printStackTrace(System.err);
			throw new InvocationException();
		}
		finally
		{
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Performing explicit commit for non-transaction operation");
			}
			if (OTS.current().get_status().value() == Status._StatusNoTransaction)
			{
				try
				{
					connection.commit();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Closing connection");
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
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void update(String name, String value)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			System.err.println("02------------------ doing update (" + name + "," + value + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			if (_databaseProperties != null)
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}

			statement = connection.createStatement();
			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

            String tableName = JDBCProfileStore.getTableName(_databaseUser, "Infotable");
            
			System.err.println("UPDATE " + tableName + " SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");
			statement.executeUpdate("UPDATE " + tableName + " SET Value = \'" + value + "\' WHERE Name = \'" + name + "\'");

		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl02.update: " + exception);
			exception.printStackTrace(System.err);
			throw new InvocationException();
		}
		finally
		{
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Performing explicit commit for non-transaction operation");
			}
			if (OTS.current().get_status().value() == Status._StatusNoTransaction)
			{
				try
				{
					connection.commit();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Closing connection");
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
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void select(String name, StringHolder value)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			System.err.println("02------------------ doing select (" + name + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			if (_databaseProperties != null)
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}

			statement = connection.createStatement();
			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

            String tableName = JDBCProfileStore.getTableName(_databaseUser, "Infotable");
            
			System.err.println("SELECT Value FROM " + tableName + " WHERE Name = \'" + name + "\'");
			resultSet = statement.executeQuery("SELECT Value FROM " + tableName + " WHERE Name = \'" + name + "\'");

			if (!resultSet.next())
			{
				throw new Exception("Result set is empty - expected a row");
			}
			value.value = resultSet.getString("Value");
			if (resultSet.next())
			{
				throw new Exception("Result set is not empty - didn't expect a row");
			}
		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl02.select: " + exception);
			exception.printStackTrace(System.err);
			throw new InvocationException();
		}
		finally
		{
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Performing explicit commit for non-transaction operation");
			}
			if (OTS.current().get_status().value() == Status._StatusNoTransaction)
			{
				try
				{
					connection.commit();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Closing connection");
			}
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
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	public void delete(String name)
			throws InvocationException
	{
		Connection connection = null;
		Statement statement = null;

		if (System.getProperty("qa.debug") == "true")
		{
			System.err.println("Setting up connection");
		}
		try
		{
			System.err.println("02------------------ doing delete (" + name + ") -----------------------------");
			System.err.println("Current Status = " + OTS.current().get_status().value());
			if (_databaseProperties != null)
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
			}
			else
			{
				connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
			}

			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("connection = " + connection);
				System.err.println("Database URL = " + _databaseURL);
			}

			statement = connection.createStatement();
			if (_useTimeout)
			{
				statement.setQueryTimeout(_databaseTimeout);
			}

            String tableName = JDBCProfileStore.getTableName(_databaseUser, "Infotable");
            
			System.err.println("DELETE FROM " + tableName + " WHERE Name = \'" + name + "\'");
			statement.executeUpdate("DELETE FROM " + tableName + " WHERE Name = \'" + name + "\'");

		}
		catch (Exception exception)
		{
			System.err.println("JDBCInfoTableImpl02.delete: " + exception);
			exception.printStackTrace(System.err);
			throw new InvocationException();
		}
		finally
		{
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Performing explicit commit for non-transaction operation");
			}
			if (OTS.current().get_status().value() == Status._StatusNoTransaction)
			{
				try
				{
					connection.commit();
				}
				catch (Exception e)
				{
					System.err.println("Ignoring exception: " + e);
					e.printStackTrace(System.err);
				}
			}
			if (System.getProperty("qa.debug") == "true")
			{
				System.err.println("Closing connection");
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
				if (connection != null)
				{
					connection.close();
				}
			}
			catch (Exception e)
			{
				System.err.println("Ignoring exception: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	private String _databaseURL;
	private String _databaseUser;
	private String _databasePassword;
	private int _databaseTimeout;
	private Properties _databaseProperties;
	private boolean _useTimeout = false;
}
