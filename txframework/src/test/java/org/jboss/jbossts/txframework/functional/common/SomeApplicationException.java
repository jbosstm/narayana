package org.jboss.jbossts.txframework.functional.common;

import java.io.Serializable;

public class SomeApplicationException extends Exception implements Serializable
{
    public SomeApplicationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SomeApplicationException(String message)
    {
        super(message);
    }

}
