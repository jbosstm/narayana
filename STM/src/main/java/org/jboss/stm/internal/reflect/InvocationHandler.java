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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import com.arjuna.ats.txoj.logging.txojLogger;
import org.jboss.stm.LockException;
import org.jboss.stm.TransactionException;
import org.jboss.stm.annotations.LockFree;
import org.jboss.stm.annotations.NestedTopLevel;
import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Retry;
import org.jboss.stm.annotations.Timeout;
import org.jboss.stm.annotations.TransactionFree;
import org.jboss.stm.annotations.Nested;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.optimistic.OptimisticLock;
import org.jboss.stm.internal.proxy.LockManagerProxy;
import org.jboss.stm.internal.proxy.OptimisticLockManagerProxy;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.TopLevelAction;
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

import static com.arjuna.ats.txoj.LockResult.GRANTED;

public class InvocationHandler<T> implements java.lang.reflect.InvocationHandler
{
    /*
     * If no locks are defined in annotations then we try to use the
     * names of the methods to infer a lock type to use.
     * 
     * todo do we need a "disable inference" annotation/rule? Maybe not since you
     * can always either explicitly define the lock or change the method name!
     */
    
    @SuppressWarnings("unused")
    private static final String GETTER_NAME = "GET";
    @SuppressWarnings("unused")
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
    
    /*
     * Not all possible LockManager options are available. We only support those that we need
     * at any given moment in STM.
     */
    
    public InvocationHandler (RecoverableContainer<T> c, T obj)
    {
        this(c, obj, null);
    }
    
    public InvocationHandler (RecoverableContainer<T> cont, T obj, Uid u)
    {
        _container = cont;
        _theObject = obj;
               
        /*
         * Do we need to use the optimistic LockManager instance?
         */
        
        Class<?> c = obj.getClass();
        
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
                
                throw new RuntimeException("Could not initialise ObjectStore!");
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
             * todo currently optimistic always uses persistent objects, but with an in-memory object store. This
             * is to ensure that concurrent threads do not interfere with each others current state view. If the application
             * is written in such a way that a single instance can be used and still be thread-safe, then we could go
             * back to using RECOVERABLE. Make it an annotation option?
             */
            
            if (_optimistic)
            {
                /*
                 * At this stage we ignore the type and model settings in the container. So annotations override
                 * the default container settings. Consider re-visiting the model such that either this is explicit
                 * for all objects where model/type is different to container, or change instance/container relationship
                 * so the container values continue to match the instance (even if lazily so container data is updated).
                 * Problem with latter approach is that we can have many different types of object coming from the same
                 * container.
                 */
                
                _txObject = new OptimisticLockManagerProxy<T>(obj, ObjectType.ANDPERSISTENT, ObjectModel.MULTIPLE, cont);  // recoverable or persistent
            }
            else
                _txObject = new LockManagerProxy<T>(obj, cont);  // recoverable or persistent

            // this is a new STM proxy so ensure that there is a record of it in the object store by taking a lock inside an atomic block
            AtomicAction action = new AtomicAction();

            action.begin();

            int result = _txObject.setlock((_optimistic ? new OptimisticLock(LockMode.WRITE) : new Lock(LockMode.WRITE)), 0, 0);

            if (result != GRANTED) {
                // we didn't get the lock so there must already be a record for it
                if (txojLogger.logger.isDebugEnabled()) {
                    txojLogger.logger.debugf("STM InvocationHandler::InvocationHandler could not grab lock. Got: " + LockResult.stringForm(result));
                }
            }

            action.commit();
        }
        
        _methods = obj.getClass().getDeclaredMethods();
        
        /*
         * Do we need to create (sub-) transactions when each method
         * is called?
         */       
        
        c = obj.getClass();
        
        // yeah ok, should use isAnnotationPresent ...
        
        while (c != null)
        {
            if (c.getAnnotation(Nested.class) != null)
            {
                _nestedTransactions = true;
                
                break;
            }
            
            if (c.getAnnotation(NestedTopLevel.class) != null)
            {
                _nestedTopLevel = true;
                
                break;
            }

            c = c.getSuperclass();
        }
        
