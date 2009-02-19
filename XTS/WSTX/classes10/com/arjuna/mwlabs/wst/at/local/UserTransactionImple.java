/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
/*
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserTransactionImple.java,v 1.16.4.1 2005/11/22 10:36:10 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at.local;

import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mwlabs.wst.at.context.TxContextImple;
import com.arjuna.mwlabs.wst.at.participants.CompletionCoordinatorImple;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;

/**
 * This is the interface that allows transactions to be started and terminated.
 * The messaging layer converts the Commit, Rollback and Notify messages into
 * calls on this.
 */

public class UserTransactionImple extends UserTransaction
{

	public UserTransactionImple ()
	{
		_completionCoordinator = new CompletionCoordinatorImple(
				_factory.coordinatorManager(), null, false);

		_contextManager = ContextManager.contextManager();
		_contextManager.initialise(_factory);
	}

	public void begin () throws WrongStateException, SystemException
	{
		begin(0);
	}

	public void begin (int timeout) throws WrongStateException, SystemException
	{
		try
		{
			if (_contextManager.currentTransaction() != null)
				throw new WrongStateException();

			final CoordinationContextType ctx = _factory.create(AtomicTransactionConstants.WSAT_PROTOCOL, null, null);

			TxContextImple theContext = new TxContextImple(ctx);

			_contextManager.resume(theContext);
		}
		catch (com.arjuna.wst.UnknownTransactionException ex)
		{
			ex.printStackTrace();

			throw new SystemException(ex.toString());
		}
		catch (com.arjuna.wsc.InvalidCreateParametersException ex)
		{
			throw new SystemException(ex.toString());
		}

		/*
		 * At this point we would normally register a completion participant.
		 * 
		 * Registrar reg =
		 * _registrarMapper.getRegistrar(_factory.coordinatorManager().identifier().toString());
		 * 
		 * reg.register(Protocols.Completion, null);
		 */
	}

    public void beginSubordinate()
        throws WrongStateException, SystemException
    {
        beginSubordinate(0);
    }

    public void beginSubordinate(final int timeout)
        throws WrongStateException, SystemException
    {
        throw new SystemException("com.arjuna.mwlabs.wst.at.local.UserTransactionImple  : beginSubordinate not implemented");
    }

	public void commit () throws TransactionRolledBackException,
			UnknownTransactionException, SystemException
	{
		try
		{
			_completionCoordinator.commit();
		}
		catch (TransactionRolledBackException ex)
		{
			throw ex;
		}
		catch (UnknownTransactionException ex)
		{
			throw ex;
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		finally
		{
			_contextManager.tidyup();
		}
	}

	public void rollback () throws UnknownTransactionException, SystemException
	{
		try
		{
			_completionCoordinator.rollback();
		}
		catch (UnknownTransactionException ex)
		{
			throw ex;
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		finally
		{
			_contextManager.tidyup();
		}
	}

	public String transactionIdentifier ()
	{
		try
		{
			return _factory.coordinatorManager().identifier().toString();
		}
		catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
		{
			return "NoTransaction";
		}
		catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
		{
			return "Unknown";
		}
	}

	public String toString ()
	{
		return transactionIdentifier();
	}

	static LocalContextFactoryImple _factory = new LocalContextFactoryImple();

	private final CompletionCoordinatorImple _completionCoordinator ;

	private ContextManager _contextManager = null;

}
