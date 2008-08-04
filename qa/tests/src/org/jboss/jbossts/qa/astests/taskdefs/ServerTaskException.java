package org.jboss.jbossts.qa.astests.taskdefs;

import java.util.Collection;
import java.util.ArrayList;

public class ServerTaskException extends Exception
{
    Collection<String> errors = new ArrayList<String> ();
    Exception cause;

    public ServerTaskException()
    {
    }

    public ServerTaskException(String message)
    {
        super(message);
    }

    public ServerTaskException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServerTaskException(Throwable cause)
    {
        super(cause);
    }

    public void addError(String error)
    {
        errors.add(error);
    }

    public void setCause(Exception cause)
    {
        this.cause = cause;
    }

    public ServerTaskException getServerTaskException()
    {
        if (cause == null && errors.size() == 0)
            return this;

        StringBuilder msg = new StringBuilder();
        String nl = System.getProperty("line.separator");

        for (String error : errors)
        {
            msg.append(error).append(nl);
        }

        //noinspection ThrowableInstanceNeverThrown
        return new ServerTaskException(msg.toString(), cause);
    }
}
