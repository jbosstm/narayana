/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.Hammer02Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.Hammer02.*;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JDBCMatrixImpl02 implements MatrixOperations
{
	public JDBCMatrixImpl02(int width, int height, String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass)
			throws InvocationException
	{
		_width = width;
		_height = height;
		_databaseUser = databaseUser;

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
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl02.JDBCMatrixImpl02: " + exception);
			throw new InvocationException();
		}
	}

	public int get_width()
			throws InvocationException
	{
		return _width;
	}

	public int get_height()
			throws InvocationException
	{
		return _height;
	}

	public void get_value(int x, int y, IntHolder value, Control ctrl)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				Connection connection;
				if (_databaseProperties != null)
				{
					connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
				}
				else
				{
					connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
				}

				Statement statement = connection.createStatement();

				ResultSet resultSet = statement.executeQuery("SELECT Value FROM " + _databaseUser + "_Matrix WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");
				resultSet.next();
				value.value = resultSet.getInt("Value");
				if (resultSet.next())
				{
					throw new Exception();
				}
				resultSet.close();

				statement.close();
				connection.close();
			}
			catch (Exception exception)
			{
				System.err.println("JDBCMatrixImpl02.get_value: " + exception);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("JDBCMatrixImpl02.get_value: " + error);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl02.get_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void set_value(int x, int y, int value, Control ctrl)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				Connection connection;
				if (_databaseProperties != null)
				{
					connection = DriverManager.getConnection(_databaseURL, _databaseProperties);
				}
				else
				{
					connection = DriverManager.getConnection(_databaseURL, _databaseUser, _databasePassword);
				}

				Statement statement = connection.createStatement();

				statement.executeUpdate("UPDATE " + _databaseUser + "_Matrix SET Value = \'" + value + "\' WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");

				statement.close();
				connection.close();
			}
			catch (Exception exception)
			{
				System.err.println("JDBCMatrixImpl02.set_value: " + exception);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			catch (Error error)
			{
				System.err.println("JDBCMatrixImpl02.set_value: " + error);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl02.set_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	private int _width;
	private int _height;

	private String _databaseURL;
	private String _databaseUser;
	private String _databasePassword;
	private Properties _databaseProperties;
}