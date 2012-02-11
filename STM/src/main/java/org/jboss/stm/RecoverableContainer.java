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

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.internal.reflect.InvocationHandler;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.LockManager;

/**
 * Instances of this class represent the transactional memory within which non-persistent user objects
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

public class RecoverableContainer<T>
{
    /**
     * Create a container without a name. A name will be assigned automatically.
     */
    
    public RecoverableContainer ()
    {
        this(new Uid().stringForm());
    }
    
    /**
     * Create a named container.
     * 
     * @param name the name (should be unique, but this is not enforced).
     */
    
    public RecoverableContainer (final String name)
    {
        _name = name;
    }
    
    /**
     * Get the name of the container.
     * 
     * @return the name.
     */
    
    public final String name ()
    {
        return _name;
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
        return createHandle(member, ObjectType.RECOVERABLE);
    }
    
    /**
     * Given an identified for an existing object, create another handle. This is particularly
     * useful when using pessimistic concurrency control and we need one object instance per
     * thread to ensure that state is safely managed.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @param id the Uid of the object.
     * @return a handle into the transactional memory that the application should use to manipulate the object.
     */
    
    @SuppressWarnings("unchecked")
    public synchronized T enlist (T member, Uid id)
    {
        if (id == null)
            return null;
        
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
     * Return a handle through which the object should be used, rather than the one
     * passed in. Can specify the type of the object (recoverable, persistent, neither).
     */
    
    @SuppressWarnings(value={"unchecked"})
    protected synchronized T createHandle (T member, int ot)
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
            
            proxy = (T) Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new InvocationHandler<T>(this, member, ot));
            
            _transactionalProxies.put(member, proxy);
        }
        
        return proxy;
    }
    
    /*
     * Should the following methods all be protected/package scope, i.e., not for applications
     * to view and/or call?
     */
    
    /**
     * Given a Uid, return the proxy for that instance.
     * 
     * @param reference the unique identifier for the handle.
     * @return the handle or null if not present.
     */
    
    @SuppressWarnings("unchecked")
    public synchronized T getHandle (Uid reference)
    {
        if (reference == null)
            throw new IllegalArgumentException();
        
        Set<T> keys = _transactionalProxies.keySet();
        Iterator<T> iter = keys.iterator();
        
        try
        {
            while (iter.hasNext())
            {
                T obj = _transactionalProxies.get(iter.next());
                InvocationHandler<T> handler = (InvocationHandler<T>) Proxy.getInvocationHandler(obj);
                
                if (handler.get_uid().equals(reference))
                    return obj;
            }
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException(ex);
        }
        
        return null;
    }
    
    /**
     * Given a real object, return the Uid if it exists in this container.
     * 
     * @param member the real object.
     * @return the Uid for the object.
     * @exception throws IllegalArgumentException if the real object is not within the container.
     */
    
    @SuppressWarnings(value={"unchecked"})
    public Uid getUidForOriginal (T member)
    {
        T proxy = _transactionalProxies.get(member);
        
        if (proxy == null)
            throw new IllegalArgumentException("No such instance in this container.");
        
        try
        {
            InvocationHandler<T> handler = (InvocationHandler<T>) Proxy.getInvocationHandler(proxy);
            
            return handler.get_uid();
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Given a real object, return the Uid if it exists in this container.
     * 
     * @param member the real object.
     * @return the Uid for the object if it exists in the container.
     * @exception throws IllegalArgumentException if the instance is not within the container.
     */
    
    @SuppressWarnings(value={"unchecked"})
    public Uid getUidForHandle (T proxy)
    {
        try
        {
            InvocationHandler<T> handler = (InvocationHandler<T>) Proxy.getInvocationHandler(proxy);
            
            return handler.get_uid();
        }
        catch (final Exception ex)
        {
            throw new IllegalArgumentException("No such instance in this container.", ex);
        }
    }
    
    /**
     * Gives the name of the container.
     */
    
    public String toString ()
    {
        return "RecoverableContainer "+_name;
    }
    
    protected final void checkObjectType (Object member)
    {
        if ((member instanceof LockManager) || (member instanceof StateManager))
            throw new IllegalArgumentException(
                    "Object type not supported by this transactional container!");

        Class<?> c = member.getClass().getSuperclass();

        while (c != null)
        {
            if (c.getAnnotation(Transactional.class) != null)
            {
                return;
            }

            c = c.getSuperclass();
        }
        
        Class<?>[] interfaces = member.getClass().getInterfaces();

        for (Class<?> i : interfaces)
        {
            if (i.getAnnotation(Transactional.class) != null)
            {
                return;
            }
        }

        throw new IllegalArgumentException("Object is not Lockable!");
    }
    
    protected WeakHashMap<T, T> _transactionalProxies = new WeakHashMap<T, T>();
    
    private final String _name;
}
