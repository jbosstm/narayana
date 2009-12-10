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
 * (C) 2009
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean.common;

import javax.management.ObjectInstance;

import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

/**
 * Base class for all Object Store instrumentation types
 *
 * @see com.arjuna.ats.arjuna.tools.osb.mbean.common.BasicBeanMBean
 */
public class BasicBean implements BasicBeanMBean
{
    protected String type;
    protected BasicBean parent;
    protected boolean marked = true;
    protected StringBuilder errorMessages = new StringBuilder();

    public BasicBean()
    {
        super();
    }

    public BasicBean(BasicBean parent, String type)
    {
		this.type = type;
        this.parent = parent;
    }

    public String getDescription() {
        return "";
    }
    public String getObjectName() {
        throw new RuntimeException("ObjectName is not implemented");
    }

    public BasicBean getParent()
    {
        return parent;
    }

    @MXBeanPropertyDescription("Record of any errors whilst populating mbean properties")
    public String getMessages()
    {
        return errorMessages.toString();
    }

    protected void clearErrors()
    {
        errorMessages.delete(0, errorMessages.length());
    }

    protected void addError(String message)
    {
        errorMessages.append(message).append(System.getProperty("line.separator", "\n"));
    }

    public String getType()
    {
        return type;
    }

    public void refresh()
    {
        clearErrors();
        unregisterDependents(true);
        register();
    }

    public  ObjectInstance register()
    {
        return JMXServer.getAgent().registerMBean(this);
    }

    public boolean unregister()
    {
        return JMXServer.getAgent().unregisterMBean(getObjectName());
    }

    public void unregisterDependents(boolean markOnly)
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BasicBean) {
            BasicBean bb = (BasicBean) obj;
            return getObjectName().equals(bb.getObjectName());
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return getObjectName().hashCode();
    }

    @Override
    public String toString()
    {
        return getObjectName();
    }

    public boolean isMarked()
    {
        return marked;
    }

    public void mark()
    {
        marked = true;
    }

}
