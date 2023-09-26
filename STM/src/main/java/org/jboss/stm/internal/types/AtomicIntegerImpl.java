/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.types.AtomicInteger;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicIntegerImpl implements AtomicInteger
{
    public AtomicIntegerImpl ()
    {
        this(0);
    }
    
    public AtomicIntegerImpl (int s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (int val)
    {
        _state = val;
    }
    
    @WriteLock
    public AtomicInteger increment ()
    {
        _state++;
        
        return this;
    }
    
    @WriteLock
    public AtomicInteger decrement ()
    {
        _state--;
        
        return this;
    }
    
    @ReadLock
    public int get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicInteger add (AtomicInteger obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicInteger subtract (AtomicInteger obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private int _state;
}