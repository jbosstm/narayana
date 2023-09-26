/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * The parent activity was invalid in the scope it was intended to be
 * used.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: InvalidParentException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 * @since 1.0.
 */

public class InvalidParentException extends WSASException
{

    public InvalidParentException ()
    {
	super();
    }

    public InvalidParentException (String s)
    {
	super(s);
    }

    public InvalidParentException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}