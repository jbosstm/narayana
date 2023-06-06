/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.wsc;

public class InvalidStateException extends Exception
{
    public InvalidStateException()
    {
    }

    public InvalidStateException(String message)
    {
        super(message);
    }

    public InvalidStateException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidStateException(Throwable cause)
    {
        super(cause);
    }
}