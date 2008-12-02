package com.arjuna.mwlabs.wst11.at.remote;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserSubTransaction;
import com.arjuna.mw.wst11.common.Environment;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wsc11.context.Context;
import com.arjuna.mwlabs.wst11.at.ContextImple;
import com.arjuna.mwlabs.wst11.at.context.TxContextImple;
import com.arjuna.mwlabs.wst.at.remote.ContextManager;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.ActivationCoordinator;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.stub.CompletionStub;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.Hashtable;

/**
 * @message com.arjuna.mwlabs.wst11.at.remote.UserSubTransactionImple_1
 *          [com.arjuna.mwlabs.wst11.at.remote.UserSubTransactionImple_1] - Received
 *          context is null!
 */
public class UserSubTransactionImple extends UserTransactionImple
{

	public UserSubTransactionImple()
	{
        super();
	}

	public void begin () throws WrongStateException, SystemException
    {
		begin(0);
	}

	public void begin (int timeout) throws WrongStateException, SystemException
	{
		try
		{
			if (_ctxManager.currentTransaction() == null)
				throw new WrongStateException();

			Context ctx = startTransaction(timeout);

			_ctxManager.resume(new TxContextImple(ctx));
		}
		catch (InvalidCreateParametersException ex)
		{
			tidyup();

			throw new SystemException(ex.toString());
		}
		catch (UnknownTransactionException ex)
		{
			tidyup();

			throw new SystemException(ex.toString());
		}
		catch (SystemException ex)
		{
			tidyup();

			throw ex;
		}
	}

	public void commit () throws TransactionRolledBackException,
            UnknownTransactionException, SecurityException, SystemException, WrongStateException
	{
        tidyup();
        throw new WrongStateException();
	}

	public void rollback () throws UnknownTransactionException, SecurityException, SystemException, WrongStateException
	{
        tidyup();
        throw new WrongStateException();
	}

	public String transactionIdentifier ()
	{
		try
		{
			return _ctxManager.currentTransaction().toString();
		}
		catch (SystemException ex)
		{
			return "Unknown";
		}
		catch (NullPointerException ex)
		{
			return "Unknown";
		}
	}

	public String toString ()
	{
		return transactionIdentifier();
	}

	/*
	 * Not sure if this is right as it doesn't map to registering a participant
	 * with the coordinator.
	 */

    private final Context startTransaction(int timeout)
			throws InvalidCreateParametersException,
			SystemException
	{
		try
		{
            // TODO: tricks for per app _activationCoordinatorService config, perhaps:
            //InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/foo.properties");



            final CoordinationContext current = currentCoordinationContext();
            final Long expires = (timeout > 0 ? new Long(timeout) : null) ;
            final String messageId = MessageId.getMessageId() ;
            final CoordinationContextType coordinationContext = ActivationCoordinator.createCoordinationContext(
                    _activationCoordinatorService, messageId, AtomicTransactionConstants.WSAT_PROTOCOL, expires, current) ;
            if (coordinationContext == null)
            {
                throw new SystemException(
                    wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst11.at.remote.UserSubTransactionImple_1"));
            }
            return new ContextImple(coordinationContext) ;
		}
        catch (final InvalidCreateParametersException icpe)
        {
            throw icpe ;
        }
		catch (final SoapFault sf)
		{
			throw new SystemException(sf.getMessage()) ;
		}
		catch (final Exception ex)
		{
			throw new SystemException(ex.toString());
		}
	}

    /**
     * get a CoordinationContext based on the one stasahed away in the current TxContext
     */
    public CoordinationContext currentCoordinationContext() throws SystemException
    {
        final TxContextImple txContext = (TxContextImple)_ctxManager.currentTransaction();
        final CoordinationContextType savedContext = txContext.context().getCoordinationContext();
        final CoordinationContext current = new CoordinationContext();
        current.setCoordinationType(savedContext.getCoordinationType());
        current.setExpires(savedContext.getExpires());
        current.setIdentifier(savedContext.getIdentifier());
        current.setRegistrationService(savedContext.getRegistrationService());
        return current;
    }

    /**
     * Create an endpoint for the local participant service labelled with the current context id which can be passed
     * to the registration service and handed on to the registered coordinator to call back to this transaction
     * @param id the current transaction context identifier
     * @return
     */
    private W3CEndpointReference getCompletionParticipant(final String id)
    {
        final QName serviceName = AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_QNAME;
        final QName endpointName = AtomicTransactionConstants.COMPLETION_INITIATOR_PORT_QNAME;
        final String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(serviceName);
        builder.endpointName(endpointName);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
        return builder.build();
    }

	private final void tidyup ()
	{
		try
		{
			_ctxManager.suspend();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}