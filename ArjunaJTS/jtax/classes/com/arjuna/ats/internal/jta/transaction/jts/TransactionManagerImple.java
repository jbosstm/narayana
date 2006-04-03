/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts;

import org.omg.CosTransactions.*;

import com.arjuna.ats.jta.logging.*;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jta.utils.LoggerSetup;

import javax.transaction.*;

import com.arjuna.common.util.logging.*;

import javax.naming.*;
import java.lang.NullPointerException;
import java.util.Hashtable;

/**
 * An implementation of javax.transaction.TransactionManager.
 * 
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TransactionManagerImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class TransactionManagerImple extends BaseTransaction implements
		javax.transaction.TransactionManager, javax.naming.spi.ObjectFactory
{

	public TransactionManagerImple ()
	{
	}

	public Transaction getTransaction ()
			throws javax.transaction.SystemException
	{
		try
		{
			return TransactionImple.getTransaction();
		}
		catch (NullPointerException ex)
		{
			return null;
		}
		catch (Exception e)
		{
			throw new javax.transaction.SystemException();
		}
	}

	/**
	 * @return the suspended transaction.
	 */

	public Transaction suspend () throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionManagerImple.suspend");
		}

		try
		{
			TransactionImple tx = TransactionImple.getTransaction();
			Control theControl = OTSManager.get_current().suspend();

			return tx;
		}
		catch (Exception e)
		{
			throw new javax.transaction.SystemException();
		}
	}

	/**
	 * Unlike the OTS, if we are already associated with a transaction then we
	 * cannot call resume.
	 */

	public void resume (Transaction which) throws InvalidTransactionException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionManagerImple.resume");
		}

		super.checkTransactionState();

		if (which instanceof TransactionImple)
		{
			TransactionImple theTransaction = (TransactionImple) which;

			try
			{
				ControlWrapper cont = theTransaction.getControlWrapper();

				OTSImpleManager.current().resumeWrapper(cont);

				cont = null;
				theTransaction = null;
			}
			catch (org.omg.CosTransactions.InvalidControl e1)
			{
				//throw new InvalidTransactionException();
			}
			catch (org.omg.CORBA.SystemException e2)
			{
				throw new javax.transaction.SystemException();
			}
		}
		else
			throw new InvalidTransactionException();
	}

	/**
	 * Creates a TransactionManageImple from the given information.
	 * 
	 * @param obj
	 * @param name
	 * @param nameCtx
	 * @param environment
	 * @return Object
	 * @throws Exception
	 */
	public Object getObjectInstance (Object obj, Name name, Context nameCtx, Hashtable environment)
			throws Exception
	{
		return this;
	}

	static
	{
		LoggerSetup.setup();
	}
}
