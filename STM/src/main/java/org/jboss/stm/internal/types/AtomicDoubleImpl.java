/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.types.AtomicDouble;
import org.jboss.stm.types.AtomicFloat;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicDoubleImpl implements AtomicDouble
{
    public AtomicDoubleImpl ()
    {
        this(0);
    }
    
    public AtomicDoubleImpl (double s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (double val)
    {
        _state = val;
    }
    
    @ReadLock
    public double get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicDouble add (AtomicDouble obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicDouble subtract (AtomicDouble obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private double _state;
}