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
public interface AtomicDouble
{
    @WriteLock
    public void set (double val);
    
    @ReadLock
    public double get ();
    
    @WriteLock
    public AtomicDouble add (AtomicDouble obj);
    
    @WriteLock
    public AtomicDouble subtract (AtomicDouble obj);
}