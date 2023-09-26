/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;

@Transactional
public interface AtomicLinkedList
{
    @WriteLock
    public void setPrev (AtomicLinkedList n);
    
    @ReadLock
    public AtomicLinkedList getPrev ();
    
    @WriteLock
    public void setNext (AtomicLinkedList n);
    
    @ReadLock
    public AtomicLinkedList getNext ();
}