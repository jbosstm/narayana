/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;

// TODO maybe pull all of this into a separate jar

@Transactional
public interface AtomicBoolean
{
    @WriteLock
    public void set (boolean val);
    
    @ReadLock
    public boolean get ();
    
    @ReadLock
    public AtomicBoolean not ();
    
    @WriteLock
    public AtomicBoolean flip ();
    
    /**
     * The following operations produce side-effects in that the
     * return values are new objects based on the current state of the
     * current object and the parameter. If performed within the scope
     * of a transaction, the objects used to determine the return value may
     * have their states changed making the return invalid.
     */
    
    // TODO make the returned value state roll back too?
    
    @ReadLock
    public AtomicBoolean and (AtomicBoolean param);
    
    @ReadLock
    public AtomicBoolean or (AtomicBoolean param);
    
    @ReadLock
    public AtomicBoolean xor (AtomicBoolean param);
}