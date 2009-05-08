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
package com.arjuna.ats.jbossatx.jta;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.jbossatx.BaseTransactionManagerDelegate;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;

public class TransactionManagerDelegate extends BaseTransactionManagerDelegate implements ObjectFactory
{
    /**
     * The transaction manager.
     */
    private static final TransactionManagerImple TRANSACTION_MANAGER = new TransactionManagerImple() ;

    /**
     * Construct the delegate with the appropriate transaction manager
     */
    public TransactionManagerDelegate()
    {
        super(getTransactionManager());
    }

    /**
     * Get the transaction timeout.
     *
     * @return the timeout in seconds associated with this thread
     * @throws SystemException for any error
     */
    public int getTransactionTimeout()
        throws SystemException
    {
        return getTransactionManager().getTimeout() ;
    }

    /**
     * Get the time left before transaction timeout
     *
     * @param errorRollback throw an error if the transaction is marked for rollback
     * @return the time (in ms) remaining in the current transaction or -1
     * if there is no transaction
     * @throws RollbackException if the transaction is marked for rollback and
     * errorRollback is true
     *
     * @message com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1
     * 		[com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1] - Transaction has or will rollback.
     * @message com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2
     * 		[com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2] - Unexpected error retrieving transaction status
     */
    public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback)
        throws RollbackException
    {
        // see JBAS-5081, JBTM-371 and http://www.jboss.com/index.html?module=bb&op=viewtopic&t=132128

        try
    	{
            switch(getStatus())
            {
                case Status.STATUS_MARKED_ROLLBACK:
                case Status.STATUS_ROLLEDBACK:
                case Status.STATUS_ROLLING_BACK:
                    if(errorRollback) {
                        throw new RollbackException(jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1"));
                    }
                    break;
                case Status.STATUS_COMMITTED:
                case Status.STATUS_COMMITTING:
                case Status.STATUS_UNKNOWN:
                    throw new IllegalStateException();  // would be better to use a checked exception,
                    // but RollbackException does not make sense and the API does not allow any other.
                    // also need to clarify if we should throw an exception at all if !errorRollback?
                case Status.STATUS_ACTIVE:
                case Status.STATUS_PREPARED:
                case Status.STATUS_PREPARING:
                    com.arjuna.ats.jta.transaction.Transaction tx = (com.arjuna.ats.jta.transaction.Transaction)getTransaction();
                    if(tx != null) {
                            return tx.getRemainingTimeoutMills();
                    } else {
                        return 0;
                    }
                case Status.STATUS_NO_TRANSACTION:
                default:
                    break;
            }
    	}
    	catch (final SystemException se)
    	{
            RollbackException rollbackException = new RollbackException(
                    jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2")) ;
            rollbackException.initCause(se);
            throw rollbackException;
    	}
        return -1 ;
    }

    /**
     * Get the transaction manager from the factory.
     * @param initObj The initialisation object.
     * @param relativeName The instance name relative to the context.
     * @param namingContext The naming context for the instance.
     * @param env The environment.
     */
    public Object getObjectInstance(final Object initObj,
           final Name relativeName, final Context namingContext,
           final Hashtable env)
        throws Exception
    {
        return this ;
    }

    /**
     * Get the transaction manager.
     * @return The transaction manager.
     */
    private static TransactionManagerImple getTransactionManager()
    {
        return TRANSACTION_MANAGER ;
    }
}
