/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package com.arjuna.wsc;

public class CannotRegisterException extends Exception
{
    public CannotRegisterException()
    {
    }

    public CannotRegisterException(String message)
    {
        super(message);
    }
}