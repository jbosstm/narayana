/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
/*
 * Copyright (C) 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: IDLBuildListElement.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.orbportability.common.ant;

import org.apache.tools.ant.*;

import java.util.ArrayList;

public class IDLBuildListElement extends Task
{
    protected String _source = null;
    protected String _package = "";
    protected String _mapping = "";

    public void initialise(Project p)
    {
        setProject(p);
    }

    public void setSrc(String src)
    {
        _source = src;
    }

    public void setPackage(String pckg)
    {
        _package = pckg;
    }

    public void setMappings(String mapping)
    {
        _mapping = mapping;
    }

    public void execute() throws BuildException
    {
        if (_source == null)
        {
            throw new BuildException("Attribute 'src' not specified in idl build list element");
        }
    }

    public String toString()
    {
        return ("<'" + _source + "','" + _package + "','" + _mapping + "'>");
    }
}
