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
 * $Id: IDLBuildList.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.orbportability.common.ant;

import org.apache.tools.ant.*;

import java.util.ArrayList;

public class IDLBuildList extends Task
{
    protected ArrayList _idlElements = new ArrayList();
    protected String _storeInProperty = null;

    public void setInproperty(String name)
    {
        _storeInProperty = name;
    }

    /**
     * Create and store new build list element
     *
     * @return The newly created build list element
     */
    public IDLBuildListElement createElement()
    {
        IDLBuildListElement element = new IDLBuildListElement();
        element.initialise(getProject());

        _idlElements.add(element);

        return (element);
    }

    public void execute() throws BuildException
    {
        if (_storeInProperty == null)
        {
            throw new BuildException("The 'property' attribute within the idl build list has not been specified");
        }

        String propertyValue = "";

        for (int count = 0; count < _idlElements.size(); count++)
        {
            IDLBuildListElement element = (IDLBuildListElement) _idlElements.get(count);

            propertyValue += element.toString();

            if ((count + 1) < _idlElements.size())
            {
                propertyValue += ",";
            }
        }

        project.setProperty(_storeInProperty, propertyValue);
    }
}
