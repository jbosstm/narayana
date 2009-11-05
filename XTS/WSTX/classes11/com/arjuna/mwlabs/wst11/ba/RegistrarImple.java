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
 * Copyright (C) 2002, 2003, 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RegistrarImple.java,v 1.7.4.1 2005/11/22 10:36:14 kconner Exp $
 */

package com.arjuna.mwlabs.wst11.ba;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.exceptions.DuplicateParticipantException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst11.ba.participants.BusinessAgreementWithCoordinatorCompletionImple;
import com.arjuna.mwlabs.wst11.ba.participants.BusinessAgreementWithParticipantCompletionImple;
import com.arjuna.mwlabs.wst11.ba.BusinessActivityTerminatorImple;
import com.arjuna.mwlabs.wst11.ba.remote.SubordinateBAParticipantManagerImple;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.BACoordinator;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc.*;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionCoordinatorEngine;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionCoordinatorEngine;
import com.arjuna.wst11.stub.BusinessAgreementWithCoordinatorCompletionStub;
import com.arjuna.wst11.stub.BusinessAgreementWithParticipantCompletionStub;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wsc11.RegistrarMapper;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.concurrent.ConcurrentHashMap;

public class RegistrarImple implements com.arjuna.wsc11.Registrar
{

	public RegistrarImple() throws ProtocolNotRegisteredException,
			SystemException
	{
		_coordManager = CoordinatorManagerFactory.coordinatorManager();

		// register with mapper using tx id as protocol identifier.
		final RegistrarMapper mapper = RegistrarMapper.getFactory();

		mapper.addRegistrar(
			BusinessActivityConstants.WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION, this);
		mapper.addRegistrar(
			BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION, this);
		mapper.addRegistrar(com.arjuna.webservices.wsarjtx.ArjunaTXConstants.WSARJTX_PROTOCOL_TERMINATION, this);
	}

	/**
	 * Called when a registrar is added to a register mapper. This method will
	 * be called multiple times if the registrar is added to multiple register
	 * mappers or to the same register mapper with different protocol
	 * identifiers.
	 *
	 * @param protocolIdentifier
	 *            the protocol identifier
	 */

	public void install (String protocolIdentifier)
	{
	}

	/**
	 * Registers the interest of participant in a particular protocol.
	 *
	 * @param participantProtocolService
	 *            the address of the participant protocol service
	 * @param protocolIdentifier
	 *            the protocol identifier
	 *
	 * @return the PortReference of the coordinator protocol service
	 *
	 * @throws com.arjuna.wsc.AlreadyRegisteredException
	 *             if the participant is already registered for this
	 *             coordination protocol under this activity identifier
	 * @throws com.arjuna.wsc.InvalidProtocolException
	 *             if the coordination protocol is not supported
	 * @throws com.arjuna.wsc.InvalidStateException
	 *             if the state of the coordinator no longer allows registration
	 *             for this coordination protocol
	 * @throws com.arjuna.wsc.NoActivityException
	 *             if the activity does not exist.
	 *
	 * @message com.arjuna.mwlabs.wst.ba.Registrar11Imple_1
	 *          [com.arjuna.mwlabs.wst.ba.Registrar11Imple_1] - Invalid type URI: < {0} , {1} >
	 */

