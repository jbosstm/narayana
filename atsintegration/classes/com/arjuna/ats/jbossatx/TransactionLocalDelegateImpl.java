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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.jboss.tm.TransactionLocal;
import org.jboss.tm.TransactionLocalDelegate;
import org.jboss.util.NestedRuntimeException;

/**
 * An implementation of the transaction local delegate using weak references.
 * @author kevin
 */
public class TransactionLocalDelegateImpl implements TransactionLocalDelegate
{
    /**
     * The transaction local map.
     */
    private Map transactionMap = Collections.synchronizedMap(new WeakHashMap()) ;
    
    /**
     * Does the specified transaction contain a value for the transaction local.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @return true if a value exists within the specified transaction, false otherwise.
     */
    public boolean containsValue(final TransactionLocal transactionLocal, final Transaction transaction)
    {
        final Map map = getMap(transaction, false) ;
        return ((map != null) && map.containsKey(transactionLocal)) ;
    }

    /**
     * Get value of the transaction local in the specified transaction.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @return The value of the transaction local.
     */
    public Object getValue(final TransactionLocal transactionLocal, final Transaction transaction)
    {
        final Map map = getMap(transaction, false) ;
        return (map == null ? null : map.get(transactionLocal)) ;
    }

    /**
     * Store the value of the transaction local in the specified transaction.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @param value The value of the transaction local.
     */
    public void storeValue(final TransactionLocal transactionLocal, final Transaction transaction,
            final Object value)
    {
        final int status ;
        try
        {
            status = transaction.getStatus() ;
        }
        catch (final SystemException se)
        {
            throw new NestedRuntimeException(se) ;
        }
        if (status == Status.STATUS_ACTIVE)
        {
            final Map map = getMap(transaction, true) ;
            map.put(transactionLocal, value) ;
        }
    }
    
    /**
     * Lock the transaction local in the context of this transaction.
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     * @throws IllegalStateException if the transaction is not active
     * @throws InterruptedException if the thread is interrupted
     */
    public void lock(final TransactionLocal transactionLocal, final Transaction transaction)
    		throws InterruptedException
	{
        final int status ;
        try
        {
            status = transaction.getStatus() ;
        }
        catch (final SystemException se)
        {
            throw new NestedRuntimeException(se) ;
        }
        if (status != Status.STATUS_ACTIVE)
        {
        		throw new IllegalStateException("Transaction not active") ;
        }
        final TransactionLocalSynchronization synchronization = getSynchronization(transaction, true) ;
        synchronization.lock(transactionLocal) ;
	}
    
    /**
     * Unlock the transaction local in the context of this transaction
     * @param transactionLocal The associated transaction local.
     * @param transaction The associated transaction.
     */
    public void unlock(final TransactionLocal transactionLocal, final Transaction transaction)
    {
        final TransactionLocalSynchronization synchronization = getSynchronization(transaction, true) ;
        if (synchronization != null)
        {
        	    synchronization.unlock(transactionLocal) ;
        }
    }
    
    /**
     * Get the value map for the transaction.
     * @param transaction The transaction.
     * @param create true if the transaction information should be created, false otherwise.
     * @return The value map or null.
     */
    private Map getMap(final Transaction transaction, final boolean create)
    {
    	   final TransactionLocalSynchronization synchronization = getSynchronization(transaction, create) ;
    	   return (synchronization == null ? null : synchronization.getMap()) ;
    }
    
    /**
     * Get the synchronization for the transaction.
     * @param transaction The transaction.
     * @param create true if the transaction information should be created, false otherwise.
     * @return The synchronization or null.
     */
    private TransactionLocalSynchronization getSynchronization(final Transaction transaction, final boolean create)
    {
       final WeakReference reference = (WeakReference)transactionMap.get(transaction) ;
       if (reference != null)
       {
           final TransactionLocalSynchronization synchronization = (TransactionLocalSynchronization)reference.get() ;
           if (synchronization != null)
           {
               return synchronization ;
           }
       }
       
       if (!create)
       {
           return null ;
       }
       
       final TransactionLocalSynchronization synchronization = new TransactionLocalSynchronization() ;
       try
       {
           transaction.registerSynchronization(synchronization) ;
       }
       catch (final Exception ex)
       {
           throw new NestedRuntimeException(ex) ;
       }
       transactionMap.put(transaction, new WeakReference(synchronization)) ;
       return synchronization ;
    }
    
   /**
    * 
    * @author kevin
    *
    */
    private static class TransactionLocalSynchronization implements Synchronization
    {
        /**
         * The values map.
         */
        private Map map = Collections.synchronizedMap(new HashMap()) ;
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
         * Get the values map.
         * @return The values map.
         */
        public Map getMap()
        {
            return map ;
        }
        
        /**
         * After completion event.
         */
        public void afterCompletion(final int result)
        {
            // Do nothing
        }
        
        /**
         * before completion event.
         */
        public void beforeCompletion()
        {
            // Do nothing
        }
        
        /**
         * Lock the transaction local within the curren thread context.
         * @param transactionLocal The transaction local.
         */
        public void lock(final TransactionLocal transactionLocal)
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
         * @param transactionLocal The transaction local.
         */
        public void unlock(final TransactionLocal transactionLocal)
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
