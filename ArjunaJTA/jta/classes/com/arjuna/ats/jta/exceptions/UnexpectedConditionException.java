/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.exceptions;

import jakarta.transaction.SystemException;

/**
 * Exception may be thrown under certain circumstances when we are
 * forced to throw a SystemException but want to give more information.
 * Blame JTA because it tends to fail the user in a way that JTS
 * didn't do!
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NotImplementedException.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class UnexpectedConditionException extends SystemException
{
    static final long serialVersionUID = -2536448376471820651L;
    
    public UnexpectedConditionException ()
    {
	super();
    }

    public UnexpectedConditionException (String s)
    {
	super(s);
    }
}