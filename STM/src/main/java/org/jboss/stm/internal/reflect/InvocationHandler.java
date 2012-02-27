/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * (C) 2009,
 * @author mark.little@jboss.com
 */

package org.jboss.stm.internal.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.jboss.stm.LockException;
import org.jboss.stm.TransactionException;
import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Retry;
import org.jboss.stm.annotations.Timeout;
import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Nested;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.proxy.LockManagerProxy;
import org.jboss.stm.internal.proxy.OptimisticLockManagerProxy;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class InvocationHandler<T> implements java.lang.reflect.InvocationHandler
{
    /*
     * If no locks are defined in annotations then we try to use the
     * names of the methods to infer a lock type to use.
     * 
     * todo do we need a "disable inference" annotation/rule? Maybe not since you
     * can always either explicitly define the lock or change the method name!
     */
    
    private static final String GETTER_NAME = "GET";
    private static final String SETTER_NAME = "SET";
    
    class LockInformation
    {
        public LockInformation (int lockType)
        {
            this(lockType, LockManager.defaultSleepTime, LockManager.defaultRetry);
        }
        
        public LockInformation (int lockType, int timeout, int retry)
        {
            _lockType = lockType;
            _timeout = timeout;
            _retry = retry;
        }
        
        public String toString ()
        {
            return "Lock < "+LockMode.stringForm(_lockType)+", "+_timeout+", "+_retry+" >";
        }
        
        public int _lockType;
        public int _timeout;
        public int _retry;
    }
    
    public InvocationHandler (RecoverableContainer<T> c, T obj)
    {
        this(c, obj, ObjectType.RECOVERABLE);
    }
    
    public InvocationHandler (RecoverableContainer<T> c, T obj, int ot)
    {
        this(c, obj, ot, null);
    }
    
    public InvocationHandler (RecoverableContainer<T> c, T obj, Uid u)
    {
        this(c, obj, ObjectType.ANDPERSISTENT, u);
    }
    
    public InvocationHandler (RecoverableContainer<T> cont, T obj, int ot, Uid u)
    {
        _container = cont;
        _theObject = obj;
               
        /*
         * Do we need to use the optimistic LockManager instance?
         */
        
        Class<?> c = obj.getClass().getSuperclass();
        
        while (c != null)
        {
            /*
             * Default is pessimistic.
             */
            
            if (c.getAnnotation(Optimistic.class) != null)
            {
                _optimistic = true;
                
                break;
            }

            c = c.getSuperclass();
        }
        
        if (!_optimistic)
        {
            Class<?>[] interfaces = obj.getClass().getInterfaces();
    
            for (Class<?> i : interfaces)
            {
                if (i.getAnnotation(Optimistic.class) != null)
                {
                    _optimistic = true;
                    
                    break;
                }
            }
        }

        if (_optimistic)
        {
            if (!initialiseStore())
            {
                _txObject = null;
                
                return;
            }
        }
        
        if (u != null)
        {
            if (_optimistic)
            {
                _txObject = new OptimisticLockManagerProxy<T>(obj, u, ObjectModel.MULTIPLE, cont);
            }
            else
                _txObject = new LockManagerProxy<T>(obj, u, cont);
        }
        else
        {
            /*
             * todo currently we optimistic always uses persistent objects, but with an in-memory object store. This
             * is to ensure that concurrent threads do not interfere with each others current state view. If the application
             * is written in such a way that a single instance can be used and still be thread-safe, then we could go
             * back to using RECOVERABLE. Make it an annotation option?
             */
            
            if (_optimistic)
            {
                _txObject = new OptimisticLockManagerProxy<T>(obj, ObjectType.ANDPERSISTENT, ObjectModel.MULTIPLE, cont);  // recoverable or persistent
            }
            else
                _txObject = new LockManagerProxy<T>(obj, ot, cont);  // recoverable or persistent
        }
        
        _methods = obj.getClass().getDeclaredMethods();
        
        /*
         * Do we need to create (sub-) transactions when each method
         * is called?
         */       
        
        c = obj.getClass().getSuperclass();
        
        while (c != null)
        {
            if (c.getAnnotation(Nested.class) != null)
            {
                _nestedTransactions = true;
                
                break;
            }

            c = c.getSuperclass();
        }
        
        if (!_nestedTransactions)
        {
            Class<?>[] interfaces = obj.getClass().getInterfaces();
    
            for (Class<?> i : interfaces)
            {
                if (i.getAnnotation(Nested.class) != null)
                {
                    _nestedTransactions = true;
                    
                    break;
                }
            }
        }
    }
    
    public Uid get_uid ()
    {
        return _txObject.get_uid();
    }
    
    public Object invoke (Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable
    {
        /*
         * Do nothing currently if not inside of a transaction and
         * not asked to create transactions for this type of object.
         * 
         * Automatically create transactions in methods for nested
         * transaction capability, i.e., duplicate what normal Arjuna
         * programmers take for granted.
         */
                
        if (_txObject == null)
            throw new LockException("Transactional object is null!");
        
        synchronized (_txObject)
        {
            synchronized (_theObject)
            {
                AtomicAction act = null;
                
                if (_nestedTransactions)
                {
                    act = new AtomicAction();
                
                    act.begin();
                }
                
                try
                {
                    LockInformation cachedLock = _cachedMethods.get(method);
        
                    
                    // todo allow null transaction context - not an issue for now with STM though!
                    
                    if (BasicAction.Current() != null)
                    {
                        Method theMethod = null;
        
                        /*
                         * Look for the corresponding method in the original object and
                         * check the annotations applied there.
                         * 
                         * Check to see if we've cached this before.
                         */
        
                        int lockType = -1;
                        boolean lockFree = false;
        
                        if (cachedLock == null)
                        {
                            for (Method mt : _methods)
                            {
                                if (mt.getName().equals(method.getName()))
                                {
                                    if (mt.getReturnType().equals(method.getReturnType()))
                                    {
                                        if (Arrays.equals(mt.getParameterTypes(), method.getParameterTypes()))
                                            theMethod = mt;
                                    }
                                }
                            }
        
                            /*
                             * Should we catch common methods, like equals, and call Object... automatically?
                             */
        
                            if (theMethod == null)
                                throw new LockException("Could not locate method "+method);
        
                            /*
                             * What about other lock types?
                             */
        
                            if (theMethod.isAnnotationPresent(ReadLock.class))
                                lockType = LockMode.READ;
                            else
                            {
                                if (theMethod.isAnnotationPresent(WriteLock.class))
                                    lockType = LockMode.WRITE;
                                else
                                {
                                    if (theMethod.isAnnotationPresent(TransactionFree.class))
                                        lockFree = true;
                                }
                            }
        
                            int timeout = LockManager.defaultSleepTime;
                            int retry = LockManager.defaultRetry;
        
                            if (theMethod.isAnnotationPresent(Timeout.class))
                                timeout = theMethod.getAnnotation(Timeout.class).period();
        
                            if (theMethod.isAnnotationPresent(Retry.class))
                                retry = theMethod.getAnnotation(Retry.class).count();
        
                            if ((lockType == -1) && (!lockFree)) // default to WRITE
                                lockType = LockMode.WRITE;
        
                            cachedLock = new LockInformation(lockType, timeout, retry);
                            _cachedMethods.put(method, cachedLock);
                        }          
        
                        // TODO type specific concurrency control (define Lock class in annotation?)
        
                        int result = _txObject.setlock(new Lock(cachedLock._lockType), cachedLock._retry, cachedLock._timeout);
        
                        if (result != LockResult.GRANTED)
                        {
                            throw new LockException("Could not set "+LockMode.stringForm(cachedLock._lockType)+" lock. Got: "+LockResult.stringForm(result));
                        }
                    }
    
                    return method.invoke(_theObject, args);
                }
                finally
                {
                    if (act != null)
                    {
                        int status = act.commit();
                        
                        if ((status != ActionStatus.COMMITTED) && (status != ActionStatus.COMMITTING))
                            throw new TransactionException("Failed to commit container transaction!", status);
                    }
                }
            }
        }
    }
    
    private static boolean initialiseStore ()
    {
        synchronized (InvocationHandler.class)
        {
            if (_storeManager == null)
            {
                try
                {
                    _storeManager = new StoreManager(null, new TwoPhaseVolatileStore(new ObjectStoreEnvironmentBean()), null);
                }
                catch (final Throwable ex)
                {
                    System.err.println("InvocationHandler could not initialise object store for pessimistic concurrency control.");
                    
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @SuppressWarnings(value={"unused"})
    private RecoverableContainer<T> _container;  // could be a persistent container, but not an issue for this class
    private T _theObject;
    private LockManager _txObject;
    private Method[] _methods;
    private HashMap<Method, InvocationHandler<T>.LockInformation> _cachedMethods = new HashMap<Method, InvocationHandler<T>.LockInformation>();
    private boolean _nestedTransactions = false;  // todo change default?
    private boolean _optimistic = false;
    
    private static StoreManager _storeManager = null;
}
