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
 * $Id: TransactionManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.utils.JNDIManager;

import javax.naming.InitialContext;

public class TransactionManager
{
    /**
	 * Retrieve a reference to the transaction manager from the passed in JNDI initial context.
	 * @param ctx The JNDI initial context to lookup the Transaction Manager reference from.
	 * @return The transaction manager bound to the appropriate JNDI context.  Returns null
     * if the transaction manager cannot be found.
	 *
	 * @message com.arjuna.ats.jta.TransactionManager.jndifailure Failed to lookup transaction manager in JNDI context
	 * @message com.arjuna.ats.jta.TransactionManager.generalfailure Failed to create instance of TransactionManager
	 */
    public static javax.transaction.TransactionManager transactionManager (InitialContext ctx)
    {
		javax.transaction.TransactionManager transactionManager = null;

		try
		{
			transactionManager = (javax.transaction.TransactionManager)ctx.lookup(JNDIManager.getTransactionManagerJNDIName());
		}
		catch (Exception e)
		{
			if ( jtaLogger.loggerI18N.isWarnEnabled() )
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.jta.TransactionManager.jndifailure", e);
			}
		}

		return transactionManager;
    }

    /**
     * Retrieve the singleton transaction manager reference.
     * @return The singleton transaction manager.  Can return null if the instantiation failed.
     */
	
    public synchronized static javax.transaction.TransactionManager transactionManager ()
    {
		return transactionManager(false);
    }

    /**
     * Retrieve the singleton transaction manager reference.
     * 
     * @param reset if <code>true</code> then ignore any previous cached implementation
     * and get a new instance.
     * @return The singleton transaction manager.  Can return null if the instantiation failed.
     */
	
    public synchronized static javax.transaction.TransactionManager transactionManager (boolean reset)
    {
        if ((_transactionManager == null ) || (reset))
        {
            try
            {
                _transactionManager = (javax.transaction.TransactionManager) Thread.currentThread().getContextClassLoader().loadClass(JNDIManager.getTransactionManagerImplementationClassname()).newInstance();
            }
            catch (Exception e)
            {
                if ( jtaLogger.loggerI18N.isErrorEnabled() )
                {
                    jtaLogger.loggerI18N.error("com.arjuna.ats.jta.TransactionManager.generalfailure", e);
                }
            }
        }

        return _transactionManager;
    }
	
    public static final void initialise (String[] args)
    {
    }

    private static javax.transaction.TransactionManager	_transactionManager = null;
    
}
