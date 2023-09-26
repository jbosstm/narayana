/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.exceptions;

/**
 * Exception may be thrown under certain circumstances. Typically
 * this is when an action has been attempted, the state of
 * the transaction is invalid for that action and JTA has reserved
 * IllegalStateException to mean something else entirely that we
 * can't use!
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NotImplementedException.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class InvalidTerminationStateException extends IllegalStateException
{
    static final long serialVersionUID = 2194094002071886192L;
    
    public InvalidTerminationStateException ()
    {
	super();
    }

    public InvalidTerminationStateException (String s)
    {
	super(s);
    }
}