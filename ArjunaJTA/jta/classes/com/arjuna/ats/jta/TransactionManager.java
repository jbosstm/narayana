/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta;

import javax.naming.InitialContext;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

public class TransactionManager
{
    /**
	 * Retrieve a reference to the transaction manager from the passed in JNDI initial context.
	 * @param ctx The JNDI initial context to lookup the Transaction Manager reference from.
	 * @return The transaction manager bound to the appropriate JNDI context.  Returns null
     * if the transaction manager cannot be found.
     * 
	 */
    public static jakarta.transaction.TransactionManager transactionManager (InitialContext ctx)
    {
		jakarta.transaction.TransactionManager transactionManager = null;

		try
		{
			transactionManager = (jakarta.transaction.TransactionManager)ctx.lookup(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
		}
		catch (Exception e)
		{
            jtaLogger.i18NLogger.warn_TransactionManager_jndifailure(e);
		}

		return transactionManager;
    }

    /**
     * Retrieve the singleton transaction manager reference.
     * @return The singleton transaction manager.  Can return null if the instantiation failed.
     */
	
    public static jakarta.transaction.TransactionManager transactionManager ()
    {
		return jtaPropertyManager.getJTAEnvironmentBean().getTransactionManager();
    }
}