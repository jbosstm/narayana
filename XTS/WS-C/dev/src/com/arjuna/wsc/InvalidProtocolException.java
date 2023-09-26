/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc;

public class InvalidProtocolException extends Exception
{
    public InvalidProtocolException()
    {
    }

    public InvalidProtocolException(String message)
    {
        super(message);
    }

    public InvalidProtocolException(Throwable cause)
    {
        super(cause);
    }

    public InvalidProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }
}