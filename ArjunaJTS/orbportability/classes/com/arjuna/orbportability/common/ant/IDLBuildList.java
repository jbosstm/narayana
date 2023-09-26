/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.orbportability.common.ant;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

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

        getProject().setProperty(_storeInProperty, propertyValue);
    }
}