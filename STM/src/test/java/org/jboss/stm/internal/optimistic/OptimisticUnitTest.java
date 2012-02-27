/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.stm.internal.optimistic;

import java.io.IOException;
import java.util.Random;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.optimistic.OptimisticLock;
import org.jboss.stm.internal.optimistic.OptimisticLockManager;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

import junit.framework.TestCase;

/**
 * Unit tests for the Class class.
 * 
 * @author Mark Little
 */

public class OptimisticUnitTest extends TestCase
{

    public class AtomicObject extends OptimisticLockManager
    {
        public AtomicObject()
        {
            super(ObjectType.ANDPERSISTENT, ObjectModel.MULTIPLE);
            
            state = 0;

            AtomicAction A = new AtomicAction();

            A.begin();

            if (setlock(new OptimisticLock(LockMode.WRITE), 0) == LockResult.GRANTED)
            {
                if (A.commit() == ActionStatus.COMMITTED)
                    System.out.println("Created persistent object " + get_uid());
                else
                    System.out.println("Action.commit error.");
            }
            else
            {
                A.abort();

                System.out.println("setlock error.");
            }
        }
        
        public AtomicObject(Uid id, int objectModel)
        {
            super(id, objectModel);
            
            state = -1;

            AtomicAction A = new AtomicAction();

            A.begin();

            if (setlock(new OptimisticLock(LockMode.READ), 0) == LockResult.GRANTED)
            {
                if (A.commit() == ActionStatus.COMMITTED)
                    System.out.println("Recreated persistent object " + get_uid());
                else
                    System.out.println("Action.commit error.");
            }
            else
            {
                A.abort();

                System.out.println("setlock error.");
            }
        }
  
        /*
         * In the pessimistic locking case we use Locks to guard against concurrent
         * access. In the optimistic case we don't. However, this means that multiple
         * threads acting on the same instance can overwrite state and conflict. So we
         * need to make these methods thread-safe. Use synchronized keyword for now, but
         * obviously this could be finer grained. Since these are language constructs they
         * are not maintained for the duration of the transaction.
         */
        
        public synchronized void incr (int value) throws Exception
        {
            AtomicAction A = new AtomicAction();

            A.begin();
            
            /*
             * setlock will activate the state and create a checkpoint. It will also
             * add a LockRecord, which takes a snapshot of the state for later comparison.
             * That is wrong: the LockRecord needs to see the current state and the final
             * state so that it can compare the current state with the state the object
             * has at commit time. In fact it probably doesn't need to see the updated state
             * at all. It could use the updated state as an optimisation: suppose the new
             * state is different to that which existed at the time setlock was called but is
             * identical to the state update that this method made, then we probably don't
             * need to rollback! Someone else made the change for us!
             */

            if (setlock(new OptimisticLock(LockMode.WRITE), 0) == LockResult.GRANTED)
            {
                state += value;

                if (A.commit() != ActionStatus.COMMITTED)
                    throw new Exception("Action commit error.");
                else
                    return;
            }

            A.abort();

            throw new Exception("Write lock error.");
        }

        public synchronized void set (int value) throws Exception
        {
            AtomicAction A = new AtomicAction();

            A.begin();
            
            if (setlock(new OptimisticLock(LockMode.WRITE), 0) == LockResult.GRANTED)
            {
                state = value;
                
                if (A.commit() != ActionStatus.COMMITTED)
                    throw new Exception("Action commit error.");
                else
                    return;
            }

            A.abort();

            throw new Exception("Write lock error.");
        }

        public synchronized int get () throws Exception
        {
            AtomicAction A = new AtomicAction();
            int value = -1;

            A.begin();
            
            if (setlock(new OptimisticLock(LockMode.READ), 0) == LockResult.GRANTED)
            {
                value = state;

                /*
                 * We don't need to call modified for read locks, but we do need to
                 * check that the state remains unmodified at commit time. This is the job
                 * of the LockRecord. So setlock should add a LockRecord if the lock is read
                 * but should ignore if it is write, because modified must be called later
                 * instead which will do the registration.
                 */
                
                if (A.commit() == ActionStatus.COMMITTED)
                    return value;
                else
                    throw new Exception("Action commit error.");
            }

            A.abort();

            throw new Exception("Read lock error.");
        }

