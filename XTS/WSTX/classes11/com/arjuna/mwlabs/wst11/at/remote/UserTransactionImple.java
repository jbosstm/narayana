package com.arjuna.mwlabs.wst11.at.remote;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.common.Environment;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wsc11.context.Context;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mwlabs.wst11.at.ContextImple;
import com.arjuna.mwlabs.wst11.at.remote.TransactionManagerImple;
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
 * @message com.arjuna.mwlabs.wst.at.remote.UserTransaction11Imple__2
 *          [com.arjuna.mwlabs.wst.at.remote.UserTransaction11Imple__2] - Received
 *          context is null!
 */
public class UserTransactionImple extends UserTransaction
{

	public UserTransactionImple()
	{
		try
		{
            _activationCoordinatorService = System.getProperty(Environment.COORDINATOR_URL);

			/*
			 * If the coordinator URL hasn't been specified via the
			 * configuration file then assume we are using a locally registered
			 * implementation.
			 */

			if (_activationCoordinatorService == null)
			{
                final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
                _activationCoordinatorService = serviceRegistry.getServiceURI(CoordinationConstants.ACTIVATION_SERVICE_NAME) ;
			}
		}
		catch (Exception ex)
		{
			// TODO

			ex.printStackTrace();
		}
        _userSubordinateTransaction = new UserSubordinateTransactionImple();
	}

    public UserTransaction getUserSubordinateTransaction() {
        return _userSubordinateTransaction;
    }

    public void begin () throws WrongStateException, SystemException
    {
		begin(0);
	}