        if (!_nestedTransactions || !_nestedTopLevel)
        {
            Class<?>[] interfaces = obj.getClass().getInterfaces();
    
            for (Class<?> i : interfaces)
            {
                if (i.getAnnotation(Nested.class) != null)
                {
                    _nestedTransactions = true;
                    
                    break;
                }
                
                if (i.getAnnotation(NestedTopLevel.class) != null)
                {
                    _nestedTopLevel = true;
                    
                    break;
                }
            }
        }
    }
    
    public Uid get_uid ()
    {
        if (_txObject == null)
            throw new RuntimeException("Transactional object is null!");
        
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
        
        AtomicAction currentTx = null;
        
        synchronized (_txObject)
        {
            synchronized (_theObject)
            {
                AtomicAction act = null;
                
                /*
                 * We could maybe be a bit more intelligent here and not create any
                 * nested transaction if TransactionFree is set, but there's very little
                 * overhead in creating the transaction and not using it.
                 */
                
                if (_nestedTransactions)
                {
                    act = new AtomicAction();
                
                    act.begin();
                }
                else
                {
                    if (_nestedTopLevel)
                    {
                        act = new TopLevelAction();
                        
                        act.begin();
                    }
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
                        boolean transactionFree = false;
        
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
                                        transactionFree = true;
                                    else
                                    {
                                        if (theMethod.isAnnotationPresent(LockFree.class))
                                            lockFree = true;
                                    }
                                }
                            }

                            // if TransactionFree then suspend any transactions and don't do locking

                            if (!lockFree && !transactionFree)
                            {
                                int timeout = LockManager.defaultSleepTime;
                                int retry = LockManager.defaultRetry;
            
                                if (theMethod.isAnnotationPresent(Timeout.class))
                                    timeout = theMethod.getAnnotation(Timeout.class).period();
            
                                if (theMethod.isAnnotationPresent(Retry.class))
                                    retry = theMethod.getAnnotation(Retry.class).count();
            
                                if (lockType == -1) // default to WRITE
                                    lockType = LockMode.WRITE;    

                                cachedLock = new LockInformation(lockType, timeout, retry);
                                _cachedMethods.put(method, cachedLock);
                            }
                            else
                            {
                                if (transactionFree)
                                    currentTx = AtomicAction.suspend();
                            }
                        }          
        
                        // TODO type specific concurrency control (define Lock class in annotation?)

                        if (!lockFree && !transactionFree)
                        {
                            int result = _txObject.setlock((_optimistic ? new OptimisticLock(cachedLock._lockType) : new Lock(cachedLock._lockType)), cachedLock._retry, cachedLock._timeout);

                            if (result != GRANTED)
                            {
                                throw new LockException(Thread.currentThread()+" could not set "+LockMode.stringForm(cachedLock._lockType)+" lock. Got: "+LockResult.stringForm(result));
                            }
                        }
                    }

                    try {
                        return method.invoke(_theObject, args);
                    } catch (InvocationTargetException e) {
                        if (txojLogger.logger.isTraceEnabled()) {
                            Throwable ae = e.getCause() != null ? e.getCause() : e;
                            txojLogger.logger.tracef("STM InvocationHandler::invoke application method %s threw exception %s",
                                    method.getName(), ae.getMessage());
                        }

                        throw e.getCause() != null ? e.getCause() : e;
                    }
                }
                finally
                {
                    if (act != null)
                    {
                        int status = act.commit();
                        
                        if ((status != ActionStatus.COMMITTED) && (status != ActionStatus.COMMITTING))
                        {
                            if (currentTx != null)
                                AtomicAction.resume(currentTx);
                            
                            throw new TransactionException("Failed to commit container transaction!", status);
                        }
                    }
                    
                    if (currentTx != null)
                        AtomicAction.resume(currentTx);
                }
            }
        }
    }
    
    /**
     * It might be useful to get the Container for the object at some points.
     * 
     * @return the container reference.
     */
    
    protected final RecoverableContainer<T> getContainer ()
    {
        return _container;
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
                catch (final IllegalStateException ex)
                {
                    _storeManager = null;
                    
                    // means store already initialised so check to see if compatible
                    
                    if (StoreManager.setupStore(null, ObjectModel.SINGLE).getClass().equals(TwoPhaseVolatileStore.class))
                    {
                        // do nothing, as we are ok
                    }
                    else
                    {
                        ex.printStackTrace();
                        
                        return false;
                    }
                }
                catch (final Throwable ex)
                {
                    System.err.println("InvocationHandler could not initialise object store for optimistic concurrency control.");
                    
                    ex.printStackTrace();
                    
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private RecoverableContainer<T> _container;  // could be a persistent container, but not an issue for this class
    private T _theObject;
    private LockManager _txObject;
    private Method[] _methods;
    private HashMap<Method, InvocationHandler<T>.LockInformation> _cachedMethods = new HashMap<Method, InvocationHandler<T>.LockInformation>();
    private boolean _nestedTransactions = false;  // todo change default?
    private boolean _nestedTopLevel = false;
    private boolean _optimistic = false;
    
    private static StoreManager _storeManager = null;
}
