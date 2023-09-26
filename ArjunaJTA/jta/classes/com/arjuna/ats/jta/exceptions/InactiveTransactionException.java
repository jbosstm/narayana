/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.exceptions;

/**
 * Exception may be thrown under certain circumstances. Typically
 * this is when we would like to throw IllegalStateException to indicate
 * that the transaction is in an illegal state for the action attempted
 * (inactive), but JTA has reserved that for something else, e.g., that
 * there is no transaction associated with the calling thread.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NotImplementedException.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class InactiveTransactionException extends IllegalStateException
{
    static final long serialVersionUID = -8288622240488128416L;
    
    public InactiveTransactionException ()
    {
	super();
    }

    public InactiveTransactionException (String s)
    {
	super(s);
    }
}