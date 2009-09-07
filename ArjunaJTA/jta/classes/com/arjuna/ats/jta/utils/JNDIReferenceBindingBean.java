/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.jta.utils;

import javax.naming.Reference;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
