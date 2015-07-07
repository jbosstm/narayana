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

package org.jboss.stm;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import org.jboss.stm.internal.PersistentContainer;
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.proxy.LockManagerProxy;
import org.jboss.stm.internal.proxy.OptimisticLockManagerProxy;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;

/**
 * Instances of this class represent the transactional memory within which user objects
 * can be placed and managed.
 * 
 * Objects must implement an interface through which all transactional accesses occur. We don't
 * mandate what the interface is, since that will depend upon the business logic. The interface, or
 * the implementing class, must also use the @Transactional annotation.
 * 
 * Unless either the Nested or NestedTopLevel annotation is used, all method invocations on objects returned from a Container
 * should be done within the context of an active transaction.
 * 
 * @author marklittle
 */

public class Container<T>
{   
    /**
     * The TYPE of the objects created by this instance.
     * 
     * RECOVERABLE cannot be shared between address spaces and cannot tolerate crash failures.
     * PERSISTENT can be shared between address spaces (though don't have to be) and can tolerate crash failures.
     * 
     * @author marklittle
     */
    
    public enum TYPE { RECOVERABLE, PERSISTENT };
    
    /**
     * The sharing MODEL of the objects created by this instance.
     * 
     * SHARED means the instance may be used within multiple processes. It must be PERSISTENT too.
     * EXCLUSIVE means that the instance will only be used within a single process. It can be PERSISTENT or RECOVERABLE.
     * 
     * @author marklittle
     */
    
    public enum MODEL { SHARED, EXCLUSIVE };
    
    /**
     * Create a container without a name. A name will be assigned automatically.
     */
    
    public Container ()
    {
        this(new Uid().stringForm(), TYPE.RECOVERABLE);
    }
    
    /**
     * Create a container (system assigned name) of the specified type. Objects will be EXCLUSIVE.
     * 
     * @param type the type of objects created.
     */
    
    public Container (final TYPE type)
    {
        this(new Uid().stringForm(), type);
    }
    
    /**
     * Create a container (system assigned name) of the specified type and model.
     * 
     * @param type the TYPE of objects.
     * @param model the MODEL of the objects.
     */
    
    public Container (final TYPE type, final MODEL model)
    {
        this(new Uid().stringForm(), type, model);
    }
    
    /**
     * Create a named container. Objects will be RECOVERABLE and EXCLUSIVE.
     * 
     * @param name the name (should be unique, but this is not enforced).
     */
    
    public Container (final String name)
    {
        this(name, TYPE.RECOVERABLE);
    }
    
    /**
     * Create a named container. Objects will be EXCLUSIVE.
     * 
     * @param name the name (should be unique, but this is not enforced).
     * @param type the TYPE of objects.
     */
    
    public Container (final String name, final TYPE type)
    {
        if (type == TYPE.RECOVERABLE)
            _theContainer = new RecoverableContainer<T>(name);
        else
            _theContainer = new PersistentContainer<T>(name);
    }
    
    /**
     * Create a named container.
     * 
     * @param name the name (should be unique, but this is not enforced).
     * @param type the TYPE of objects.
     * @param model the MODEL of objects.
     */
    
    public Container (final String name, final TYPE type, final MODEL model)
    {
        int theModel = (model == MODEL.SHARED ? ObjectModel.MULTIPLE : ObjectModel.SINGLE);
        
        if (type == TYPE.RECOVERABLE)
        {
            if (model != MODEL.EXCLUSIVE)
                throw new InvalidParameterException("Object must be EXCLUSIVE!");
            
            _theContainer = new RecoverableContainer<T>(name);  // NOTE currently ObjectModel data not exposed for RecoverableContainers
        }
        else
            _theContainer = new PersistentContainer<T>(name, theModel);
    }
    
    /**
     * Get the name of the container.
     * 
     * @return the name.
     */
    
    public final String name ()
    {
        return _theContainer.name();
    }
    
