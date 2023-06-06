/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.stm;

import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

import org.jboss.stm.BasicContainerUnitTest.TestObject;
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.reflect.InvocationHandler;

/**
 * Instances of this class represent the transactional memory within which user objects
 * can be placed and managed.
 * 
 * Objects must implement an interface through which all transactional accesses occur. We don't
 * mandate what the interface is, since that will depend upon the business logic. The interface, or
 * the implementing class, must also use the @Lockable annotation.
 * 
 * TODO look at using JBossAOP to get round the problem of requiring an interface for proxying.
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

public class TestContainer<T> extends RecoverableContainer<T>
{    
    /**
     * Given an object we create a new transactional instance of it and return that
     * for future use. All accesses on the returned object will be managed according to
     * the rules defined in the various annotations. If the original object instance is used
     * then no transactional manipulation will occur so you need to be careful!
     */
    
    /**
     * Return a handle through which the object should be used, rather than the one
     * passed in.
     */
    
    @SuppressWarnings(value={"unchecked"})
    public T enlist (T member)
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
            
            proxy = (T) Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new InvocationHandler<T>(this, member));
            
            _transactionalProxies.put(member, proxy);
        }
        
        return proxy;
    }

    private WeakHashMap<T, T> _transactionalProxies = new WeakHashMap<T, T>();
}