        public boolean save_state (OutputObjectState os, int ot)
        {
            boolean result = super.save_state(os, ot);

            if (!result)
                return false;

            try
            {
                os.packInt(state);
            }
            catch (IOException e)
            {
                result = false;
            }

            return result;
        }

        public boolean restore_state (InputObjectState os, int ot)
        {
            boolean result = super.restore_state(os, ot);

            if (!result)
                return false;

            try
            {
                state = os.unpackInt();
            }
            catch (IOException e)
            {
                result = false;
            }

            return result;
        }

        public String type ()
        {
            return "/StateManager/LockManager/OptimisticLockManager/AtomicObject";
        }

        private int state;
    }

    public class Worker extends Thread
    {
        public Worker (AtomicObject obj)
        {
            _obj = obj;
        }
        
        public void run ()
        {
            Random rand = new Random();
            
            for (int i = 0; i < 10; i++)
            {
                boolean fault;
                
                do
                {
                    fault = false;
                    
                    AtomicAction A = new AtomicAction();
                    boolean doCommit = true;
                    
                    A.begin();
                    
                    try
                    {
                        _obj.incr(i);
                    }
                    catch (final Throwable ex)
                    {
                        ex.printStackTrace();
                        
                        doCommit = false;
                        fault = true;
                    }
                    
                    if (doCommit)
                    {
                        int s = A.commit();

                        if ((s != ActionStatus.COMMITTED) && (A.status() != ActionStatus.COMMITTED))
                        {
                            fault = true;
                        }
                    }
                    else
                        A.abort();
                    
                } while (fault);
            }
        }
        
        private AtomicObject _obj;
    }

    public void testAtomicObject () throws Exception
    {
        init();

        AtomicObject obj = new AtomicObject();
        AtomicAction a = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        a.commit();
        
        assertEquals(obj.get(), 1234);
        
        a = new AtomicAction();
        
        a.begin();
        
        obj.incr(1);
        
        a.abort();
        
        assertEquals(obj.get(), 1234);
    }

    public void testMultiSet () throws Exception
    {
        init();

        AtomicObject obj = new AtomicObject();
        AtomicAction a = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        obj.set(345);
        
        a.commit();
        
        assertEquals(obj.get(), 345);
    }
    
    public void testNestedAbort () throws Exception
    {
        init();

        AtomicObject obj = new AtomicObject();
        AtomicAction a = new AtomicAction();
        AtomicAction b = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        b.begin();
        
        obj.set(345);
        
        b.abort();
        
        a.commit();
        
        assertEquals(obj.get(), 1234);
    }
    
    public void testNestedCommit () throws Exception
    {
        init();

        AtomicObject obj = new AtomicObject();
        AtomicAction a = new AtomicAction();
        AtomicAction b = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        b.begin();
        
        obj.set(345);
        
        b.commit();
        
        a.commit();
        
        assertEquals(obj.get(), 345);
    }

    public void testShared () throws Exception
    {
        init();

        AtomicObject obj1 = new AtomicObject();
        AtomicObject obj2 = new AtomicObject(obj1.get_uid(), ObjectModel.MULTIPLE);
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        obj1.set(10);
        
        A.commit();
        
        A = new AtomicAction();
        
        A.begin();
        
        assertEquals(obj2.get(), obj1.get());
        
        A.commit();
    }

    private static synchronized void init () throws Exception
    {
        if (!_init)
        {
            StoreManager sm = new StoreManager(null, new TwoPhaseVolatileStore(new ObjectStoreEnvironmentBean()), null);
            
            _init = true;
        }
    }
    
    private static boolean _init = false;
}