    /**
     * @return the TYPE of objects created by this instance.
     */
    
    public final TYPE type ()
    {
        if (_theContainer.objectType() == ObjectType.RECOVERABLE)
            return TYPE.RECOVERABLE;
        else
            return TYPE.PERSISTENT;
    }
    
    /**
     * @return the MODEL of the objects created by this instance.
     */
    
    public final MODEL model ()
    {
        if (_theContainer.objectModel() == ObjectModel.MULTIPLE)
            return MODEL.SHARED;
        else
            return MODEL.EXCLUSIVE;
    }
    
    /**
     * Given an object we create a new transactional instance of it and return that
     * for future use. All accesses on the returned object will be managed according to
     * the rules defined in the various annotations. If the original object instance is used
     * then no transactional manipulation will occur so you need to be careful!
     * 
     * All handles are uniquely identified using Uid.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @return a handle into the transactional memory that the application should use to manipulate the object.
     */

    public synchronized T create (T member)
    {
        return _theContainer.enlist(member);
    }
    
    /**
     * Given an existing object, create another handle. This is particularly
     * useful when using pessimistic concurrency control and we need one object instance per
     * thread to ensure that state is safely managed.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @param proxy the instance you want to copy.
     */
    
    public synchronized T clone (T member, T proxy)
    {
        if (member == null)
            throw new InvalidParameterException();
        
        /*
         * If we are using pessimistic cc then we don't need to do a clone and can
         * return the same instance. No MVCC needed here, so shortcut.
         */
        
        if (_theContainer.isPessimistic(proxy))
            return proxy;
        else
            return _theContainer.enlist(member, _theContainer.getUidForHandle(proxy));
    }
    
    /**
     * Given an identified for an existing object, create another handle. This is particularly
     * useful when using optimistic concurrency control and we need one object instance per
     * thread to ensure that state is safely managed.
     * 
     * WARNING: if the Uid is invalid, e.g., points to a state that no longer exists, then a handle
     * will still be returned because checks for validity (other than null parameter) cannot be done
     * until you try to use the state. At that time a lock will be refused (state cannot be activated)
     * and a suitable warning message will be output.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @param id the Uid of the object.
     */
    
    public synchronized T clone (T member, Uid id)
    {
        if (member == null)
            throw new InvalidParameterException();
        
        return _theContainer.enlist(member, id);
    }
    
    /**
     * @return the unique name for the instance.
     */
    
    public Uid getIdentifier (T proxy)
    {
        return _theContainer.getUidForHandle(proxy);
    }

    /**
     * Given the proxy return the container that is managing it.
     * 
     * @param proxy the instance within the container we're looking for.
     * @return the container or null. Shouldn't really be possible to get null!
     */
    
    public static final Container<?> getContainer (Object proxy)
    {
        /*
         * Rather than maintain a list of Container instances and iterate through them
         * we create a clone of the Container using the real container within the
         * proxy itself. Container is essentially a (almost) stateless wrapper class
         * anyway so hopefully this is a lightweight and faster way of achieving this.
         * Can revisit later if this creates too many instances.
         */
        
        Container<?> toReturn = null;
        
        if (proxy instanceof OptimisticLockManagerProxy<?>)
        {
            RecoverableContainer<?> cont = ((OptimisticLockManagerProxy<?>) proxy).getContainer();
            
            toReturn = new Container(cont);
        }
        else
        {
            if (proxy instanceof LockManagerProxy<?>)
            {
                RecoverableContainer<?> cont = ((LockManagerProxy<?>) proxy).getContainer();
                
                toReturn = new Container(cont);
            }
            else
                throw new IllegalArgumentException("Not a proxy object!");
        }
        
        return toReturn;
    }
    
    private Container (RecoverableContainer<T> toUse)
    {
        _theContainer = toUse;
    }
    
    /*
     * The actual container (recoverable or persistent).
     */
    
    private RecoverableContainer<T> _theContainer;
}
