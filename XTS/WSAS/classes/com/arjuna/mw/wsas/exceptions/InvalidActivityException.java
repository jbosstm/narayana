/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if an attempt is made to utilise an unknown activity.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: InvalidActivityException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 * @since 1.0.
 */

public class InvalidActivityException extends WSASException
{

    public InvalidActivityException ()
    {
	super();
    }

    public InvalidActivityException (String s)
    {
	super(s);
    }

    public InvalidActivityException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}