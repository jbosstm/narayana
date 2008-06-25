/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ContextManager.java,v 1.3.8.1 2005/11/22 10:36:08 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.remote;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;

/**
 */

// publish via JNDI for each address space?

public class ContextManager
{

    public ContextManager ()
    {
    }
    
    // resume overwrites. Should we check first a la JTA?

    public void resume (TxContext tx) throws UnknownTransactionException, SystemException
    {
        _threadTxData.set(tx);
    }
    
    public TxContext suspend () throws SystemException
    {
        final TxContext ctx = currentTransaction() ;
        if (ctx != null)
        {
            _threadTxData.set(null) ;
        }
	return ctx;
    }

    public TxContext currentTransaction () throws SystemException
    {
	return (TxContext) _threadTxData.get();
    }
    
    private static ThreadLocal _threadTxData = new ThreadLocal();
    
}
