/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.utils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * JavaBean for binding a Reference into JNDI.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-09
 */
public class JNDIReferenceBindingBean
{
    private String className; // The non-null class name of the object to which this reference refers.
    private String factory; // The possibly null class name of the object's factory.
    private String factoryLocation; // The possibly null location from which to load the factory (e.g. URL)

    private String bindName; // the JNDI location to bind at.


    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getFactory()
    {
        return factory;
    }

    public void setFactory(String factory)
    {
        this.factory = factory;
    }

    public String getFactoryLocation()
    {
        return factoryLocation;
    }

    public void setFactoryLocation(String factoryLocation)
    {
        this.factoryLocation = factoryLocation;
    }

    public String getBindName()
    {
        return bindName;
    }

    public void setBindName(String bindName)
    {
        this.bindName = bindName;
    }

    public void bind(InitialContext initialContext) throws NamingException
    {
        Reference ref = new Reference(className, factory, factoryLocation);
        initialContext.rebind(bindName, ref);
    }

    public void bind() throws NamingException
    {
        bind(new InitialContext());
    }

    public void unbind(InitialContext initialContext) throws NamingException
    {
        initialContext.unbind(bindName);
    }

    public void unbind() throws NamingException
    {
        unbind(new InitialContext());
    }
}