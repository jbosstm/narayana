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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ContextFactoryImple.java,v 1.16.4.1 2005/11/22 10:36:21 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at;

import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.model.twophase.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wscf.coordinator.LocalFactory;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorControl;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.CoordinatorServiceImple;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateATCoordinator;
import com.arjuna.mwlabs.wst.at.context.ArjunaContextImple;
import com.arjuna.mwlabs.wst.at.participants.CleanupSynchronization;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.RegistrationCoordinator;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.stub.SubordinateVolatile2PCStub;
import com.arjuna.wst.stub.SubordinateDurable2PCStub;
import com.arjuna.wst.messaging.engines.ParticipantEngine;

public class ContextFactoryImple implements ContextFactory, LocalFactory
{
	public ContextFactoryImple ()
	{
		try
		{
			_coordManager = CoordinatorManagerFactory.coordinatorManager();

            _theRegistrar = new RegistrarImple();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Called when a context factory is added to a context factory mapper. This
	 * method will be called multiple times if the context factory is added to
	 * multiple context factory mappers or to the same context mapper with
	 * different protocol identifiers.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 */
	public void install(final String coordinationTypeURI)
	{
	}

	/**
	 * Creates a coordination context.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 * @param expires
	 *            the expire date/time for the returned context, can be null
	 * @param currentContext
	 *            the current context, can be null
	 * 
	 * @return the created coordination context
	 * 
	 * @throws InvalidCreateParametersException
	 *             if a parameter passed is invalid this activity identifier.
	 * 
	 */

	public CoordinationContextType create (final String coordinationTypeURI, final Long expires,
            final CoordinationContextType currentContext)
			throws InvalidCreateParametersException
	{
		if (coordinationTypeURI.equals(AtomicTransactionConstants.WSAT_PROTOCOL))
		{
			try
			{
				// make sure no transaction is currently associated

                if (currentContext == null) {
				_coordManager.suspend();

				final int timeout ;
                if (expires == null)
                {
                    timeout = 0 ;
                }
                else
                {
                    final long timeoutVal = expires.longValue() ;
                    timeout = (timeoutVal > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)timeoutVal) ;
                }

				_coordManager.begin("TwoPhaseHLS", timeout);
				
                final ArjunaContextImple arjunaContext = ArjunaContextImple.getContext() ;
                final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
                final String registrationCoordinatorURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
                
                final CoordinationContextType coordinationContext = new CoordinationContextType() ;
                coordinationContext.setCoordinationType(new URI(coordinationTypeURI)) ;
                coordinationContext.setIdentifier(new AttributedURIType("urn:"+arjunaContext.getTransactionIdentifier())) ;
                final int transactionExpires = arjunaContext.getTransactionExpires() ;
                if (transactionExpires > 0)
                {
                    coordinationContext.setExpires(new AttributedUnsignedIntType(transactionExpires)) ;
                }
                final EndpointReferenceType registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
                InstanceIdentifier.setEndpointInstanceIdentifier(registrationCoordinator, arjunaContext.getTransactionIdentifier()) ;
                coordinationContext.setRegistrationService(registrationCoordinator) ;

				/*
				 * Now add the registrar for this specific coordinator to the
				 * mapper.
				 */

				_coordManager.enlistSynchronization(new CleanupSynchronization(_coordManager.identifier().toString(), _theRegistrar));

				/*
				 * TODO Uughh! This does a suspend for us! Left over from original
				 * WS-AS stuff.
				 * 
				 * TODO
				 * REFACTOR, REFACTOR, REFACTOR.
				 */
				
				_theRegistrar.associate();

				return coordinationContext;
                } else {
                    // we need to create a subordinate transaction and register it as both a durable and volatile
                    // participant with the registration service defined in the current context

                    SubordinateATCoordinator subTx = (SubordinateATCoordinator) createSubordinate();
                    // hmm, need to create wrappers here as the subTx is in WSCF which only knows
                    // about WSAS and WS-C and the participant is in WS-T
                    String vtppid = subTx.getVolatile2PhaseId();
                    String dtppid = subTx.getDurable2PhaseId();
                    Volatile2PCParticipant vtpp = new SubordinateVolatile2PCStub(subTx);
                    Durable2PCParticipant dtpp = new SubordinateDurable2PCStub(subTx);
                    final String messageId = MessageId.getMessageId() ;
                    EndpointReferenceType participant;
                    EndpointReferenceType coordinator;
                    participant= getParticipant(vtppid);
                    coordinator = RegistrationCoordinator.register(currentContext, messageId, participant, AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC) ;
                    ParticipantProcessor.getProcessor().activateParticipant(new ParticipantEngine(vtpp, vtppid, coordinator), vtppid) ;
                    participant= getParticipant(dtppid);
                    coordinator = RegistrationCoordinator.register(currentContext, messageId, participant, AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC) ;
                    ParticipantProcessor.getProcessor().activateParticipant(new ParticipantEngine(dtpp, dtppid, coordinator), dtppid) ;

                    // ok now create the context
                    final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
                    final String registrationCoordinatorURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;

                    final CoordinationContextType coordinationContext = new CoordinationContextType() ;
                    coordinationContext.setCoordinationType(new URI(coordinationTypeURI));
                    String txId = subTx.get_uid().stringForm();
                    coordinationContext.setIdentifier(new AttributedURIType("urn:"+ txId)) ;
                    AttributedUnsignedIntType expiresObject = currentContext.getExpires();
                    if (expiresObject != null) {
                        long transactionExpires = currentContext.getExpires().getValue();
                        if (transactionExpires > 0) {
                            coordinationContext.setExpires(new AttributedUnsignedIntType(transactionExpires)) ;
                        }
                    }
                    final EndpointReferenceType registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
                    InstanceIdentifier.setEndpointInstanceIdentifier(registrationCoordinator, txId) ;
                    coordinationContext.setRegistrationService(registrationCoordinator) ;
                    coordinationContext.setRegistrationService(registrationCoordinator) ;

                    // now associate the tx id with the sub transaction

                    _theRegistrar.associate(subTx);
                    return coordinationContext;
                }
			}
			catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
			{
				ex.printStackTrace();
				
				throw new InvalidCreateParametersException();
			}
			catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
			{
				ex.printStackTrace();
			}
			catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
			{
				ex.printStackTrace();
				
				throw new InvalidCreateParametersException();
			}
			catch (Exception ex)
			{
				// TODO handle properly

				ex.printStackTrace();
			}
		}
		else {
            wstxLogger.i18NLogger.warn_mwlabs_wst_at_ContextFactoryImple_1(AtomicTransactionConstants.WSAT_PROTOCOL, coordinationTypeURI);

            throw new InvalidCreateParametersException(
                    wstxLogger.i18NLogger.get_mwlabs_wst_at_ContextFactoryImple_3()
                            + " < "
                            + AtomicTransactionConstants.WSAT_PROTOCOL
                            + ", "
                            + coordinationTypeURI + " >");
        }

		return null;
	}

	/**
	 * Called when a context factory is removed from a context factory mapper.
	 * This method will be called multiple times if the context factory is
	 * removed from multiple context factory mappers or from the same context
	 * factory mapper with different coordination type uris.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 */

	public void uninstall (final String coordinationTypeURI)
	{
		// we don't use this as one implementation is registered per type
	}

    public final Object createSubordinate () throws NoActivityException, InvalidProtocolException, SystemException
    {
        return createSubordinate(SubordinateATCoordinator.SUBORDINATE_TX_TYPE_AT_AT);
    }
    
    public final Object createSubordinate (String subordinateType) throws NoActivityException, InvalidProtocolException, SystemException
    {
		try
		{
			CoordinatorServiceImple coordManager = (CoordinatorServiceImple) _coordManager;
			CoordinatorControl theControl = coordManager.coordinatorControl();
			ATCoordinator subordinateTransaction = theControl.createSubordinate(subordinateType);
			
			/*
			 * Now add the registrar for this specific coordinator to the
			 * mapper.
			 */

			subordinateTransaction.enlistSynchronization(new CleanupSynchronization(subordinateTransaction.get_uid().stringForm(), _theRegistrar));

			_theRegistrar.associate(subordinateTransaction);
			
			return subordinateTransaction;
		}
		catch (Exception ex)
		{
			throw new SystemException(ex.toString());
		}
	}
	
    private EndpointReferenceType getParticipant(final String id)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String participantURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
        final EndpointReferenceType participant = new EndpointReferenceType(new AttributedURIType(participantURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participant, id) ;
        return participant;
    }

	public final RegistrarImple registrar ()
	{
		return _theRegistrar;
	}

	private CoordinatorManager _coordManager;
	private RegistrarImple _theRegistrar;

}
