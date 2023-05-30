/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.wsc;

public class AlreadyRegisteredException extends Exception
{
    public AlreadyRegisteredException()
    {
    }

    public AlreadyRegisteredException(String message)
    {
        super(message);
    }
}