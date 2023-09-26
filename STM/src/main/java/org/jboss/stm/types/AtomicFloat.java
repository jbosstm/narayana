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
public interface AtomicFloat
{
    @WriteLock
    public void set (float val);
    
    @ReadLock
    public float get ();
    
    @WriteLock
    public AtomicFloat add (AtomicFloat obj);
    
    @WriteLock
    public AtomicFloat subtract (AtomicFloat obj);
}