/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wst;

/**
 * This class represents a handle on a stack of transactions.
 * It should only be used for suspending and resuming the
 * thread-to-transaction association.
 *
 * The transaction at the top of the stack is the current transaction.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TxContext.java,v 1.5 2004/03/15 13:25:14 nmcl Exp $
 * @since XTS 1.0.
 */

public interface TxContext
{

    /**
     * @return true if the context is valid, false otherwise.
     */

    public boolean valid ();

    /**
     * @return true if the parameter represents the same context as
     * the target object, false otherwise.
     */

    public boolean equals (Object o);

}