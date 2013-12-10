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

public class Container<T>
{
    public enum TYPE { RECOVERABLE, PERSISTENT };
    public enum MODEL { SHARED, EXCLUSIVE };
    
    /**
     * Create a container without a name. A name will be assigned automatically.
     */
    
    public Container ()
    {
        this(new Uid().stringForm(), TYPE.RECOVERABLE);
    }
    
    public Container (final TYPE type)
    {
        this(new Uid().stringForm(), type);
    }
    
    public Container (final TYPE type, final MODEL model)
    {
        this(new Uid().stringForm(), type, model);
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
    
    public Container (final String name, final TYPE type, final MODEL model)
    {
        int theModel = (model == MODEL.SHARED ? ObjectModel.MULTIPLE : ObjectModel.SINGLE);
        
        if (type == TYPE.RECOVERABLE)
            _theContainer = new RecoverableContainer<T>(name);  // NOTE currently ObjectModel data not exposed for RecoverableContainers
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
     * useful when using pessimistic concurrency control and we need one object instance per
     * thread to ensure that state is safely managed.
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
     * Get the unique name for the instance.
     */
    
    public Uid getIdentifier (T proxy)
    {
        return _theContainer.getUidForHandle(proxy);
    }
    
    /*
     * Only one of these should ever be non-null.
     */
    
    private RecoverableContainer<T> _theContainer;
}
