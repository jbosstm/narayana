/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: UserBusinessActivityImple.java,v 1.3.6.1 2005/11/22 10:36:17 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.local;

import com.arjuna.mwlabs.wst.ba.context.*;

import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.common.Protocols;

import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wst.*;

import com.arjuna.mwlabs.wst.ba.participants.*;

/**
 * This is the interface that allows transactions to be started and terminated.
 * The messaging layer converts the Commit, Rollback and Notify messages into
 * calls on this.
 */

public class UserBusinessActivityImple extends UserBusinessActivity
{
    public UserBusinessActivityImple ()
    {
    	_terminationCoordinator = new TerminationCoordinatorImple(_factory.coordinatorManager(), null);
    
    	_contextManager = ContextManager.contextManager();
    	_contextManager.initialise(_factory);
    }
    
    public void begin () throws WrongStateException, SystemException
    {
    	begin(0);
    }
    
    public void begin (int timeout) throws WrongStateException, SystemException
    {
    	try
    	{
    	    if (_contextManager.currentTransaction() != null)
    		throw new WrongStateException();
    	    
    	    CoordinationContextType ctx = _factory.create(Protocols.BusinessActivityAtomic, null, null);
    
    	    TxContextImple theContext = new TxContextImple(ctx);
    
    	    _contextManager.resume(theContext);
    	}
    	catch (com.arjuna.wst.UnknownTransactionException ex)
    	{
    	    ex.printStackTrace();
    	    
    	    throw new SystemException(ex.toString());
    	}
    	catch (com.arjuna.wsc.InvalidCreateParametersException ex)
    	{
    	    throw new SystemException(ex.toString());
    	}
    
    	/*
    	 * At this point we would normally register a completion participant.
    	 *
    	 * Registrar reg = _registrarMapper.getRegistrar(_factory.coordinatorManager().identifier().toString());
    	 *
    	 * reg.register(Protocols.Completion, null);
    	 */
    }

    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
    	try
    	{
    	    _terminationCoordinator.close();
    	}
    	catch (TransactionRolledBackException ex)
    	{
    	    throw ex;
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	finally
    	{
    	    _contextManager.tidyup();
    	}
    }
    
    public void cancel () throws UnknownTransactionException, SystemException
    {
    	try
    	{
    	    _terminationCoordinator.cancel();
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	finally
    	{
    	    _contextManager.tidyup();
    	}
    }

    public void complete () throws UnknownTransactionException, SystemException
    {
    	try
    	{
    	    _terminationCoordinator.complete();
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	finally
    	{
    	    _contextManager.tidyup();
    	}
    }

    public String transactionIdentifier ()
    {
    	try
    	{
    	    return _factory.coordinatorManager().identifier().toString();
    	}
    	catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
    	{
    	    return "NoBusinessActivity";
    	}
    	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
    	{
    	    return "Unknown";
    	}
    }

    public String toString ()
    {
        return transactionIdentifier();
    }

    static LocalContextFactoryImple _factory = new LocalContextFactoryImple();

    private TerminationCoordinatorImple _terminationCoordinator = null;
    private ContextManager              _contextManager = null;

}
