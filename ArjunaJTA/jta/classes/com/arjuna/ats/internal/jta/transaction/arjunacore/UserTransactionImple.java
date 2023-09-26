/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

public class UserTransactionImple extends BaseTransaction
		implements jakarta.transaction.UserTransaction, javax.naming.spi.ObjectFactory, Serializable, Referenceable {
    
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