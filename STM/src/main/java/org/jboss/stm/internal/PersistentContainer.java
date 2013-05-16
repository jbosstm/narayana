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

package org.jboss.stm.internal;

import java.lang.reflect.Proxy;

import org.jboss.stm.internal.reflect.InvocationHandler;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.txoj.lockstore.BasicPersistentLockStore;
import com.arjuna.ats.txoj.common.txojPropertyManager;

/**
 * Instances of this class represent the transactional memory within which persistent user objects
 * can be placed and managed.
 * 
 * Objects must implement an interface through which all transactional accesses occur. We don't
 * mandate what the interface is, since that will depend upon the business logic. The interface, or
 * the implementing class, must also use the @Transactional annotation.
 * 
 * @author marklittle
 */

/*
 * Could provide a container that works on any classes without annotations. The rules would have to be
 * more restrictive:
 * 
 * (i) all operations are assumed to modify the state (therefore write lock).
 * (ii) all state is saved and restored.
 * 
 * Or use a setter/getter pattern to provide flexibility around (i).
 */

public class PersistentContainer<T> extends RecoverableContainer<T>
{
    /**
     * Create a container without a name. A name will be assigned automatically.
     */
    
    public PersistentContainer ()
    {
        this(ObjectModel.SINGLE);
    }
    
    /**
     * Create a named container.
     * 
     * @param name the name (should be unique, but this is not enforced).
     */
    
    public PersistentContainer (final String name)
    {
        this(name, ObjectModel.SINGLE);
    }
    
    /**
     * Create a container without a name. A name will be assigned automatically.
     *
     * @param global whether the instances are to be shared across address spaces
     * or classloaders.
     */
    
    public PersistentContainer (int objectModel)
    {
        super(objectModel);
        
        if (objectModel == ObjectModel.MULTIPLE)
            txojPropertyManager.getTxojEnvironmentBean().setLockStoreType(BasicPersistentLockStore.class.getName());
    }
    
    /**
     * Create a named container.
     * 
     * @param name the name (should be unique, but this is not enforced).
     * @param global whether the instances are to be shared across address spaces
     * or classloaders.
     */
    
    public PersistentContainer (final String name, int objectModel)
    {
        super(name, objectModel);
        
        _type = ObjectType.ANDPERSISTENT;
        
        if (objectModel == ObjectModel.MULTIPLE)
            txojPropertyManager.getTxojEnvironmentBean().setLockStoreType(BasicPersistentLockStore.class.getName());
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
    
    public synchronized T enlist (T member)
    {
        return super.createHandle(member, ObjectType.ANDPERSISTENT);
    }
    
    /**
     * Given a unique identifier, the container will either return an existing handle to the object or
     * recreate the handle with the same state that existed at the commit of the last transaction to use it.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @param id the unique identifier for the instance.
     * @return a handle into the transactional memory that the application should use to manipulate the object.
     */
    
    @SuppressWarnings(value={"unchecked"})
    public synchronized T recreate (T member, Uid id)
    {
        /*
         * Everything that is transactional needs to be explicitly marked as such in
         * the public API, even if the private methods are the ones that do the
         * real work.
         */

        checkObjectType(member);
        
        /*
         * Is it already registered? If so just return the same instance.
         */
        
        T proxy = _transactionalProxies.get(member);
        
        if (proxy == null)
        {
            Class<?> c = member.getClass();
            
            proxy = (T) Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new InvocationHandler<T>(this, member, id));
            
            _transactionalProxies.put(member, proxy);
        }
      
        return proxy;
    }
        
    /**
     * Gives the name of the container.
     */
    
    public String toString ()
    {
        return "PersistentContainer "+name();
    }
}
