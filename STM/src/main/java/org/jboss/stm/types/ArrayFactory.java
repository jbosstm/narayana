/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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