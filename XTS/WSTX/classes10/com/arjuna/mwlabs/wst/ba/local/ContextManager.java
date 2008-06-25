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
 * $Id: ContextManager.java,v 1.4 2005/03/10 15:37:16 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.ba.local;

import com.arjuna.mw.wstx.logging.wstxLogger;

import com.arjuna.mw.wsas.activity.*;

import com.arjuna.wst.*;

import com.arjuna.mw.wst.TxContext;

import java.util.*;

/**
 * @message com.arjuna.mwlabs.wst.ba.local.ContextManager_1 [com.arjuna.mwlabs.wst.ba.local.ContextManager_1] - One context was null!
 */

public class ContextManager
{
    
    public static final ContextManager contextManager ()
    {
	return _instance;
    }
    
    public final void initialise (LocalContextFactoryImple factory)
    {
	_factory = factory;
    }
    
    public final TxContext suspend () throws SystemException
    {
	ActivityHierarchy wstxHier = null;

	try
	{
	    wstxHier = _factory.coordinatorManager().suspend();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}

	TxContext txCtx = (TxContext) _threadTxData.get();
	if (txCtx != null)
	{
	    _threadTxData.set(null) ;
	}
	
	if ((txCtx != null) && (wstxHier != null)) // should be equal!
	{
	    _contextMapper.put(txCtx, wstxHier);
	    
	    return txCtx;
	}
	else
	{
	    if ((txCtx == null) && (wstxHier == null))
		return null;
	    else
	    {
		throw new SystemException(wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.ba.local.ContextManager_1"));
	    }
	}
    }

    public final TxContext currentTransaction () throws SystemException
    {
	return (TxContext) _threadTxData.get();
    }

    // resume overwrites. Should we check first a la JTA?

    public final void resume (TxContext tx) throws UnknownTransactionException, SystemException
    {
	ActivityHierarchy wstxHier = null;
	boolean imported = true;
	
	if (tx != null)
	{
	    wstxHier = (ActivityHierarchy) _contextMapper.remove(tx);
	
	    if (wstxHier == null)
		imported = false;
	}
        _threadTxData.set(tx);
	
	try
	{
	    if (imported)
		_factory.coordinatorManager().resume(wstxHier);
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    public final void tidyup ()
    {
	TxContext txCtx = (TxContext) _threadTxData.get();
	
	if (txCtx != null)
	{
	    _threadTxData.set(null) ;
	    _contextMapper.remove(txCtx);
	}
    }

    protected ContextManager ()
    {
    }

    private static ThreadLocal _threadTxData = new ThreadLocal();
    private static HashMap     _contextMapper = new HashMap();

    private static LocalContextFactoryImple _factory = null;

    private static ContextManager _instance = new ContextManager();
    
}
