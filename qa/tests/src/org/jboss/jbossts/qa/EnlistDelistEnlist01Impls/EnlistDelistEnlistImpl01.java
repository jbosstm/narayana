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
// $Id: EnlistDelistEnlistImpl01.java,v 1.4 2004/02/24 11:06:08 rbegg Exp $
//

package org.jboss.jbossts.qa.EnlistDelistEnlist01Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: EnlistDelistEnlistImpl01.java,v 1.4 2004/02/24 11:06:08 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: EnlistDelistEnlistImpl01.java,v 1.4 2004/02/24 11:06:08 rbegg Exp $
 */


import org.jboss.jbossts.qa.EnlistDelistEnlist01.*;

import javax.naming.InitialContext;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.NotSupportedException;
import javax.transaction.Transaction;
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

			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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

			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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

			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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

			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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
