package org.jboss.jbossts.qa.astests.taskdefs;

/**
 * Key/value holder for passing in parameters to custom ant tasks
 */
public class TaskProperty
{
    private String key;
    private String value;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    // add some syntactic sugar to make specific use of properties more intuitive

    public String getFrom()
    {
        return key;
    }

    public void setFrom(String from)
    {
        this.key = from;
    }

    public String getTo()
    {
        return value;
    }

    public void setTo(String to)
    {
        this.value = to;
    }
}
