/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2009,
 * @author mark.little@jboss.com
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