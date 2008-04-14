/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RegistrarImple.java,v 1.20.4.1 2005/11/22 10:36:21 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at;

import java.util.HashMap;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.model.twophase.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.mwlabs.wst.at.participants.CompletionCoordinatorImple;
import com.arjuna.mwlabs.wst.at.participants.DurableTwoPhaseCommitParticipant;
import com.arjuna.mwlabs.wst.at.participants.VolatileTwoPhaseCommitParticipant;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.Registrar;
import com.arjuna.wsc.RegistrarMapper;
import com.arjuna.wst.CompletionCoordinatorParticipant;
import com.arjuna.wst.stub.Durable2PCStub;
import com.arjuna.wst.stub.Volatile2PCStub;

/*
 * TODO
 * 
 * This entire architecture needs reworking. The WSAS and WSCF modules are based
 * on the original pre-WS-CAF work and now out-of-date. They are more generic
 * than required and pass through so many different levels of indirection that
 * it confuses the code path and makes it difficult to see what is going on.
 * 
 * The notion of an activity is fine in the general context of WS-CAF and
 * WS-C/Tx, but within the implementation, it causes us to do too much
 * thread-to-activity related work. It would be like having to go through the JTA
 * UT and TM interfaces in order to do anywork on the transaction!
 */

public class RegistrarImple implements Registrar
{
	public RegistrarImple ()
        throws ProtocolNotRegisteredException, SystemException
	{
		_coordManager = CoordinatorManagerFactory.coordinatorManager();

		// register with mapper using tx id as protocol identifier.
        final RegistrarMapper mapper = RegistrarMapper.getFactory() ;

		mapper.addRegistrar(AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC, this);
		mapper.addRegistrar(AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC, this);
		mapper.addRegistrar(AtomicTransactionConstants.WSAT_SUB_PROTOCOL_COMPLETION, this);
	}

	/**
	 * Called when a registrar is added to a register mapper. This method will
	 * be called multiple times if the registrar is added to multiple register
	 * mappers or to the same register mapper with different protocol
	 * identifiers.
	 * 
	 * @param serviceAddress
	 *            the address of the service
	 * @param protocolIdentifier
	 *            the protocol identifier
	 */

	public void install (final String protocolIdentifier)
	{
	}

	/**
	 * Registers the interest of participant in a particular protocol.
	 * 
	 * @param participantProtocolServiceAddress
	 *            the address of the participant protocol service
	 * @param protocolIdentifier
	 *            the protocol identifier
	 * 
	 * @return the PortReference of the coordinator protocol service
	 * 
	 * @throws AlreadyRegisteredException
	 *             if the participant is already registered for this
	 *             coordination protocol under this activity identifier
	 * @throws InvalidProtocolException
	 *             if the coordination protocol is not supported
	 * @throws InvalidStateException
	 *             if the state of the coordinator no longer allows registration
	 *             for this coordination protocol
	 * @throws NoActivityException
	 *             if the activity does not exist.
	 * 
	 * @message com.arjuna.mwlabs.wst.at.RegistrarImple_1
	 *          [com.arjuna.mwlabs.wst.at.Registrar_1] - Invalid type URI: < {0} ,
	 *          {1} >
	 */

