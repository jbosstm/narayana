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

public class JDBCMatrixImpl01 implements MatrixOperations
{
	public JDBCMatrixImpl01(int width, int height, String databaseURL, String databaseUser, String databasePassword, String databaseDynamicClass)
			throws InvocationException
	{
		_width = width;
		_height = height;
		_dbUser = databaseUser;

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
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.JDBCMatrixImpl01: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void finalize()
			throws Throwable
	{
		try
		{
			if (_connection != null)
			{
				;
			}
			_connection.close();
		}
		catch (Exception exception)
		{
			System.err.println("JDBCMatrixImpl01.finalize: " + exception);
			throw exception;
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
				Statement statement = _connection.createStatement();

				ResultSet resultSet = statement.executeQuery("SELECT Value FROM " + _dbUser + "_Matrix WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");
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
				System.err.println("JDBCMatrixImpl01.get_value: " + exception);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			catch (Error error)
			{
				System.err.println("JDBCMatrixImpl01.get_value: " + error);

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
			System.err.println("JDBCMatrixImpl01.get_value: " + exception);
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
				Statement statement = _connection.createStatement();

				statement.executeUpdate("UPDATE " + _dbUser + "_Matrix SET Value = \'" + value + "\' WHERE X = \'" + x + "\' AND Y = \'" + y + "\'");

				statement.close();
			}
			catch (Exception exception)
			{
				System.err.println("JDBCMatrixImpl01.set_value: " + exception);

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("JDBCMatrixImpl01.set_value: " + error);

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
			System.err.println("JDBCMatrixImpl01.set_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	private int _width;
	private int _height;

	private Connection _connection;
	private String _dbUser;
}