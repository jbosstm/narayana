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
import org.jboss.stm.internal.types.AtomicArrayImpl;

// TODO maybe pull all of this into a separate jar

public class ArrayFactory<E>
{
    @SuppressWarnings("unchecked")
    public static ArrayFactory instance ()
    {
        return _theFactory;
    }
    
    public AtomicArray<E> createArray ()
    {
        return createArray(AtomicArrayImpl.DEFAULT_SIZE);
    }
    
    public AtomicArray<E> createArray (int size)
    {
        return _afContainer.enlist(new AtomicArrayImpl<E>(size));
    }

    private RecoverableContainer<AtomicArray<E>> _afContainer = new RecoverableContainer<AtomicArray<E>>();
    
    @SuppressWarnings("unchecked")
    private static final ArrayFactory _theFactory = new ArrayFactory();
}