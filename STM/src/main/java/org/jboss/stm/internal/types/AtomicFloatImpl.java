/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.types.AtomicFloat;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicFloatImpl implements AtomicFloat
{
    public AtomicFloatImpl ()
    {
        this(0);
    }
    
    public AtomicFloatImpl (float s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (float val)
    {
        _state = val;
    }
    
    @ReadLock
    public float get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicFloat add (AtomicFloat obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicFloat subtract (AtomicFloat obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private float _state;
}