/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;

// TODO maybe pull all of this into a separate jar

/**
 * An array of basic types.
 */

@Transactional
public interface AtomicArray<E>
{
    @ReadLock
    public int size ();
    
    @ReadLock
    public boolean isEmpty ();
    
    @WriteLock
    public void empty ();
    
    @ReadLock
    public E get (int index);
    
    @WriteLock
    public void set (int index, E val);
}