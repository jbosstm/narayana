/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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