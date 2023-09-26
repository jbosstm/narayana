/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.types.AtomicBoolean;
import org.jboss.stm.types.AtomicFactory;
import org.jboss.stm.types.AtomicInteger;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicBooleanImpl implements AtomicBoolean
{
    public AtomicBooleanImpl ()
    {
        this(false);
    }
    
    public AtomicBooleanImpl (boolean s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (boolean val)
    {
        _state = val;
    }
    
    @ReadLock
    public boolean get ()
    {
        return _state;
    }

    @ReadLock
    public AtomicBoolean and (AtomicBoolean param)
    {
        return AtomicFactory.instance().createBoolean(_state & param.get());
    }

    @WriteLock
    public AtomicBoolean flip ()
    {
        _state = !_state;
        
        return this;
    }

    @ReadLock
    public AtomicBoolean not ()
    {
        return AtomicFactory.instance().createBoolean(!_state);
    }

    @ReadLock
    public AtomicBoolean or (AtomicBoolean param)
    {
        return AtomicFactory.instance().createBoolean(_state | param.get());
    }

    @ReadLock
    public AtomicBoolean xor (AtomicBoolean param)
    {
        return AtomicFactory.instance().createBoolean(_state ^ param.get());
    }
    
    @State
    private boolean _state;
}