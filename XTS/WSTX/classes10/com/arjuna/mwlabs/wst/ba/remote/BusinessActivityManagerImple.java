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
 * $Id: BusinessActivityManagerImple.java,v 1.7.4.1 2005/11/22 10:36:08 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.remote;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mwlabs.wst.ba.context.TxContextImple;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.RegistrationCoordinator;
import com.arjuna.wst.BAParticipantManager;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.messaging.engines.CoordinatorCompletionParticipantEngine;
import com.arjuna.wst.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst.stub.BACoordinatorCompletionParticipantManagerStub;
import com.arjuna.wst.stub.BAParticipantCompletionParticipantManagerStub;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 */

public class BusinessActivityManagerImple extends BusinessActivityManager
{
    public BusinessActivityManagerImple ()
    {
    }
	
    public BAParticipantManager enlistForBusinessAgreementWithParticipantCompletion (BusinessAgreementWithParticipantCompletionParticipant bap, String id)
        throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException
    {
        final EndpointReferenceType participant = getParticipant(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT, id) ;
    	try
    	{
            EndpointReferenceType baPMEndpoint = registerParticipant(participant, BusinessActivityConstants.WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION);
            final ParticipantCompletionParticipantEngine engine = new ParticipantCompletionParticipantEngine(id, baPMEndpoint, bap) ;
            ParticipantCompletionParticipantProcessor.getProcessor().activateParticipant(engine, id) ;
    
            return new BAParticipantCompletionParticipantManagerStub(engine);
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
    	catch (Throwable ex)
    	{
    	    ex.printStackTrace();
    	    
    	    throw new SystemException(ex.toString());
    	}
    }

    public BAParticipantManager enlistForBusinessAgreementWithCoordinatorCompletion (BusinessAgreementWithCoordinatorCompletionParticipant bawcp, String id) throws WrongStateException, UnknownTransactionException, AlreadyRegisteredException, SystemException
    {
        final EndpointReferenceType participant = getParticipant(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT, id) ;
    	try
    	{
    	    EndpointReferenceType baPMEndpoint = registerParticipant(participant, BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION);
            final CoordinatorCompletionParticipantEngine engine = new CoordinatorCompletionParticipantEngine(id, baPMEndpoint, bawcp) ;
            CoordinatorCompletionParticipantProcessor.getProcessor().activateParticipant(engine, id) ;
        
            return new BACoordinatorCompletionParticipantManagerStub(engine);
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
    	catch (Throwable ex)
    	{
    	    ex.printStackTrace();
    	    
    	    throw new SystemException(ex.toString());
    	}
    }

    public TxContext suspend () throws SystemException
    {
        return _ctxManager.suspend();
    }

    // resume overwrites. Should we check first a la JTA?

    public void resume (TxContext tx) throws UnknownTransactionException, SystemException
    {
        _ctxManager.resume(tx);
    }

    public TxContext currentTransaction () throws SystemException
    {
        return _ctxManager.currentTransaction();
    }
	
    private final EndpointReferenceType registerParticipant(final EndpointReferenceType participant, final String protocol)
        throws com.arjuna.wsc.InvalidProtocolException, com.arjuna.wsc.InvalidStateException, com.arjuna.wsc.NoActivityException, SystemException
    {
    	TxContextImple currentTx = null;
    
    	try
    	{
    	    currentTx = (TxContextImple) _ctxManager.currentTransaction();
    	    
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
    	catch (com.arjuna.wsc.NoActivityException ex)
    	{
    	    throw ex;
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	    
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

    private EndpointReferenceType getParticipant(final String participantService, final String id)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantServiceURI = soapRegistry.getServiceURI(participantService) ;
        final EndpointReferenceType endpoint = new EndpointReferenceType(new AttributedURIType(participantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(endpoint, id) ;
        return endpoint ;
    }
    
    private ContextManager _ctxManager = new ContextManager();
}
