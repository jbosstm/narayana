/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * Thrown if the state of the transaction is incompatible with the
 * operation attempted. For example, asking the transaction to rollback
 * if it is already committing.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WrongStateException.java,v 1.2 2003/04/11 14:47:29 nmcl Exp $
 * @since 1.0.
 */

public class WrongStateException extends Exception
{
    
    public WrongStateException ()
    {
	super();
    }

    public WrongStateException (String s)
    {
	super(s);
    }
 
}