	/*
	 * TODO
	 * 
	 * See comment at head of class definition. We shouldn't have to rely on
	 * thread-to-activity association to register a participant. We currently do
	 * because the code is based on old WS-CAF models that are no longer
	 * applicable. This needs updating!
	 */
	public EndpointReferenceType register(final EndpointReferenceType participantProtocolService,
        final String protocolIdentifier, final InstanceIdentifier instanceIdentifier)
			throws AlreadyRegisteredException, InvalidProtocolException,
			InvalidStateException, NoActivityException
	{
		Object tx = _hierarchies.get(instanceIdentifier.getInstanceIdentifier());
		
		if (tx instanceof SubordinateCoordinator)
			return registerWithSubordinate((SubordinateCoordinator)tx, participantProtocolService, protocolIdentifier);

		ActivityHierarchy hier = (ActivityHierarchy) tx;

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

		if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC.equals(protocolIdentifier))
		{
			// enlist participant that wraps the requester URI.
			final String participantId = "D" + new Uid().stringForm();

			try
			{
                final Durable2PCStub participantStub = new Durable2PCStub(participantId, participantProtocolService) ;
				_coordManager.enlistParticipant(new DurableTwoPhaseCommitParticipant(participantStub, participantId));

				_coordManager.suspend();

				return getCoordinator(participantId) ;
			}
			catch (Exception ex)
			{
				throw new InvalidStateException();
			}
		}
		else if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC.equals(protocolIdentifier))
		{
			// enlist participant that wraps the requester URI.
			final String participantId = "V" + new Uid().stringForm();

			try
			{
                final Volatile2PCStub participantStub = new Volatile2PCStub(participantId, participantProtocolService) ;
				_coordManager.enlistSynchronization(new VolatileTwoPhaseCommitParticipant(participantStub)) ;

				_coordManager.suspend();

				return getCoordinator(participantId) ;
			}
			catch (Exception ex)
			{
				throw new InvalidStateException();
			}
		}
		else if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_COMPLETION.equals(protocolIdentifier))
		{
			try
			{
                final CompletionCoordinatorParticipant participant = new CompletionCoordinatorImple(_coordManager, hier, true) ;
                CompletionCoordinatorProcessor.getProcessor().activateParticipant(participant, instanceIdentifier.getInstanceIdentifier()) ;

				_coordManager.suspend();

				return getCompletionCoordinator(instanceIdentifier) ;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();

				throw new InvalidStateException(ex.toString());
			}
		}
		else
		{
			wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.RegistrarImple_1", new Object[]
			{ AtomicTransactionConstants.WSAT_PROTOCOL, protocolIdentifier });

			throw new InvalidProtocolException();
		}
	}

    /**
	 * Called when a registrar is removed from a register mapper. This method
	 * will be called multiple times if the registrar is removed from multiple
	 * register mappers or from the same register mapper with different protocol
	 * identifiers.
	 * 
	 * @param serviceAddress
	 *            the address of the service
	 * @param protocolIdentifier
	 *            the protocol identifier
	 */
	public void uninstall(final String protocolIdentifier)
	{
	}

	public final void associate () throws Exception
	{
		// TODO colocation won't do suspend

		String txIdentifier = _coordManager.identifier().toString();
		ActivityHierarchy hier = _coordManager.suspend();

		_hierarchies.put(txIdentifier, hier);
	}

	public final void associate (ACCoordinator transaction) throws Exception
	{
		String txIdentifier = transaction.get_uid().stringForm();
		
		_hierarchies.put(txIdentifier, transaction);
	}

	public final void disassociate (String txIdentifier) throws Exception
	{
		_hierarchies.remove(txIdentifier);
	}

	private final EndpointReferenceType registerWithSubordinate(final SubordinateCoordinator theTx,
        final EndpointReferenceType participantProtocolService, final String protocolIdentifier)
			throws AlreadyRegisteredException, InvalidProtocolException,
			InvalidStateException, NoActivityException
    {
		if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC.equals(protocolIdentifier))
		{
			// enlist participant that wraps the requester URI.
            final String participantId = "D" + new Uid().stringForm();

            try
            {
                final Durable2PCStub participantStub = new Durable2PCStub(participantId, participantProtocolService) ;
                theTx.enlistParticipant(new DurableTwoPhaseCommitParticipant(participantStub, participantId));

                return getCoordinator(participantId) ;
            }
            catch (Exception ex)
            {
                throw new InvalidStateException();
            }
		}
		else if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC.equals(protocolIdentifier))
		{
			// enlist participant that wraps the requester URI.
            final String participantId = "V" + new Uid().stringForm();

            try
            {
                final Volatile2PCStub participantStub = new Volatile2PCStub(participantId, participantProtocolService) ;
                theTx.enlistSynchronization(new VolatileTwoPhaseCommitParticipant(participantStub)) ;

                return getCoordinator(participantId) ;
            }
            catch (Exception ex)
            {
                throw new InvalidStateException();
            }
		}
		else if (AtomicTransactionConstants.WSAT_SUB_PROTOCOL_COMPLETION.equals(protocolIdentifier))
		{
			// not allowed for subordinate transactions!
			
			throw new InvalidStateException();
		}
		else
		{
			wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.RegistrarImple_1", new Object[]
			{ AtomicTransactionConstants.WSAT_PROTOCOL, protocolIdentifier });

			throw new InvalidProtocolException();
		}
	}

    private EndpointReferenceType getCompletionCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String completionCoordinatorURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_COORDINATOR) ;
        final EndpointReferenceType completionCoordinator = new EndpointReferenceType(new AttributedURIType(completionCoordinatorURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(completionCoordinator, instanceIdentifier) ;
        return completionCoordinator ;
    }

    private EndpointReferenceType getCoordinator(final String participantId)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String coordinatorURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR) ;
        final EndpointReferenceType coordinator = new EndpointReferenceType(new AttributedURIType(coordinatorURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinator, participantId) ;
        return coordinator ;
    }

	private CoordinatorManager _coordManager = null;
	private HashMap _hierarchies = new HashMap();
}
