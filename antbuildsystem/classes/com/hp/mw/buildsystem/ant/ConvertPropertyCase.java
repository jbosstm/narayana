/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mw.buildsystem.ant;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ConvertPropertyCase.java 2342 2006-03-30 13:06:17Z  $
 */

import org.apache.tools.ant.*;

/**
 * This ant task can be used to easily convert property values cases.
 *
 * @version $Id: ConvertPropertyCase.java 2342 2006-03-30 13:06:17Z  $
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
public class ConvertPropertyCase extends Task
{
    private String  _inProperty = null;
    private String  _outProperty = null;
    private String  _function = null;

    public void setInProperty(String inProperty)
    {
        _inProperty = inProperty;
    }

    public void setOutProperty(String outProperty)
    {
        _outProperty = outProperty;
    }

    public void setFunction(String function)
    {
        _function = function;
    }

    public String uppercase(String text)
    {
        return getProject().getProperty(text).toUpperCase();
    }

    public String lowercase(String text)
    {
        return getProject().getProperty(text).toLowerCase();
    }

    public void execute() throws BuildException
    {
        if ( _inProperty == null )
        {
            throw new BuildException("'inproperty' not set");
        }

        if ( _outProperty == null )
        {
            throw new BuildException("'outproperty' not set");
        }

        if ( _function == null )
        {
            throw new BuildException("'function' not set");
        }

        try
        {
            java.lang.reflect.Method function = this.getClass().getMethod(_function, new Class[] { String.class });

            String newValue = (String)function.invoke(this, new Object[] { _inProperty });

            getProject().setProperty(_outProperty, newValue);
        }
        catch (Exception e)
        {
            throw new BuildException("Failed to invoke function '"+_function+"': "+e);
        }
    }
}
