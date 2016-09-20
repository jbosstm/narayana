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
package com.arjuna.ats.jbossatx;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

import org.jboss.tm.listener.TransactionTypeNotSupported;
import org.jboss.tm.TransactionLocal;
import org.jboss.tm.TransactionLocalDelegate;
import org.jboss.tm.TransactionTimeoutConfiguration;

import org.jboss.tm.listener.TransactionEvent;
import org.jboss.tm.listener.TransactionListener;
import org.jboss.tm.listener.TransactionListenerRegistry;
import org.jboss.tm.listener.EventType;

import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Delegate for JBoss TransactionManager/TransactionLocalDelegate.
 * @author kevin
 */
public abstract class BaseTransactionManagerDelegate implements TransactionManager, TransactionLocalDelegate, TransactionTimeoutConfiguration, TransactionListenerRegistry
{
    private static final String LISTENER_MAP_KEY = "__TX_LISTENERS";

    /**
     * Delegate transaction manager.
     */
    private final TransactionManager transactionManager ;

    /**
     * Construct the delegate using the specified transaction manager.
     * @param transactionManager The delegate transaction manager.
     */
    protected BaseTransactionManagerDelegate(final TransactionManager transactionManager)
    {
        this.transactionManager = transactionManager ;
    }

    /**
     * Begin a transaction and associate it with the current thread.
     */
    public void begin()
        throws NotSupportedException, SystemException
    {
        transactionManager.begin() ;
    }

    /**
     * Commit the current transaction and disassociate from the thread.
     */
    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
        SecurityException, IllegalStateException, SystemException
    {
        notifyAssociationListeners(getTransaction(), EnumSet.of(EventType.DISASSOCIATING));
        transactionManager.commit() ;
    }

    /**
     * Get the transaction status.
     * @return the transaction status.
     */
    public int getStatus()
        throws SystemException
    {
        return transactionManager.getStatus() ;
    }

    /**
     * Get the transaction associated with the thread.
     * @return the transaction or null if none associated.
     */
    public Transaction getTransaction()
        throws SystemException
    {
        return transactionManager.getTransaction() ;
    }

    /**
     * Resume the specified transaction.
     * @param transaction The transaction to resume.
     */
    public void resume(final Transaction transaction)
        throws InvalidTransactionException, IllegalStateException, SystemException
    {
        if (transaction == null) {
            suspend(); // This is what AtomicAction does
        } else {
            transactionManager.resume(transaction) ;
            notifyAssociationListeners(transaction, EnumSet.of(EventType.ASSOCIATED));
        }
    }

    /**
     * Rollback the current transaction and disassociate from the thread.
     */
    public void rollback()
        throws IllegalStateException, SecurityException, SystemException
    {
        notifyAssociationListeners(getTransaction(), EnumSet.of(EventType.DISASSOCIATING));
        transactionManager.rollback() ;
    }

    /**
     * Set rollback only on the current transaction.
     */
    public void setRollbackOnly()
        throws IllegalStateException, SystemException
    {
        transactionManager.setRollbackOnly() ;
    }

    /**
     * Set the transaction timeout on the current thread.
     * @param timeout The transaction timeout.
     */
    public void setTransactionTimeout(final int timeout)
        throws SystemException
    {
        transactionManager.setTransactionTimeout(timeout) ;
    }

    /**
     * register a listener for transaction related events that effect the current thread
     * @param listener the callback for event notifications
     */
    @Override
    public void addListener (Transaction transaction, TransactionListener listener, EnumSet<EventType> types) throws TransactionTypeNotSupported
    {
        if (transaction == null)
            throw new NullPointerException(); // we could interpret this as meaning register for all transactions

        if (!(transaction instanceof com.arjuna.ats.jta.transaction.Transaction))
            throw new TransactionTypeNotSupported("Unsupported transaction type");

        Collection<TransactionListener> listeners = getListeners(transaction, true);

        if (listeners != null) {
            listeners.add(listener);

            // if transaction is already associated with the current thread notify this listener
            try {
                if (transaction.equals(getTransaction()) && types.contains(EventType.ASSOCIATED))
                    listener.onEvent(new TransactionEvent(transaction, EnumSet.of(EventType.ASSOCIATED)));
            } catch (SystemException e) {
                // no transaction associated so do not trigger the ASSOCIATED callback
            }
        }
    }

