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

import org.jboss.stm.internal.PersistentContainer;
import org.jboss.stm.internal.RecoverableContainer;

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

/*
RecoverableContainer<Sample> theContainer = new RecoverableContainer<Sample>();
Sample obj1 = theContainer.create(new SampleLockable(10));
Sample obj2 = theContainer.create(new SampleLockable(10));

// todo make it easier to create and share objects.

  Make sharing of objects like this the default even for pessimistic. Just pass back the
  same instance in that case. Also hide getUidForHandle by simply passing the obj instance.fr


Sample obj3 = theContainer.duplicate(obj1);
Sample obj4 = theContainer.duplicate(obj2);

but for duplicate to work, we'd need to create a new instance of SampleLockable, so it needs to have a default constructor. And even then, it needs to be transactional.
*/

public class Container<T>
{
    public enum TYPE { RECOVERABLE, PERSISTENT };
    
    /**
     * Create a container without a name. A name will be assigned automatically.
     */
    
    public Container ()
    {
        this(new Uid().stringForm(), TYPE.RECOVERABLE);
    }
    
    /**
     * Create a named container.
     * 
     * @param name the name (should be unique, but this is not enforced).
     */
    
    public Container (final String name)
    {
        this(name, TYPE.RECOVERABLE);
    }
    
    public Container (final String name, final TYPE type)
    {
        if (type == TYPE.RECOVERABLE)
            _theContainer = new RecoverableContainer<T>(name);
        else
            _theContainer = new PersistentContainer<T>(name);
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
    
    public final TYPE type ()
    {
        if (_theContainer.objectType() == ObjectType.RECOVERABLE)
            return TYPE.RECOVERABLE;
        else
            return TYPE.PERSISTENT;
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
     * Given an identified for an existing object, create another handle. This is particularly
     * useful when using pessimistic concurrency control and we need one object instance per
     * thread to ensure that state is safely managed.
     * 
     * @param member the instance of type T that you want to be made transactional and persistent.
     * @param id the Uid of the object.
     * @return a handle into the transactional memory that the application should use to manipulate the object.
     */
    
    public synchronized T clone (T instance, T proxy)
    {
        if (instance == null)
            throw new InvalidParameterException();
        
        return _theContainer.enlist(instance, _theContainer.getUidForHandle(proxy));
    }
    
    /*
     * Only one of these should ever be non-null.
     */
    
    private RecoverableContainer<T> _theContainer;
}
