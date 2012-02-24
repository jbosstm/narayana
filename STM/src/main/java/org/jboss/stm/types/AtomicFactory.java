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

import org.jboss.stm.internal.RecoverableContainer;
import org.jboss.stm.internal.types.AtomicBooleanImpl;
import org.jboss.stm.internal.types.AtomicDoubleImpl;
import org.jboss.stm.internal.types.AtomicFloatImpl;
import org.jboss.stm.internal.types.AtomicIntegerImpl;
import org.jboss.stm.internal.types.AtomicLongImpl;

// TODO maybe pull all of this into a separate jar

public class AtomicFactory
{
    public static AtomicFactory instance ()
    {
        return _theFactory;
    }
    
    public AtomicBoolean createBoolean ()
    {
        return createBoolean(false);
    }
    
    public AtomicBoolean createBoolean (boolean initialValue)
    {
        return _abContainer.enlist(new AtomicBooleanImpl(initialValue));
    }
    
    public AtomicInteger createInteger ()
    {
        return createInteger(0);
    }
    
    public AtomicInteger createInteger (int initialValue)
    {
        return _aiContainer.enlist(new AtomicIntegerImpl(initialValue));
    }
    
    public AtomicLong createLong ()
    {
        return createLong(0);
    }
    
    public AtomicLong createLong (long initialValue)
    {
        return _alContainer.enlist(new AtomicLongImpl(initialValue));
    }
    
    public AtomicFloat createFloat ()
    {
        return createFloat(0);
    }
    
    public AtomicFloat createFloat (float initialValue)
    {
        return _afContainer.enlist(new AtomicFloatImpl(initialValue));
    }
    
    public AtomicDouble createDouble ()
    {
        return createDouble(0);
    }
    
    public AtomicDouble createDouble (double initialValue)
    {
        return _adContainer.enlist(new AtomicDoubleImpl(initialValue));
    }

    private AtomicFactory ()
    {
    }
    
    private RecoverableContainer<AtomicInteger> _aiContainer = new RecoverableContainer<AtomicInteger>();
    private RecoverableContainer<AtomicFloat> _afContainer = new RecoverableContainer<AtomicFloat>();
    private RecoverableContainer<AtomicDouble> _adContainer = new RecoverableContainer<AtomicDouble>();
    private RecoverableContainer<AtomicLong> _alContainer = new RecoverableContainer<AtomicLong>();
    private RecoverableContainer<AtomicBoolean> _abContainer = new RecoverableContainer<AtomicBoolean>();
    
    private static final AtomicFactory _theFactory = new AtomicFactory();
}