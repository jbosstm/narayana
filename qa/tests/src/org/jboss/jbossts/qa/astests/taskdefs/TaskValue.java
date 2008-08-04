package org.jboss.jbossts.qa.astests.taskdefs;

/**
 * Value holder for passing in parameters to custom ant tasks
 */
public class TaskValue
{
    String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
