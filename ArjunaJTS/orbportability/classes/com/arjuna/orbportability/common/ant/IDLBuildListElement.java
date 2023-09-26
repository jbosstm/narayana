/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.orbportability.common.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

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