package com.arjuna.mwlabs.wst11.at.remote;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst11.at.context.TxContextImple;
import com.arjuna.mwlabs.wst.at.remote.ContextManager;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc.*;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.*;
import com.arjuna.wst11.messaging.engines.ParticipantEngine;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

/**
 * This is the interface that the core exposes in order to allow different types
 * of participants to be enrolled. The messaging layer continues to work in
 * terms of the registrar, but internally we map to one of these methods.
 */

public class TransactionManagerImple extends TransactionManager
{
	public TransactionManagerImple()
	{
	}

	public void enlistForDurableTwoPhase (Durable2PCParticipant tpp, String id)
			throws WrongStateException, UnknownTransactionException,
            AlreadyRegisteredException, SystemException
    {
		try
		{
			final W3CEndpointReference coordinator = registerParticipant(getParticipant(id), AtomicTransactionConstants.WSAT_SUB_PROTOCOL_DURABLE_2PC);

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
			final W3CEndpointReference coordinator = registerParticipant(getParticipant(id), AtomicTransactionConstants.WSAT_SUB_PROTOCOL_VOLATILE_2PC);

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
	 * @message com.arjuna.mwlabs.wst.at.remote.Transaction11ManagerImple_1
	 *          [com.arjuna.mwlabs.wst.at.remote.Transaction11ManagerImple_1] -
	 *          Not implemented!
	 */

	public int replay () throws SystemException
	{
		throw new SystemException(
				wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.at.remote.Transaction11ManagerImple_1"));
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

	protected W3CEndpointReference enlistForCompletion (final W3CEndpointReference participantEndpoint)
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

    private W3CEndpointReference getParticipant(final String id)
    {
        final QName serviceName = AtomicTransactionConstants.PARTICIPANT_SERVICE_QNAME;
        final QName endpointName = AtomicTransactionConstants.PARTICIPANT_PORT_QNAME;
        final String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(serviceName);
        builder.endpointName(endpointName);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
        return builder.build();
    }

	private final W3CEndpointReference registerParticipant (final W3CEndpointReference participant, final String protocol)
			throws InvalidProtocolException, InvalidStateException, NoActivityException, SystemException
	{
		TxContextImple currentTx = null;

		try
		{
			currentTx = (TxContextImple) _ctxManager.suspend();

			if (currentTx == null)
				throw new NoActivityException();

            final CoordinationContextType coordinationContext = currentTx.context().getCoordinationContext() ;
            final String messageId = MessageId.getMessageId() ;

            return com.arjuna.wsc11.RegistrationCoordinator.register(coordinationContext, messageId, participant, protocol) ;
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
