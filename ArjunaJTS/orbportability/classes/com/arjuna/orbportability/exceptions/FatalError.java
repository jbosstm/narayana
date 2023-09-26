/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.exceptions;

public class FatalError extends Error
{
    static final long serialVersionUID = -9060909679329943146L;

    public FatalError()
    {
        super();
    }

    public FatalError(String s)
    {
        super(s);
    }
}