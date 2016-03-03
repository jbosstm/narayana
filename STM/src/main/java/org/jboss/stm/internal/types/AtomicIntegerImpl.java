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

package org.jboss.stm.internal.types;

import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.types.AtomicInteger;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicIntegerImpl implements AtomicInteger
{
    public AtomicIntegerImpl ()
    {
        this(0);
    }
    
    public AtomicIntegerImpl (int s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (int val)
    {
        _state = val;
    }
    
    @WriteLock
    public AtomicInteger increment ()
    {
        _state++;
        
        return this;
    }
    
    @WriteLock
    public AtomicInteger decrement ()
    {
        _state--;
        
        return this;
    }
    
    @ReadLock
    public int get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicInteger add (AtomicInteger obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicInteger subtract (AtomicInteger obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private int _state;
}
