/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc;

public class InvalidCreateParametersException extends Exception
{
    public InvalidCreateParametersException()
    {
    }

    public InvalidCreateParametersException(String message)
    {
        super(message);
    }
}