/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.EnlistDelistEnlist01Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.EnlistDelistEnlist01.*;

import javax.naming.InitialContext;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.util.Hashtable;

public class EnlistDelistEnlistImpl01 implements ServiceOperations
{
	private boolean _isCorrect = true;
	private XADataSource _xaDataSource = null;

	private String _databaseUser;
	private String _databasePassword;

	public EnlistDelistEnlistImpl01(String binding, String databaseUser, String databasePassword)
			throws InvocationException
	{
		_databaseUser = databaseUser;
		_databasePassword = databasePassword;

		try
		{
			Hashtable env = new Hashtable();
			String initialCtx = System.getProperty("Context.INITIAL_CONTEXT_FACTORY");
			String bindingsLocation = System.getProperty("Context.PROVIDER_URL");

			if (bindingsLocation != null)
			{
				env.put(javax.naming.Context.PROVIDER_URL, bindingsLocation);
			}

			env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, initialCtx);
			javax.naming.Context ctx = new InitialContext(env);
			_xaDataSource = (XADataSource) ctx.lookup(binding);
		}
		catch (Exception exception)
		{
			System.err.println("EnlistDelistEnlist01.constructor: " + exception);
			throw new InvocationException();
		}
	}

	public void finalize()
			throws Throwable
	{
		try
		{
			// close XADataSource ?
		}
		catch (Exception exception)
		{
			System.err.println("EnlistDelistEnlist01.finalize: " + exception);
			throw exception;
		}
	}

	public boolean isCorrect()
			throws InvocationException
	{
		return _isCorrect;
	}

	public void begin_begin()
			throws InvocationException
	{
		boolean correct = true;

		try
		{
			XAConnection xaConnection = _xaDataSource.getXAConnection(_databaseUser, _databasePassword);

			XAResource xaResource = xaConnection.getXAResource();

			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			tm.begin();

			Transaction transaction = tm.getTransaction();

			correct = correct && transaction.enlistResource(xaResource);

			if (correct)
			{
				try
				{
					tm.begin();
					correct = false;
				}
				catch (NotSupportedException notSupportedException)
				{
					// correct behaviour for nested XA transaction
				}
			}

			tm.rollback();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			correct = false;
		}

		_isCorrect = _isCorrect && correct;
		return;
	}

	public void begin_enlist_delist_enlist_commit()
			throws InvocationException
	{
		boolean correct = true;

		try
		{
			XAConnection xaConnection = _xaDataSource.getXAConnection(_databaseUser, _databasePassword);

			XAResource xaResource = xaConnection.getXAResource();

			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			tm.begin();

			Transaction transaction = tm.getTransaction();

			correct = correct && transaction.enlistResource(xaResource);

			if (correct)
			{
				correct = correct && transaction.delistResource(xaResource, XAResource.TMSUCCESS);
			}

			if (correct)
			{
				correct = correct && transaction.enlistResource(xaResource);
			}

			if (correct)
			{
				tm.commit();
			}
			else
			{
				tm.rollback();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			correct = false;
		}

		_isCorrect = _isCorrect && correct;
		return;
	}

	public void begin_enlist_delist_close_commit()
			throws InvocationException
	{
		boolean correct = true;

		try
		{
			XAConnection xaConnection = _xaDataSource.getXAConnection(_databaseUser, _databasePassword);

			XAResource xaResource = xaConnection.getXAResource();
			Connection conn = xaConnection.getConnection();

			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			tm.begin();

			Transaction transaction = tm.getTransaction();

			correct = correct && transaction.enlistResource(xaResource);

			if (correct)
			{
				correct = correct && transaction.delistResource(xaResource, XAResource.TMSUCCESS);
			}

			if (correct)
			{
				conn.close();
			}

			if (correct)
			{
				tm.commit();
			}
			else
			{
				tm.rollback();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			correct = false;
		}

		_isCorrect = _isCorrect && correct;
		return;
	}

	public void begin_enlist_enlist_delist_commit()
			throws InvocationException
	{
		boolean correct = true;

		try
		{
			XAConnection xaConnection = _xaDataSource.getXAConnection(_databaseUser, _databasePassword);

			XAResource xaResource = xaConnection.getXAResource();
			Connection conn = xaConnection.getConnection();

			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			tm.begin();

			Transaction transaction = tm.getTransaction();

			correct = correct && transaction.enlistResource(xaResource);

			if (correct)
			{
				correct = correct && transaction.enlistResource(xaResource);
			}

			if (correct)
			{
				correct = correct && transaction.delistResource(xaResource, XAResource.TMSUCCESS);
			}

			if (correct)
			{
				tm.commit();
			}
			else
			{
				tm.rollback();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			correct = false;
		}

		_isCorrect = _isCorrect && correct;
		return;
	}
}