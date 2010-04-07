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
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import javax.naming.InitialContext;

public class UserTransaction
{

	/**
	 * Retrieve a reference to the user transaction.
	 * 
	 * @return The user transaction bound to the appropriate JNDI context
	 * 
	 * @message com.arjuna.ats.jta.UserTransaction.jndifailure Failed to lookup
	 *          user transaction in JNDI context
	 * @message com.arjuna.ats.jta.UserTransaction.generalfailure Failed to
	 *          create instance of UserTransaction
	 */

	public static synchronized javax.transaction.UserTransaction userTransaction (InitialContext ctx)
	{
		javax.transaction.UserTransaction userTransaction = null;

		try
		{
			userTransaction = (javax.transaction.UserTransaction) ctx.lookup(jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionJNDIContext());
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.jta.UserTransaction.jndifailure", e);
			}
		}

		return userTransaction;
	}

	/**
	 * Retrieve the singleton UserTransaction reference.
	 * 
	 * @return The singleton UserTransaction reference. Can return null if the
	 *         instantiation failed.
	 */
	
	public static synchronized javax.transaction.UserTransaction userTransaction ()
	{
		return jtaPropertyManager.getJTAEnvironmentBean().getUserTransaction();
	}
}