    /**
     * Suspend the current transaction.
     * @return The suspended transaction.
     */
    public Transaction suspend()
        throws SystemException
    {
        if (getStatus() != Status.STATUS_NO_TRANSACTION)
            notifyAssociationListeners(getTransaction(), EnumSet.of(EventType.DISASSOCIATING));

        return transactionManager.suspend();
    }

    // TransactionListener implementation methods.
    // return all the event listeners associated with this thread
    private Collection<TransactionListener> getListeners(Transaction transaction, boolean create)
    {
        com.arjuna.ats.jta.transaction.Transaction txn = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        Object resource;

        // protect against two concurrent listener registrations both trying to create the initial resource entry
        synchronized (transaction) {
            resource = txn.getTxLocalResource(LISTENER_MAP_KEY);

            if (resource == null && create) {
                Collection<TransactionListener> listeners = new ConcurrentLinkedQueue<>();

                txn.putTxLocalResource(LISTENER_MAP_KEY, listeners);

                return listeners;
            }
        }

        if (resource != null && !(resource instanceof ConcurrentLinkedQueue)) {
            // another container subsystem has inadvertently used our key
            throw new IllegalStateException("Invalid transaction local resource associated with key");
        }

        return (Collection<TransactionListener>) resource;
    }

    // notify any listeners for this transaction that there has been an event
    private void notifyAssociationListeners(Transaction transaction, EnumSet<EventType> reasons)
    {
        if (transaction != null) {
            Collection<TransactionListener> listeners = getListeners(transaction, false);
            TransactionEvent event = new TransactionEvent(transaction, reasons);

            if (listeners != null) {
                for (TransactionListener s : listeners)
                    s.onEvent(event);
            }
        }
    }

    /////////////////////////

    // TransactionLocalDelegate implementation methods. This part is basically
    // stateless, we just delegate down to the object storage on the TransactionImple

    // Note this has some interesting effects around Transaction termination. The TransactionImple instance
    // lifetime is up to tx termination only. After that getTransaction() on the tm returns a new instance
    // representing the same tx. Hence TransactionLocal state goes away magically at tx termination time
    // since it's part of the TransactionImple instance. That's what we want and saves writing cleanup code.
    // On the down side, since locks use the same storage they go away too. This causes us to have to
    // jump through some hoops to deal with locks vanishing and maybe never getting unlocked, see below.

    /**
     * Does the specified transaction contain a value for the transaction local.
     *
     * @param transactionLocal The associated transaction local.
     * @param transaction      The associated transaction.
     * @return true if a value exists within the specified transaction, false otherwise.
     */
    public boolean containsValue(final TransactionLocal transactionLocal, final Transaction transaction) {
        com.arjuna.ats.jta.transaction.Transaction transactionImple = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        if(transactionImple.isAlive()) {
            return (transactionImple.getTxLocalResource(transactionLocal) != null ? true : false);
        } else {
            return false;
        }
    }

    /**
     * Get value of the transaction local in the specified transaction.
     *
     * @param transactionLocal The associated transaction local.
     * @param transaction      The associated transaction.
     * @return The value of the transaction local.
     */
    public Object getValue(final TransactionLocal transactionLocal, final Transaction transaction) {
        com.arjuna.ats.jta.transaction.Transaction transactionImple = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        if(transactionImple.isAlive()) {
            return transactionImple.getTxLocalResource(transactionLocal);
        } else {
            return null;
        }
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
        com.arjuna.ats.jta.transaction.Transaction transactionImple = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        if(transactionImple.isAlive()) {
            transactionImple.putTxLocalResource(transactionLocal, value);
        } else {
            throw new IllegalStateException("Can't store value in a TransactionLocal after the Transaction has ended");
        }
    }

    /**
     * Lock the transaction local in the context of this transaction.
     *
     * @throws IllegalStateException if the transaction is not active
     * @throws InterruptedException  if the thread is interrupted
     */
    public void lock(final TransactionLocal local, final Transaction transaction)
            throws InterruptedException {
        com.arjuna.ats.jta.transaction.Transaction transactionImple = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        if(transactionImple.isAlive()) {
            // There is a race here, as the transaction may terminate between the check
            // and the lock attempt. See lock() implementation below.
            // It's still possible to lock a dead transaction, but that does no real harm.
            TransactionLocalLock lock = findLock(local, transaction);
            if(lock.lock(transactionImple)) {
                return;
            }
        }

        throw new IllegalStateException("Can't lock a TransactionLocal after the Transaction has ended");
    }

