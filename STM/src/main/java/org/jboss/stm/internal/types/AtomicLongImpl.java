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
import org.jboss.stm.types.AtomicLong;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicLongImpl implements AtomicLong
{
    public AtomicLongImpl ()
    {
        this(0);
    }
    
    public AtomicLongImpl (long s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (long val)
    {
        _state = val;
    }
    
    @WriteLock
    public AtomicLong increment ()
    {
        _state++;
        
        return this;
    }
    
    @WriteLock
    public AtomicLong decrement ()
    {
        _state--;
        
        return this;
    }
    
    @ReadLock
    public long get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicLong add (AtomicLong obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicLong subtract (AtomicLong obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private long _state;
}