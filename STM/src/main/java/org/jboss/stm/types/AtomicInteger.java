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
public interface AtomicInteger
{
    @WriteLock
    public void set (int val);
    
    @ReadLock
    public int get ();
    
    @WriteLock
    public AtomicInteger increment ();
    
    @WriteLock
    public AtomicInteger decrement ();
    
    @WriteLock
    public AtomicInteger add (AtomicInteger obj);
    
    @WriteLock
    public AtomicInteger subtract (AtomicInteger obj);
}