    /**
     * Unlock the transaction local in the context of this transaction
     */
    public void unlock(final TransactionLocal local, final Transaction transaction) {
        TransactionLocalLock lock = findLock(local, transaction);
        lock.unlock();
    }

    // Lock implementation: This used to use a Synchronization for lock storage, but
    // we need to be able to lock things in some states where registration of
    // Synchronizations is not permitted. Besides, the JTA 1.1 work gives us a nice
    // general object storage mechanism on a TransactionImple, so we use that.

    // This is the key under which the map of TransactionLocals to locks is stored.
    // Bad things will probably happen if users ever use this key themselves
    private final String LOCKS_MAP = "__LOCKS_MAP";

    // Using string LOCKS_MAP directly as object of synchronization is not recommended
    // String literals are centrally interned and could also be locked on by a library,
    // potentially having deadlocks or lock collisions
    private static final Object locksMapLock = new Object();

    // locate and return the lock for a given TransactionLocal+Transaction tuple.
    // create it if it does not exist.
    private TransactionLocalLock findLock(final TransactionLocal local, final Transaction transaction) {

        com.arjuna.ats.jta.transaction.Transaction transactionImple = (com.arjuna.ats.jta.transaction.Transaction) transaction;
        Map locks; // <TransactionLocal, TransactionLocalLock>
        // ideally for performance we should sync on the tx instance itself but that may have nasty
        // side effects so we use something else as the lock object for the sync block
        locks = (Map) transactionImple.getTxLocalResource(LOCKS_MAP);
        // this is not a double-check locking anti-pattern, because locks
        // is a local variable and thus can not leak.
        if (locks == null) {
            synchronized (locksMapLock) {
                // ensure there is a holder for lock storage on the given tx instance.
                locks = (Map) transactionImple.getTxLocalResource(LOCKS_MAP);
                if (locks == null) {
                    locks = new HashMap(); // <TransactionLocal, TransactionLocalLock>
                    transactionImple.putTxLocalResource(LOCKS_MAP, locks);
                }
            }
        }

        TransactionLocalLock transactionLocalLock = (TransactionLocalLock) locks.get(local);
        if (transactionLocalLock == null) {
            synchronized (locks) {
                // ensure there is a lock for the specified local+tx tuple
                transactionLocalLock = (TransactionLocalLock)locks.get(local);
                if (transactionLocalLock == null) {
                    transactionLocalLock = new TransactionLocalLock();
                    locks.put(local, transactionLocalLock);
                }
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
         * Lock the transaction local within the current thread context.
         * true on lock acquired, false otherwise
         */
        public boolean lock(com.arjuna.ats.jta.transaction.Transaction tx)
        {
            // The current code in the app server locks the transaction for all, we follow that practice
            synchronized(lock)
            {
                final Thread currentThread = Thread.currentThread() ;
                if (currentThread == lockingThread)
                {
                    lockCount++ ;
                    return true;
                }

                while (lockingThread != null)
                {
                    try
                    {
                        // lock object instances get thrown away at Transaction termination. That makes it impossible
                        // to call unlock() on them. Searching through them and unlocking them from the transaction
                        // termination code is a pain and finalizers suck.
                        // Hence we need to make sure we don't wait forever for a notify
                        // that will never come. Probably the only thing that will terminate a Transaction in another
                        // Thread is the transaction reaper, so we wait not longer than the tx timeout plus a fudge factor.
                        long timeout = 0;
                        try {
                            timeout = getTransactionTimeout();
                        } catch(SystemException e) {}

                        lock.wait(timeout+1000);
                        if(!tx.isAlive()) {
                            // transaction is dead, can't be locked, cleanup
                            lockingThread = null;
                            lockCount = 0;
                            return false;
                        }
                    }
                    catch (final InterruptedException ie) {}
                }

                lockingThread = currentThread ;
                lockCount ++ ;
                return true;
            }
        }

        /**
         * Unlock the transaction local within the current thread context.
         */
        public void unlock()
        {
            synchronized(lock)
            {
                if(lockCount == 0 && lockingThread == null) {
                    // the lock was probably reset by a transaction termination.
                    // we fail silent to save the caller having to deal with race condition.
                    return;
                }

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
