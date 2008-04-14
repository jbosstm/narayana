package com.arjuna.mwlabs.wst11.at;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf11.model.twophase.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.mwlabs.wst11.at.participants.CompletionCoordinatorImple;
import com.arjuna.mwlabs.wst.at.participants.DurableTwoPhaseCommitParticipant;
import com.arjuna.mwlabs.wst.at.participants.VolatileTwoPhaseCommitParticipant;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc.*;
import com.arjuna.wst11.CompletionCoordinatorParticipant;
import com.arjuna.wst11.stub.Durable2PCStub;
import com.arjuna.wst11.stub.Volatile2PCStub;
import com.arjuna.wsc11.RegistrarMapper;
import com.arjuna.wsc11.Registrar;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.HashMap;

public class RegistrarImple implements Registrar
{
	public RegistrarImple()
        throws ProtocolNotRegisteredException, SystemException
    {
		_coordManager = CoordinatorManagerFactory.coordinatorManager();

		// register with mapper using tx id as protocol identifier.
        final com.arjuna.wsc11.RegistrarMapper mapper = RegistrarMapper.getFactory() ;

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
	 * @param protocolIdentifier
	 *            the protocol identifier
	 */

	public void install (final String protocolIdentifier)
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
	 * @message com.arjuna.mwlabs.wst.at.Registrar11Imple_1
	 *          [com.arjuna.mwlabs.wst.at.Registrar11Imple_1] - Invalid type URI: < {0} ,
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
	public W3CEndpointReference register(final W3CEndpointReference participantProtocolService,
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
		catch (SystemException ex)
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
                final CompletionCoordinatorParticipant participant = new CompletionCoordinatorImple(_coordManager, hier, true, participantProtocolService) ;
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
			wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.Registrar11Imple_1", new Object[]
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

	private final W3CEndpointReference registerWithSubordinate(final SubordinateCoordinator theTx,
        final W3CEndpointReference participantProtocolService, final String protocolIdentifier)
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
			wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.Registrar11Imple_1", new Object[]
			{ AtomicTransactionConstants.WSAT_PROTOCOL, protocolIdentifier });

			throw new InvalidProtocolException();
		}
	}

    private W3CEndpointReference getCompletionCoordinator(final InstanceIdentifier instanceIdentifier)
    {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_COORDINATOR_SERVICE_NAME);
        builder.serviceName(AtomicTransactionConstants.COMPLETION_COORDINATOR_SERVICE_QNAME);
        builder.endpointName(AtomicTransactionConstants.COMPLETION_COORDINATOR_PORT_QNAME);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, instanceIdentifier);
        return builder.build();
    }

    private W3CEndpointReference getCoordinator(final String participantId)
    {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COORDINATOR_SERVICE_NAME);
        builder.serviceName(AtomicTransactionConstants.COORDINATOR_SERVICE_QNAME);
        builder.endpointName(AtomicTransactionConstants.COORDINATOR_PORT_QNAME);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, participantId);
        return builder.build();
    }

	private CoordinatorManager _coordManager = null;
	private HashMap _hierarchies = new HashMap();
}
