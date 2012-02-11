/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * Two phase volatile store. Allows for recoverable and shared object instances to
 * participate in a transaction. Does not support all recovery methods that are
 * specific to persistent (durable) object stores.
 * 
 * @author marklittle
 */

public class TwoPhaseVolatileStore extends ObjectStore
{
    public TwoPhaseVolatileStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException 
    {
        super(objectStoreEnvironmentBean);
    }

    /**
     * Obtain all of the Uids for a specified type.
     *
     * @param s The type to scan for.
     * @param buff The object state in which to store the Uids
     * @param m The file type to look for (e.g., committed, shadowed).
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean allObjUids(String s, InputObjectState buff, int m) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Obtain all types of objects stored in the object store.
     *
     * @param buff The state in which to store the types.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean allTypes(InputObjectState buff) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * @param u The object to query.
     * @param tn The type of the object to query.
     * @return the current state of the object's state (e.g., shadowed,
     *         committed ...)
     */

    public int currentState(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("TwoPhaseVolatileStore.currentState(Uid=" + u + ", typeName=" + tn + ")");
        }

        StateInstance inst = _stateMap.get(u);
        
        if (inst != null)
        {
            if (inst.original != null)
                return StateStatus.OS_COMMITTED;
            else
            {
                if (inst.shadow != null)
                    return StateStatus.OS_UNCOMMITTED;
            }
        }
        
        return StateStatus.OS_UNKNOWN;
    }

    /**
     * @return the name of the object store.
     */

    public String getStoreName()
    {
        return "TwoPhaseVolatileStore";
    }

    /**
     * Commit the object's state in the object store.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean commit_state(Uid u, String tn) throws ObjectStoreException
    {
        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
                throw new ObjectStoreException("Could not find state instance to commit!");

            synchronized (inst)
            {
                if (inst.shadow != null)
                {
                    inst.original = inst.shadow;
                    inst.shadow = null;
                    inst.owner = null;
                    
                    return true;
                }
                else
                    return false;
            }
        }
    }

    /**
     * Hide the object's state in the object store. Used by crash
     * recovery.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean hide_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Reveal a hidden object's state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean reveal_state(Uid u, String tn) throws ObjectStoreException
    {
        throw new ObjectStoreException("Operation not supported by this implementation");
    }

    /**
     * Read the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return the state of the object.
     */

    public InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("TwoPhaseVolatileStore.read_committed(Uid=" + u + ", typeName=" + tn + ")");
        }
 
        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
                return null;

            synchronized (inst)
            {
                if ((inst.original != null) && ((inst.owner == null) || (inst.owner == Thread.currentThread())))
                {
                    return new InputObjectState(inst.original);
                }
                else
                    return null;
            }
        }
    }

    /**
     * Read the object's shadowed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return the state of the object.
     */

    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
                return null;

            synchronized (inst)
            {
                if ((inst.shadow != null) && ((inst.owner == null) || (inst.owner == Thread.currentThread())))
                {
                    return new InputObjectState(inst.shadow);
                }
                else
                    return null;
            }
        }
    }

    /**
     * Remove the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean remove_committed(Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("TwoPhaseVolatileStore.remove_committed(Uid=" + u + ", typeName=" + tn + ")");
        }

        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
                return false;

            synchronized (inst)
            {
                if ((inst.original != null) && (inst.owner == Thread.currentThread()))
                {
                    inst.original = null;
                    inst.owner = null;
                    
                    return true;
                }
                else
                    return false;
            }
        }
    }

    /**
     * Remove the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException
    {
        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
                return false;

            synchronized (inst)
            {
                if ((inst.shadow != null) && (inst.owner == Thread.currentThread()))
                {
                    inst.shadow = null;
                    inst.owner = null;
                    
                    return true;
                }
                else
                    return false;
            }
        }
    }

    /**
     * Write a new copy of the object's committed state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean write_committed(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("TwoPhaseVolatileStore.write_committed(Uid=" + u + ", typeName=" + tn + ")");
        }

        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
            {
                _stateMap.put(u,  new StateInstance(buff, null, tn, u, Thread.currentThread()));
            }
            else
            {
                synchronized (inst)
                {
                    if (inst.original == null)
                    {
                        inst.original = buff;
                        inst.owner = Thread.currentThread();
                    }
                    else
                        return false;
                }
            }
            
            return true;
        }
    }

    /**
     * Write a copy of the object's uncommitted state.
     *
     * @param u The object to work on.
     * @param tn The type of the object to work on.
     * @param buff The state to write.
     * @return <code>true</code> if no errors occurred, <code>false</code>
     *         otherwise.
     */

    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException
    {
        synchronized (_stateMap)
        {
            StateInstance inst = _stateMap.get(u);

            if (inst == null)
            {
                inst = new StateInstance(null, buff, tn, u, Thread.currentThread());
                
                _stateMap.put(u, inst);
            }
            else
            {
                if (inst.shadow != null)
                {
                    return false; // probably another thread trying to commit optimistically.
                }
                else              
                {
                    inst.shadow = buff;
                    inst.owner = Thread.currentThread();
                }
            }
    
            return true;
        }
    }

    /**
     * Suppress directories of the specified type from
     * allTypes etc?
     */

    protected boolean supressEntry(String name)
    {
        return false;
    }
    
    private class StateInstance
    {
        public StateInstance (OutputObjectState orig, OutputObjectState sd, String tn, Uid u, Thread o)
        {
            original = orig;
            shadow = sd;
            typeName = tn;
            uid = u;
            owner = o;
        }
        
        public String toString ()
        {
            return "StateInstance < original "+(original == null ? "empty" : "present")+", shadow "+(shadow == null ? "empty" : "present")+", "+typeName+" "+uid+", "+owner+" >";
        }
        
        public OutputObjectState original;
        public OutputObjectState shadow;
        public String typeName;
        public Uid uid;
        public Thread owner;
    }
    
    /*
     * This could potentially grow indefinitely. Place a limit on the size?
     */
    
    //private WeakHashMap<Uid, StateInstance> _stateMap = new WeakHashMap<Uid, StateInstance>();
    
    private ConcurrentHashMap<Uid, StateInstance> _stateMap = new ConcurrentHashMap<Uid, StateInstance>();
}
