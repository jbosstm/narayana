/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.exceptions;


public class TestException extends Exception
{

    public TestException ()
    {
	super();
    }

    public TestException (String s)
    {
	super(s);
    }

    public TestException(Exception e) {
        super (e);
    }
}