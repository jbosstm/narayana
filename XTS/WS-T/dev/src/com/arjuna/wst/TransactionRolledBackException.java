/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * Thrown if the transaction rolls back rather than commits.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TransactionRolledBackException.java,v 1.1 2003/02/03 16:24:46 nmcl Exp $
 * @since 1.0.
 */

public class TransactionRolledBackException extends Exception
{
    
    public TransactionRolledBackException ()
    {
	super();
    }

    public TransactionRolledBackException (String s)
    {
	super(s);
    }

}