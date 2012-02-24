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

package org.jboss.stm.internal.types;

import java.io.IOException;
import java.util.ArrayList;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.types.AtomicArray;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

// TODO maybe pull all of this into a separate jar

/*
 * TODO currently does not assume sparse list, i.e., requires
 * contiguous.
 */

@Transactional
public class AtomicArrayImpl<E> implements AtomicArray<E>
{
    public static final int DEFAULT_SIZE = 10;
    
    public AtomicArrayImpl ()
    {
        this(DEFAULT_SIZE);
    }
    
    public AtomicArrayImpl (int size)
    {
        _size = size;
        _array = new ArrayList<E>(size);
        
        for (int i = 0; i < _size; i++)
            _array.add(i, null);
    }
    
    @WriteLock
    public synchronized void empty ()
    {
        _array = new ArrayList<E>(_size);
    }

    @ReadLock
    public synchronized E get (int index)
    {
        return _array.get(index);
    }

    @ReadLock
    public synchronized boolean isEmpty ()
    {
        return _array.isEmpty();
    }

    @WriteLock
    public synchronized void set (int index, E val)
    {
        if (basicType(val))
            _array.set(index, val);
        else
        {   
            if (_container == null)
                _container = new RecoverableContainer<E>();
            
            _array.set(index, _container.enlist(val));
        }
    }

    @ReadLock
    public synchronized int size ()
    {
        return _array.size();
    }
   
    @SaveState
    public void save_state (OutputObjectState os) throws IOException
    {
        if (_type == null)
            _type = _array.get(0).getClass();
        
       os.packInt(_size);
       
       for (int i = 0; i < _array.size(); i++)
       {
           Object inst = _array.get(i);

           if (_type.equals(Boolean.class))
               os.packBoolean(((Boolean) inst).booleanValue());
           else if (_type.equals(Byte.class))
               os.packByte(((Byte) inst).byteValue());
           else if (_type.equals(Short.class))
               os.packShort(((Short) inst).shortValue());
           else if (_type.equals(Integer.class))
               os.packInt(((Integer) inst).intValue());
           else if (_type.equals(Long.class))
               os.packLong(((Long) inst).longValue());
           else if (_type.equals(Float.class))
               os.packFloat(((Float) inst).floatValue());
           else if (_type.equals(Double.class))
               os.packDouble(((Double) inst).doubleValue());
           else if (_type.equals(Character.class))
               os.packChar(((Character) inst).charValue());
           else if (_type.equals(String.class))
               os.packString((String) inst);
           else
           {   
               if (inst == null)
                   os.packBoolean(false);
               else
               {
                   os.packBoolean(true);
                   
                   Uid temp = _container.getUidForHandle(_array.get(i));
                   
                   /*
                    * Assume transactional object! Responsible for its own state, so we only
                    * track references.
                    */
                   
                   try
                   {
                       UidHelper.packInto(temp, os);
                   }
                   catch (final Exception ex)
                   {
                       throw new IOException(ex);
                   }
               }
           }
       }
    }
    
    @SuppressWarnings("unchecked")
    @RestoreState
    public void restore_state (InputObjectState os) throws IOException
    {
        _size = os.unpackInt();
        _array = new ArrayList<E>(_size);
        
        for (int i = 0; i < _size; i++)
            _array.add(i, null);
        
        for (int i = 0; i < _size; i++)
            _array.add(i, null);
        
        for (int i = 0; i < _size; i++)
        {   
            if (_type.equals(Boolean.class))
                _array.set(i, (E) ((Boolean) os.unpackBoolean()));
            else if (_type.equals(Byte.class))
                _array.set(i, (E) ((Byte) os.unpackByte()));
            else if (_type.equals(Short.class))
                _array.set(i, (E) ((Short) os.unpackShort()));
            else if (_type.equals(Integer.class))
                _array.set(i, (E) ((Integer) os.unpackInt()));
            else if (_type.equals(Long.class))
                _array.set(i, (E) ((Long) os.unpackLong()));
            else if (_type.equals(Float.class))
                _array.set(i, (E) ((Float) os.unpackFloat()));
            else if (_type.equals(Double.class))
                _array.set(i, (E) ((Double) os.unpackDouble()));
            else if (_type.equals(Character.class))
                _array.set(i, (E) ((Character) os.unpackChar()));
            else if (_type.equals(String.class))
                _array.set(i, (E) os.unpackString());
            else
            {
                boolean ptr = os.unpackBoolean();
                
                if (!ptr)
                    _array.set(i, null);
                else
                {
                    /*
                     * Assume transactional object! Responsible for its own state, so we only
                     * track references.
                     */
                    
                    try
                    {
                        Uid temp = UidHelper.unpackFrom(os);
                        
                        _array.set(i, _container.getHandle(temp));
                    }
                    catch (final Exception ex)
                    {
                        throw new IOException(ex);
                    }
                }
            }
        }
    }
    
    private boolean basicType (E val)
    {
        if (_type == null)
            _type = val.getClass();
        
        if (_type.equals(Boolean.class))
            return true;
        else if (_type.equals(Byte.class))
            return true;
        else if (_type.equals(Short.class))
            return true;
        else if (_type.equals(Integer.class))
            return true;
        else if (_type.equals(Long.class))
            return true;
        else if (_type.equals(Float.class))
            return true;
        else if (_type.equals(Double.class))
            return true;
        else if (_type.equals(Character.class))
            return true;
        else if (_type.equals(String.class))
            return true;
        else
            return false;
    }

    private ArrayList<E> _array;
    private int _size;
    private Class<?> _type;
    private RecoverableContainer<E> _container; // used for non-basic types
}