	public void begin (int timeout) throws WrongStateException, SystemException
	{
		try
		{
			if (_ctxManager.currentTransaction() != null)
				throw new WrongStateException();

			Context ctx = startTransaction(timeout, null);

			_ctxManager.resume(new TxContextImple(ctx));

			enlistCompletionParticipants();
		}
		catch (com.arjuna.wsc.InvalidCreateParametersException ex)
		{
			tidyup();

			throw new SystemException(ex.toString());
		}
		catch (com.arjuna.wst.UnknownTransactionException ex)
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
		try
		{
			commitWithoutAck();
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		finally
		{
			tidyup();
		}
	}

	public void rollback () throws UnknownTransactionException, SecurityException, SystemException, WrongStateException
	{
		try
		{
			abortWithoutAck();
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		finally
		{
			tidyup();
		}
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

    /**
     * method provided for the benefit of UserSubordinateTransactionImple to allow it
     * to begin a subordinate transaction which requires an existing context to be
     * installed on the thread before it will start and instal la new transaction
     *
     * @param timeout
     * @throws WrongStateException
     * @throws SystemException
     */
    public void beginSubordinate(int timeout) throws WrongStateException, SystemException
    {
        try
        {
            TxContext current = _ctxManager.currentTransaction();
            if ((current == null) || !(current instanceof TxContextImple))
                throw new WrongStateException();

            TxContextImple currentImple = (TxContextImple) current;
            Context ctx = startTransaction(timeout, currentImple);

            _ctxManager.resume(new TxContextImple(ctx));
            // n.b. we don't enlist the subordinate transaction for completion
            // that ensures that any attempt to commit or rollback will fail
        }
        catch (com.arjuna.wsc.InvalidCreateParametersException ex)
        {
            tidyup();

            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.wst.UnknownTransactionException ex)
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

	/*
	 * Not sure if this is right as it doesn't map to registering a participant
	 * with the coordinator.
	 */

	private final void enlistCompletionParticipants ()
			throws WrongStateException, UnknownTransactionException,
			SystemException
	{
		try
		{
			TransactionManagerImple tm = (TransactionManagerImple) TransactionManager.getTransactionManager();

            final TxContextImple txContext = (TxContextImple) tm.currentTransaction();
            final String id = txContext.identifier();
            final W3CEndpointReference completionCoordinator = tm.enlistForCompletion(getCompletionParticipant(id, txContext.isSecure()));

			_completionCoordinators.put(id, completionCoordinator);
		}
		catch (com.arjuna.wsc.AlreadyRegisteredException ex)
		{
			throw new SystemException(ex.toString());
		}
	}

    /**
     * fetch the coordination context type stashed in the current AT context implememtation
     * and use it to construct an instance of the coordination context extension type we need to
     * send down the wire to the activation coordinator
     * @param current the current AT context implememtation
     * @return an instance of the coordination context extension type
     */
    private CoordinationContext getContext(TxContextImple current)
    {
        CoordinationContextType contextType = getContextType(current);
        CoordinationContext context = new CoordinationContext();
        context.setCoordinationType(contextType.getCoordinationType());
        context.setExpires(contextType.getExpires());
        context.setIdentifier(contextType.getIdentifier());
        context.setRegistrationService(contextType.getRegistrationService());

        return context;
    }

    /**
     * fetch the coordination context type stashed in the current AT context implememtation
     * @param current the current AT context implememtation
     * @return the coordination context type stashed in the current AT context implememtation
     */
    private CoordinationContextType getContextType(TxContextImple current)
    {
        ContextImple contextImple = (ContextImple)current.context();
        return contextImple.getCoordinationContext();
    }

    protected final Context startTransaction(int timeout, TxContextImple current)
			throws com.arjuna.wsc.InvalidCreateParametersException,
			SystemException
	{
		try
		{
            // TODO: tricks for per app _activationCoordinatorService config, perhaps:
            //InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/foo.properties");


            final Long expires = (timeout > 0 ? new Long(timeout) : null) ;
            final String messageId = MessageId.getMessageId() ;
            final CoordinationContext currentContext = (current != null ? getContext(current) : null);
            final CoordinationContextType coordinationContext = ActivationCoordinator.createCoordinationContext(
                    _activationCoordinatorService, messageId, AtomicTransactionConstants.WSAT_PROTOCOL, expires, currentContext) ;
            if (coordinationContext == null)
            {
                throw new SystemException(
                    wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.at.remote.UserTransaction11Imple__2"));
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

	private final void commitWithoutAck ()
			throws TransactionRolledBackException, UnknownTransactionException,
			SecurityException, SystemException, WrongStateException
	{
		TxContextImple ctx = null;
		String id = null;

		try
		{
			ctx = (TxContextImple) _ctxManager.suspend();
            if (ctx == null) {
                throw new WrongStateException();
            }
			id = ctx.identifier();

			/*
			 * By default the completionParticipantURL won't be set for an interposed (imported)
			 * bridged transaction. This is fine, because you shouldn't be able to commit that
			 * transaction from a node in the tree, only from the root. So, we can prevent commit
			 * or rollback at this stage. The alternative would be to setup the completionParticipantURL
			 * and throw the exception from the remote coordinator side (see enlistCompletionParticipants
			 * for how to do this).
			 *
			 * The same applies for an interposed subordinate transaction created via beginSubordinate.
			 */

			final W3CEndpointReference completionCoordinator = (W3CEndpointReference) _completionCoordinators.get(id);

			if (completionCoordinator == null)
				throw new WrongStateException();

			CompletionStub completionStub = new CompletionStub(id, completionCoordinator);

			completionStub.commit();
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (TransactionRolledBackException ex)
		{
			throw ex;
		}
		catch (UnknownTransactionException ex)
		{
			throw ex;
		}
		catch (SecurityException ex)
		{
			throw ex;
		}
        catch (WrongStateException ex)
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
				if (ctx != null)
					_ctxManager.resume(ctx);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			if (id != null)
				_completionCoordinators.remove(id);
		}
	}

	private final void abortWithoutAck () throws UnknownTransactionException, SecurityException,
			SystemException, WrongStateException
	{
		TxContextImple ctx = null;
		String id = null;

		try
		{
			ctx = (TxContextImple) _ctxManager.suspend();
            if (ctx == null) {
                throw new WrongStateException();
            }
			id = ctx.identifier();

			/*
			 * By default the completionParticipantURL won't be set for an interposed (imported)
			 * bridged transaction. This is fine, because you shouldn't be able to commit that
			 * transaction from a node in the tree, only from the root. So, we can prevent commit
			 * or rollback at this stage. The alternative would be to setup the completionParticipantURL
			 * and throw the exception from the remote coordinator side (see enlistCompletionParticipants
			 * for how to do this).
			 *
			 * The same applies for an interposed subordinate transaction created via beginSubordinate.
			 */

			W3CEndpointReference completionCoordinator = (W3CEndpointReference) _completionCoordinators.get(id);

			if (completionCoordinator == null)
				throw new WrongStateException();

			CompletionStub completionStub = new CompletionStub(id, completionCoordinator);

			completionStub.rollback();
		}
		catch (SystemException ex)
		{
			throw ex;
		}
		catch (UnknownTransactionException ex)
		{
			throw ex;
		}
		catch (SecurityException ex)
		{
			throw ex;
		}
        catch (WrongStateException ex)
        {
            throw ex;
        }
		catch (Exception ex)
		{
			throw new SystemException(ex.toString());
		}
		finally
		{
			try
			{
				if (ctx != null)
					_ctxManager.resume(ctx);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			if (id != null)
				_completionCoordinators.remove(id);
		}
	}

    /**
     * Create an endpoint for the local participant service labelled with the current context id which can be passed
     * to the registration service and handed on to the registered coordinator to call back to this transaction
     * @param id the current transaction context identifier
     * @return
     */
    private W3CEndpointReference getCompletionParticipant(final String id, final boolean isSecure)
    {
        final QName serviceName = AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_QNAME;
        final QName endpointName = AtomicTransactionConstants.COMPLETION_INITIATOR_PORT_QNAME;
        final String address = ServiceRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME, isSecure);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(serviceName);
        builder.endpointName(endpointName);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
        return builder.build();
    }

	protected final void tidyup ()
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

	protected ContextManager _ctxManager = new ContextManager();
	protected String _activationCoordinatorService;
	private Hashtable _completionCoordinators = new Hashtable();
    private UserSubordinateTransactionImple _userSubordinateTransaction;
}
