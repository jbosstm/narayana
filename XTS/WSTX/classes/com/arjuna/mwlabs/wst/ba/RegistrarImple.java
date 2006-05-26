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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RegistrarImple.java,v 1.7.4.1 2005/11/22 10:36:14 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba;

import java.util.HashMap;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wst.common.Protocols;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.ba.participants.BusinessAgreementWithCoordinatorCompletionImple;
import com.arjuna.mwlabs.wst.ba.participants.BusinessAgreementWithParticipantCompletionImple;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.Registrar;
import com.arjuna.wsc.RegistrarMapper;
import com.arjuna.wst.stub.BusinessAgreementWithCoordinatorCompletionStub;
import com.arjuna.wst.stub.BusinessAgreementWithParticipantCompletionStub;

public class RegistrarImple implements Registrar
{

    public RegistrarImple ()
        throws ProtocolNotRegisteredException, SystemException
    {
	    _coordManager = CoordinatorManagerFactory.coordinatorManager();

	    // register with mapper using tx id as protocol identifier.
        final RegistrarMapper mapper = RegistrarMapper.getFactory() ;

	    mapper.addRegistrar(Protocols.BusinessAgreementWithParticipantCompletion, this);
	    mapper.addRegistrar(Protocols.BusinessAgreementWithCoordinatorCompletion, this);
        mapper.addRegistrar(Protocols.BusinessAgreementTermination, this) ;
    }
    
    /**
     * Called when a registrar is added to a register mapper. This method will be called multiple times if the
     * registrar is added to multiple register mappers or to the same register mapper with different protocol
     * identifiers.
     *
     * @param serviceAddress the address of the service
     * @param protocolIdentifier the protocol identifier
     */

    public void install (String protocolIdentifier)
    {
    }

    /**
     * Registers the interest of participant in a particular protocol.
     *
     * @param participantProtocolServiceAddress the address of the participant protocol service
     * @param protocolIdentifier the protocol identifier
     *
     * @return the PortReference of the coordinator protocol service
     *
     * @throws AlreadyRegisteredException if the participant is already registered for this coordination protocol under
     *         this activity identifier
     * @throws InvalidProtocolException if the coordination protocol is not supported
     * @throws InvalidStateException if the state of the coordinator no longer allows registration for this
     *         coordination protocol
     * @throws NoActivityException if the activity does not exist.
     *
     * @message com.arjuna.mwlabs.wst.ba.RegistrarImple_1 [com.arjuna.mwlabs.wst.ba.RegistrarImple_1] - Invalid type URI: < {0} , {1} >
     */

    public EndpointReferenceType register (final EndpointReferenceType participantProtocolService, final String protocolIdentifier, final InstanceIdentifier instanceIdentifier)
        throws AlreadyRegisteredException, InvalidProtocolException, InvalidStateException, NoActivityException
    {
        ActivityHierarchy hier = (ActivityHierarchy) _hierarchies.get(instanceIdentifier.getInstanceIdentifier());
    	
    	if (hier == null)
    	    throw new NoActivityException();
    
    	try
    	{
    	    _coordManager.resume(hier);
    	}
    	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
    	{
    	    throw new NoActivityException();
    	}
    	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
    	{
    	    throw new InvalidProtocolException();
    	}
    	
    	// TODO check for AlreadyRegisteredException
    	
    	if (Protocols.BusinessAgreementWithParticipantCompletion.equals(protocolIdentifier))
    	{
    	    // enlist participant that wraps the requester URI.
    	    final String id = new Uid().stringForm() ;

    	    try
    	    {
                BusinessAgreementWithParticipantCompletionImple participant = new BusinessAgreementWithParticipantCompletionImple(new BusinessAgreementWithParticipantCompletionStub(id, participantProtocolService), id);
                
                _coordManager.enlistParticipant(participant);
    
                ParticipantCompletionCoordinatorProcessor.getCoordinator().activateParticipant(participant.participantManager(), id);
    
                _coordManager.suspend();
    
                return getParticipantManager(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR, id) ;
    	    }
    	    catch (Exception ex)
    	    {
                throw new InvalidStateException();
    	    }
    	}
    	else if (Protocols.BusinessAgreementWithCoordinatorCompletion.equals(protocolIdentifier))
        {
    	    final String id = new Uid().stringForm() ;
    		try
    		{
    		    BusinessAgreementWithCoordinatorCompletionImple participant = new BusinessAgreementWithCoordinatorCompletionImple(new BusinessAgreementWithCoordinatorCompletionStub(id, participantProtocolService), id);
    
    		    _coordManager.enlistParticipant(participant);
    
    
                CoordinatorCompletionCoordinatorProcessor.getCoordinator().activateParticipant(participant.participantManager(), id);
    
    		    _coordManager.suspend();
    		    
    		    return getParticipantManager(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR, id) ;
    		}
    		catch (Exception ex)
    		{
    		    throw new InvalidStateException();
    		}
        }
        else if (Protocols.BusinessAgreementTermination.equals(protocolIdentifier))
        {
            try
            {
                return getParticipantManager(ArjunaTXConstants.SERVICE_TERMINATION_COORDINATOR, instanceIdentifier.getInstanceIdentifier()) ;
            }
            catch (Exception ex)
            {
                throw new InvalidStateException();
            }
        }
        else
        {
    		wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.ba.RegistrarImple_1",
    					      new Object[]{Protocols.BusinessActivityAtomic, protocolIdentifier});
    
    		throw new InvalidProtocolException();
    	}
    }

    /**
     * Called when a registrar is removed from a register mapper. This method will be called multiple times if the
     * registrar is removed from multiple register mappers or from the same register mapper with different protocol
     * identifiers.
     *
     * @param serviceAddress the address of the service
     * @param protocolIdentifier the protocol identifier
     */

    public void uninstall (String protocolIdentifier)
    {
    }

    public final void associate () throws Exception
    {
    	// TODO colocation won't do suspend
    
    	String txIdentifier = _coordManager.identifier().toString();
    	ActivityHierarchy hier = _coordManager.suspend();
    
    	_hierarchies.put(txIdentifier, hier);
    }

    public final void disassociate (String txIdentifier) throws Exception
    {
        _hierarchies.remove(txIdentifier);
    }
    
    private EndpointReferenceType getParticipantManager(final String service, final String id)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantManagerParticipantURI = soapRegistry.getServiceURI(service) ;
        final EndpointReferenceType participantManagerParticipant = new EndpointReferenceType(new AttributedURIType(participantManagerParticipantURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantManagerParticipant, id) ;
        return participantManagerParticipant ;
    }
    
    private CoordinatorManager _coordManager = null;
    private HashMap            _hierarchies = new HashMap();
}
