/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc;

public class NoActivityException extends Exception
{
    public NoActivityException()
    {
    }

    public NoActivityException(String message)
    {
        super(message);
    }

    public NoActivityException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NoActivityException(Throwable cause)
    {
        super(cause);
    }
}