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
 * $Id: BusinessActivityManagerImple.java,v 1.3.8.1 2005/11/22 10:36:16 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.local;

import com.arjuna.wst.*;

import com.arjuna.mw.wst.common.Protocols;

import com.arjuna.wsc.AlreadyRegisteredException;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.TxContext;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 *
 * This could also be the interface that high-level users see (e.g., at the
 * application Web Service).
 */

public class BusinessActivityManagerImple extends BusinessActivityManager
{

    public BusinessActivityManagerImple ()
    {
	_contextManager = ContextManager.contextManager();
	_contextManager.initialise(UserBusinessActivityImple._factory);
    }
	
    public com.arjuna.wst.BAParticipantManager enlistForBusinessAgreementWithParticipantCompletion (BusinessAgreementWithParticipantCompletionParticipant pzp, String id) throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException
    {
	try
	{
	    _registrar.register(pzp, Protocols.BusinessAgreementWithParticipantCompletion, id);

	    return new BAParticipantManagerImple(id);
	}
	catch (com.arjuna.wsc.InvalidProtocolException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.wsc.InvalidStateException ex)
	{
	    throw new WrongStateException();
	}
	catch (com.arjuna.wsc.NoActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
    }

    public com.arjuna.wst.BAParticipantManager enlistForBusinessAgreementWithCoordinatorCompletion (BusinessAgreementWithCoordinatorCompletionParticipant tpp, String id) throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException
    {
	try
	{
	    _registrar.register(tpp, Protocols.BusinessAgreementWithCoordinatorCompletion, id);

	    return new BAParticipantManagerImple(id);
	}
	catch (com.arjuna.wsc.InvalidProtocolException ex)
	{
	    ex.printStackTrace();
	    
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.wsc.InvalidStateException ex)
	{
	    throw new WrongStateException();
	}
	catch (com.arjuna.wsc.NoActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
    }

    public void resume (TxContext tx) throws UnknownTransactionException, SystemException
    {
	_contextManager.resume(tx);
    }
    
    public TxContext suspend () throws SystemException
    {
	return _contextManager.suspend();
    }

    public TxContext currentTransaction () throws SystemException
    {
	return _contextManager.currentTransaction();
    }

    final LocalRegistrarImple getRegistrar ()
    {
	return _registrar;
    }

    private ContextManager _contextManager = null;
    
    private static LocalRegistrarImple _registrar = new LocalRegistrarImple();
    
}
