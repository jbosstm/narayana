/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.internal.reflect;

import org.jboss.stm.TestContainer;

public class TestInvocationHandler<T> implements java.lang.reflect.InvocationHandler
{
    public TestInvocationHandler (TestContainer<T> c, T obj)
    {
        _container = c;
        _theObject = obj;
    }
    
    public Object invoke (Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable
    {
        return method.invoke(_theObject, args);
    }
    
    @SuppressWarnings(value={"unused"})
    private TestContainer<T> _container;
    private T _theObject;
}