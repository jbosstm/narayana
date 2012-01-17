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
import org.jboss.stm.types.AtomicFloat;

// TODO maybe pull all of this into a separate jar

@Transactional
public class AtomicFloatImpl implements AtomicFloat
{
    public AtomicFloatImpl ()
    {
        this(0);
    }
    
    public AtomicFloatImpl (float s)
    {
        _state = s;
    }
    
    @WriteLock
    public void set (float val)
    {
        _state = val;
    }
    
    @ReadLock
    public float get ()
    {
        return _state;
    }
    
    @WriteLock
    public AtomicFloat add (AtomicFloat obj)
    {
        if (obj != null)
        {
            _state += obj.get();
        }
        
        return this;
    }
    
    @WriteLock
    public AtomicFloat subtract (AtomicFloat obj)
    {
        if (obj != null)
        {
            _state -= obj.get();
        }
        
        return this;
    }
    
    @State
    private float _state;
}