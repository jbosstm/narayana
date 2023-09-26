/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

/**
 * An implementation of jakarta.transaction.UserTransaction.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: UserTransactionImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class UserTransactionImple extends BaseTransaction
		implements jakarta.transaction.UserTransaction, javax.naming.spi.ObjectFactory, Serializable, Referenceable
{
    
    public UserTransactionImple ()
    {
    }

	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
									Hashtable environment) throws Exception
	{
		return this;
	}

    @Override
    public Reference getReference() throws NamingException
    {
        return new Reference(this.getClass().getCanonicalName(), this.getClass().getCanonicalName(), null);
    }
}