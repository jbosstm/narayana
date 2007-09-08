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
import java.util.Map;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jbossatx.BaseTransactionManagerDelegate;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;

import org.jboss.tm.TransactionLocal;

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
     * @return the remaining in the current transaction or -1
     * if there is no transaction
     * @throws RollbackException if the transaction is marked for rollback and
     * errorRollback is true
     *
     * @message com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1
     * 		[com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1] - Transaction rolledback
     * @message com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2
     * 		[com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2] - Unexpected error retrieving transaction status
     */
    public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback)
        throws RollbackException
    {
    	try
    	{
	    	if (getStatus() == Status.STATUS_MARKED_ROLLBACK)
	    	{
	    		throw new RollbackException(jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_1")) ;
			}
    	}
    	catch (final SystemException se)
    	{
    		throw new RollbackException(jbossatxLogger.logMesg.getString("com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate.getTimeLeftBeforeTransactionTimeout_2")) ;
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


    /////////////////////////

    // TransactionLocalDelegate implementation methods. This part is basically
    // stateless, we just delegate down to the object storage on the TransactionImple

    /**
     * Does the specified transaction contain a value for the transaction local.
     *
     * @param transactionLocal The associated transaction local.
     * @param transaction      The associated transaction.
     * @return true if a value exists within the specified transaction, false otherwise.
     */
    public boolean containsValue(final TransactionLocal transactionLocal, final Transaction transaction) {
        TransactionImple transactionImple = (TransactionImple) transaction;
        return (transactionImple.getTxLocalResource(transactionLocal) != null ? true : false);
    }

    /**
     * Get value of the transaction local in the specified transaction.
     *
     * @param transactionLocal The associated transaction local.
     * @param transaction      The associated transaction.
     * @return The value of the transaction local.
     */
    public Object getValue(final TransactionLocal transactionLocal, final Transaction transaction) {
        TransactionImple transactionImple = (TransactionImple) transaction;
        return transactionImple.getTxLocalResource(transactionLocal);
    }

    /**
     * Store the value of the transaction local in the specified transaction.
     *
     * @param transactionLocal The associated transaction local.
     * @param transaction      The associated transaction.
     * @param value            The value of the transaction local.
     */
    public void storeValue(final TransactionLocal transactionLocal, final Transaction transaction,
                           final Object value) {
        TransactionImple transactionImple = (TransactionImple) transaction;
        transactionImple.putTxLocalResource(transactionLocal, value);
    }

    /**
     * Lock the transaction local in the context of this transaction.
     *
     * @throws IllegalStateException if the transaction is not active
     * @throws InterruptedException  if the thread is interrupted
     */
    public void lock(final TransactionLocal local, final Transaction transaction)
            throws InterruptedException {
        TransactionLocalLock lock = findLock(local, transaction);
        lock.lock();
    }

    /**
     * Unlock the transaction local in the context of this transaction
     */
    public void unlock(final TransactionLocal local, final Transaction transaction) {
        TransactionLocalLock lock = findLock(local, transaction);
        lock.unlock();
    }

    // Lock implementaion: This used to use a Synchronization for lock storage, but
    // we need to be able to create locks after transactions end, at which point
    // registration of Synchronizations is no longer permitted. Hence we now
    // store locks in the general object storage Map on the TransactionImple, using this
    // as the key under which the map of TransactionLocals to locks is stored.
    // Bad things will probably happen if users ever use this key themselves
    private final String LOCKS_MAP = "__LOCKS_MAP";

    // locate and return the lock for a given TransactionLocal+Transaction tuple.
    // create it if it does not exist.
    private TransactionLocalLock findLock(final TransactionLocal local, final Transaction transaction) {

        TransactionImple transactionImple = (TransactionImple) transaction;
        Map<TransactionLocal, TransactionLocalLock> locks;
        // ideally for performance we should sync on the tx instance itself but that may have nasty
        // side effects so we use something else as the lock object for the sync block
        synchronized (LOCKS_MAP) {
            // ensure there is a holder for lock storage on the given tx instance.
            locks = (Map) transactionImple.getTxLocalResource(LOCKS_MAP);
            if (locks == null) {
                locks = new HashMap<TransactionLocal, TransactionLocalLock>();
                transactionImple.putTxLocalResource(LOCKS_MAP, locks);
            }
        }

        TransactionLocalLock transactionLocalLock;
        synchronized (locks) {
            // ensure there is a lock for the specified local+tx tuple
            transactionLocalLock = locks.get(local);
            if (transactionLocalLock == null) {
                transactionLocalLock = new TransactionLocalLock();
                locks.put(local, transactionLocalLock);
            }
        }

        return transactionLocalLock;
    }

    // A class for the storage of individual lock state:

    private class TransactionLocalLock
    {
        /**
         * The locking thread.
         */
        private Thread lockingThread ;
        /**
         * The lock count.
         */
        private int lockCount ;
        /**
         * The lock.
         */
        private byte[] lock = new byte[0] ;

        /**
         * Lock the transaction local within the curren thread context.
         */
        public void lock()
        {
        	    // The current code in the app server locks the transaction for all, we follow that practice
        	    synchronized(lock)
        	    {
                final Thread currentThread = Thread.currentThread() ;
                if (currentThread == lockingThread)
                {
                    lockCount++ ;
                    return ;
                }

                while (lockingThread != null)
                {
                    try
                    {
                        lock.wait();
                    }
                    catch (final InterruptedException ie) {}
                }

                lockingThread = currentThread ;
                lockCount ++ ;
        	    }
        }

        /**
         * Unlock the transaction local within the curren thread context.
         */
        public void unlock()
        {
        	    synchronized(lock)
        	    {
                final Thread currentThread = Thread.currentThread() ;
                if (currentThread != lockingThread)
                {
                    throw new IllegalStateException("Unlock called from wrong thread.  Locking thread: " + lockingThread +
                        ", current thread: " + currentThread) ;
                }

                if (--lockCount == 0)
                {
                    lockingThread = null ;
                    lock.notify() ;
                }
            }
        }
    }
}
