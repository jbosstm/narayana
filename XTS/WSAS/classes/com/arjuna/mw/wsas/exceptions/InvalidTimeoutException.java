/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if the timeout value associated with the invoking thread
 * which should be applied to newly created activities is invalid.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: InvalidTimeoutException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 * @since 1.0.
 */

public class InvalidTimeoutException extends WSASException
{

    public InvalidTimeoutException ()
    {
	super();
    }

    public InvalidTimeoutException (String s)
    {
	super(s);
    }

    public InvalidTimeoutException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}