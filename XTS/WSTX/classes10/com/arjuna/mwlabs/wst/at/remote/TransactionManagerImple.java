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
 * $Id: TransactionManagerImple.java,v 1.19.4.1 2005/11/22 10:36:09 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at.remote;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.at.context.TxContextImple;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.RegistrationCoordinator;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.ParticipantEngine;

/**
 * This is the interface that the core exposes in order to allow different types
 * of participants to be enrolled. The messaging layer continues to work in
 * terms of the registrar, but internally we map to one of these methods.
 */

public class TransactionManagerImple extends TransactionManager
{
	public TransactionManagerImple ()
	{
	}

	public void enlistForDurableTwoPhase (Durable2PCParticipant tpp, String id)
			throws WrongStateException, UnknownTransactionException,
			AlreadyRegisteredException, SystemException
	{
		try
		{
			final EndpointReferenceType coordinator = registerParticipant(getParticipant(id) , AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC);

			ParticipantProcessor.getProcessor().activateParticipant(new ParticipantEngine(tpp, id, coordinator), id) ;
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

	public void enlistForVolatileTwoPhase (Volatile2PCParticipant tpp, String id)
			throws WrongStateException, UnknownTransactionException,
			AlreadyRegisteredException, SystemException
	{
		try
		{
			final EndpointReferenceType coordinator = registerParticipant(getParticipant(id), AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC);

			ParticipantProcessor.getProcessor().activateParticipant(new ParticipantEngine(tpp, id, coordinator), id) ;
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
			ex.printStackTrace();

			throw new UnknownTransactionException();
		}
	}

	/*
	 * TODO
	 * 
	 * Have participant interfaces in WSTX have url method and services use
	 * those. The ones in WS-T are message oriented and we translate to/from.
	 *  
	 */

	/**
	 * @message com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple_1
	 *          [com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple_1] -
	 *          Not implemented!
	 */

	public int replay () throws SystemException
	{
		throw new SystemException(
				wstxLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple_1"));
	}

	public TxContext suspend () throws SystemException
	{
		return _ctxManager.suspend();
	}

	// resume overwrites. Should we check first a la JTA?

	public void resume (TxContext tx) throws UnknownTransactionException,
			SystemException
	{
		_ctxManager.resume(tx);
	}

	public TxContext currentTransaction () throws SystemException
	{
		return _ctxManager.currentTransaction();
	}

	protected EndpointReferenceType enlistForCompletion (final EndpointReferenceType participantEndpoint)
			throws WrongStateException, UnknownTransactionException,
            AlreadyRegisteredException, SystemException
	{
		try
		{
			TxContextImple currentTx = (TxContextImple) _ctxManager.currentTransaction();

			if (currentTx == null)
				throw new com.arjuna.wsc.NoActivityException();

			return registerParticipant(participantEndpoint, AtomicTransactionConstants.WSAT_SUB_PROTOCOL_COMPLETION);
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
    
    private EndpointReferenceType getParticipant(final String id)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String serviceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
        final EndpointReferenceType participant = new EndpointReferenceType(new AttributedURIType(serviceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participant, id) ;
        return participant ;
    }

	private final EndpointReferenceType registerParticipant (final EndpointReferenceType participant, final String protocol)
			throws InvalidProtocolException, InvalidStateException, NoActivityException, SystemException
	{
		TxContextImple currentTx = null;

		try
		{
			currentTx = (TxContextImple) _ctxManager.suspend();

			if (currentTx == null)
				throw new com.arjuna.wsc.NoActivityException();

            final CoordinationContextType coordinationContext = currentTx.context().getCoordinationContext() ;
            final String messageId = new Uid().stringForm() ;
            return RegistrationCoordinator.register(coordinationContext, messageId, participant, protocol) ;
		}
		catch (final SoapFault sf)
		{
			throw new SystemException(sf.getMessage());
		}
		catch (final NoActivityException nae)
		{
			throw nae ;
		}
        catch (final InvalidStateException ise)
        {
            throw ise ;
        }
        catch (final InvalidProtocolException ipe)
        {
            throw ipe ;
        }
		catch (final Exception ex)
		{
			throw new SystemException(ex.toString());
		}
		finally
		{
			try
			{
				if (currentTx != null)
					_ctxManager.resume(currentTx);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private ContextManager _ctxManager = new ContextManager();

}