	public W3CEndpointReference register (
			final W3CEndpointReference participantProtocolService,
			final String protocolIdentifier,
			final InstanceIdentifier instanceIdentifier,
            final boolean isSecure)
			throws AlreadyRegisteredException, InvalidProtocolException,
			InvalidStateException, NoActivityException
	{
        Object tx = _hierarchies.get(instanceIdentifier.getInstanceIdentifier());

        if (tx instanceof SubordinateBACoordinator)
            return registerWithSubordinate((SubordinateBACoordinator)tx, participantProtocolService, protocolIdentifier, isSecure);

        ActivityHierarchy hier = (ActivityHierarchy) tx;

		if (hier == null) throw new NoActivityException();

		try
		{
			_coordManager.resume(hier);
		}
		catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
		{
			throw new NoActivityException();
		}
		catch (SystemException ex)
		{
			throw new InvalidProtocolException();
		}

		// TODO check for AlreadyRegisteredException

		if (BusinessActivityConstants.WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION
				.equals(protocolIdentifier))
		{
			// enlist participant that wraps the requester URI.
			final String id = new Uid().stringForm();

			try
			{
                final ParticipantCompletionCoordinatorEngine engine = new ParticipantCompletionCoordinatorEngine(id, participantProtocolService) ;
				BusinessAgreementWithParticipantCompletionImple participant = new BusinessAgreementWithParticipantCompletionImple(
						new BusinessAgreementWithParticipantCompletionStub(engine), id);
                engine.setCoordinator(participant.participantManager()) ;

				_coordManager.enlistParticipant(participant);

				_coordManager.suspend();

				return getParticipantManager(
                        BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_QNAME,
                        BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_PORT_QNAME,
                        ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME, isSecure),
						id);
			}
			catch (Exception ex)
			{
				throw new InvalidStateException();
			}
		}
		else
			if (BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION
					.equals(protocolIdentifier))
			{
				final String id = new Uid().stringForm();
				try
				{
                    final CoordinatorCompletionCoordinatorEngine engine = new CoordinatorCompletionCoordinatorEngine(id, participantProtocolService) ;
					BusinessAgreementWithCoordinatorCompletionImple participant = new BusinessAgreementWithCoordinatorCompletionImple(
							new BusinessAgreementWithCoordinatorCompletionStub(engine), id);
                    engine.setCoordinator(participant.participantManager()) ;

					_coordManager.enlistParticipant(participant);

					_coordManager.suspend();

					return getParticipantManager(
                            BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_QNAME,
							BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_PORT_QNAME,
                            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME, isSecure),
							id);
				}
				catch (Exception ex)
				{
					throw new InvalidStateException();
				}
			}
			else
				if (com.arjuna.webservices.wsarjtx.ArjunaTXConstants.WSARJTX_PROTOCOL_TERMINATION.equals(protocolIdentifier))
				{
                    /*
                     * update the server side terminator with the participant end point
                     */
                    BusinessActivityTerminatorImple terminator;
                    terminator = (BusinessActivityTerminatorImple) TerminationCoordinatorProcessor.getProcessor().getParticipant(instanceIdentifier);
                    terminator.setEndpoint(participantProtocolService);
                    
                    try
					{
                        return getParticipantManager(
                                ArjunaTX11Constants.TERMINATION_COORDINATOR_SERVICE_QNAME,
								ArjunaTX11Constants.TERMINATION_COORDINATOR_PORT_QNAME,
                                ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_COORDINATOR_SERVICE_NAME, isSecure),
                                instanceIdentifier.getInstanceIdentifier());
					}
					catch (Exception ex)
					{
						throw new InvalidStateException();
					}
				}
				else
				{
					wstxLogger.arjLoggerI18N
							.warn(
									"com.arjuna.mwlabs.wst.ba.Registrar11Imple_1",
									new Object[] { BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME, protocolIdentifier });

					throw new InvalidProtocolException();
				}
	}

	/**
	 * Called when a registrar is removed from a register mapper. This method
	 * will be called multiple times if the registrar is removed from multiple
	 * register mappers or from the same register mapper with different protocol
	 * identifiers.
	 *
	 * @param protocolIdentifier
	 *            the protocol identifier
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

    public final void associate (BACoordinator transaction) throws Exception
    {
        String txIdentifier = transaction.get_uid().stringForm();

        _hierarchies.put(txIdentifier, transaction);
    }

	public final void disassociate (String txIdentifier) throws Exception
	{
		_hierarchies.remove(txIdentifier);
	}

    private final W3CEndpointReference registerWithSubordinate(final SubordinateBACoordinator theTx,
        final W3CEndpointReference participantProtocolService, final String protocolIdentifier,
        final boolean isSecure)
            throws AlreadyRegisteredException, InvalidProtocolException,
            InvalidStateException, NoActivityException
    {
        if (BusinessActivityConstants.WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION.equals(protocolIdentifier)) {
            // enlist participant that wraps the requester URI.
            final String id = "PCP" + new Uid().stringForm();

            try {
                // we use a manager which goes direct to the tx rather than via the activity service
                BAParticipantManager manager = new SubordinateBAParticipantManagerImple(theTx, id);
                final ParticipantCompletionCoordinatorEngine engine = new ParticipantCompletionCoordinatorEngine(id, participantProtocolService) ;
                BusinessAgreementWithParticipantCompletionImple participant =
                        new BusinessAgreementWithParticipantCompletionImple(
                                manager,
                                new BusinessAgreementWithParticipantCompletionStub(engine),
                                id);
                engine.setCoordinator(participant.participantManager()) ;

                theTx.enlistParticipant(participant);

                return getParticipantManager(
                        BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_QNAME,
                        BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_PORT_QNAME,
                        ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME, isSecure),
                        id);
            } catch (DuplicateParticipantException dpe ) {
                throw new AlreadyRegisteredException();
            } catch (Exception ex) {
                throw new InvalidStateException();
            }

        } else if (BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION.equals(protocolIdentifier)) {
            // enlist participant that wraps the requester URI.
            final String id = "CCP" + new Uid().stringForm();

            try
            {
                BAParticipantManager manager = new SubordinateBAParticipantManagerImple(theTx, id);
                final CoordinatorCompletionCoordinatorEngine engine = new CoordinatorCompletionCoordinatorEngine(id, participantProtocolService) ;
                BusinessAgreementWithCoordinatorCompletionImple participant =
                        new BusinessAgreementWithCoordinatorCompletionImple(
                                manager,
                                new BusinessAgreementWithCoordinatorCompletionStub(engine),
                                id);
                engine.setCoordinator(participant.participantManager()) ;

                theTx.enlistParticipant(participant);

                return getParticipantManager(
                        BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_QNAME,
                        BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_PORT_QNAME,
                        ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME, isSecure),
                        id);
            }
            catch (Exception ex)
            {
                throw new InvalidStateException();
            }
        } else if (com.arjuna.webservices.wsarjtx.ArjunaTXConstants.WSARJTX_PROTOCOL_TERMINATION.equals(protocolIdentifier)) {
            // not allowed for subordinate transactions!

            throw new InvalidStateException();
        } else {
            wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.ba.Registrar11Imple_1", new Object[]
            { BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME, protocolIdentifier });

            throw new InvalidProtocolException();
        }
    }

	private W3CEndpointReference getParticipantManager (final QName serviceName, final QName endpointName, final String address, final String id)
	{
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(serviceName);
        builder.endpointName(endpointName);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
        return builder.build();
	}

	private CoordinatorManager _coordManager = null;

	private ConcurrentHashMap _hierarchies = new ConcurrentHashMap();
}