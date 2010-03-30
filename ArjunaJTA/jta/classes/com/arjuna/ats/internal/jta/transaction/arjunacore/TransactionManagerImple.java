/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
/*
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 *
 * $Id: TransactionManagerImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore;

import com.arjuna.ats.jta.logging.*;
import com.arjuna.ats.arjuna.AtomicAction;

import com.arjuna.common.util.logging.*;

import java.util.Hashtable;

import javax.transaction.*;
import javax.naming.*;

public class TransactionManagerImple extends BaseTransaction implements
		javax.transaction.TransactionManager, javax.naming.spi.ObjectFactory
{

	public TransactionManagerImple()
	{
	}

	public Transaction getTransaction()
			throws javax.transaction.SystemException
	{
		return TransactionImple.getTransaction();
	}

	/**
	 * @return the suspended transaction.
	 */

	public Transaction suspend() throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("TransactionImpleManager.suspend");
        }

		try
		{
			TransactionImple tx = TransactionImple.getTransaction();

			if (tx != null)
			{
				tx.getAtomicAction().suspend();
			}

			return tx;
		}
		catch (Exception e)
		{
            javax.transaction.SystemException systemException = new javax.transaction.SystemException(e.toString());
            systemException.initCause(e);
            throw systemException;
		}
	}

	/**
	 * Unlike the OTS, if we are already associated with a transaction then we
	 * cannot call resume.
	 */

	public void resume(Transaction which) throws InvalidTransactionException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("TransactionImpleManager.resume");
        }

		super.checkTransactionState();

		/*
		 * If we are here then there is no transaction associated with the
		 * thread.
		 */

		if ((which == null) || (which instanceof TransactionImple))
		{
		    TransactionImple theTransaction = (TransactionImple) which;

		    try
		    {
		        AtomicAction act = ((theTransaction == null) ? null : theTransaction.getAtomicAction());

		        if (!AtomicAction.resume(act))
		            throw new InvalidTransactionException();

		        theTransaction = null;
		    }
		    catch (final Exception e2)
		    {
		        javax.transaction.SystemException systemException = new javax.transaction.SystemException();
                systemException.initCause(e2);
                throw systemException;
		    }
		}
		else
		    throw new InvalidTransactionException("Illegal type is: "
		            + which);
	}

	/**
	 * Creates a TransactionManageImple from the given information.
	 *
	 * @param obj
	 * @param name
	 * @param nameCtx
	 * @param environment
	 * @return
	 * @throws Exception
	 */
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable environment) throws Exception
	{
		return this;
	}
}
