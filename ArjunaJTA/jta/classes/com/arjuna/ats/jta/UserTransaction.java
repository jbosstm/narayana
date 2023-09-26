/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta;

import javax.naming.InitialContext;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

public class UserTransaction
{

	/**
	 * Retrieve a reference to the user transaction.
	 * 
	 * @return The user transaction bound to the appropriate JNDI context
	 */

	public static synchronized jakarta.transaction.UserTransaction userTransaction (InitialContext ctx)
	{
		jakarta.transaction.UserTransaction userTransaction = null;

		try
		{
			userTransaction = (jakarta.transaction.UserTransaction) ctx.lookup(jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionJNDIContext());
		}
		catch (Exception e)
		{
            jtaLogger.i18NLogger.warn_UserTransaction_jndifailure(e);
		}

		return userTransaction;
	}

	/**
	 * Retrieve the singleton UserTransaction reference.
	 * 
	 * @return The singleton UserTransaction reference. Can return null if the
	 *         instantiation failed.
	 */
	
	public static synchronized jakarta.transaction.UserTransaction userTransaction ()
	{
		return jtaPropertyManager.getJTAEnvironmentBean().getUserTransaction();
	}
}