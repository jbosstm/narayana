package com.arjuna.mwlabs.wst11.at.remote;

import com.arjuna.mw.wsc11.context.Context;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.at.remote.ContextManager;
import com.arjuna.mwlabs.wst11.at.ContextImple;
import com.arjuna.mwlabs.wst11.at.context.TxContextImple;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc.CannotRegisterException;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc11.ActivationCoordinator;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.stub.CompletionRPCStub;
import com.arjuna.wst11.stub.CompletionStub;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.util.Hashtable;

/**
 * An implementation of UserTransaction which employs an RPC MEP based completion protocol specific to
 * JBoss to complete the transaction. This implementation allows the client to be deployed without the
 * need to expose any service endpoints.
 */
public class UserTransactionStandaloneImple extends UserTransaction
{

	public UserTransactionStandaloneImple()
	{
		try
		{
            _activationCoordinatorService = XTSPropertyManager.getWSCEnvironmentBean().getCoordinatorURL11();

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
        // this implementation cannot provide support for subordinate transactions
        _userSubordinateTransaction = null;
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
     * @throws com.arjuna.wst.WrongStateException
     * @throws com.arjuna.wst.SystemException
     */
    public void beginSubordinate(int timeout) throws WrongStateException, SystemException
    {
        throw new SystemException("UserTransactionStandaloneImple does not support subordinate transactions");
    }

	/*
	 * enlist the client for the completiopn protocol so it can commit or ro0ll back the transaction
	 */

	private final void enlistCompletionParticipants ()
			throws WrongStateException, UnknownTransactionException,
			SystemException
	{
        TransactionManagerImple tm = (TransactionManagerImple) TransactionManager.getTransactionManager();

        final TxContextImple currentTx = (TxContextImple) tm.currentTransaction();
        if (currentTx == null)
            throw new UnknownTransactionException();

        final String id = currentTx.identifier();
        W3CEndpointReference completionCoordinator = null;

        try
        {
            completionCoordinator = tm.registerParticipant(null, AtomicTransactionConstants.WSAT_SUB_PROTOCOL_COMPLETION_RPC);
        }
        catch (InvalidProtocolException ex)
        {
            ex.printStackTrace();

            throw new SystemException(ex.toString());
        }
        catch (InvalidStateException ex)
        {
            throw new WrongStateException();
        }
        catch (CannotRegisterException ex)
        {
            // cause could actually be no activity or already registered
            throw new UnknownTransactionException();
        }

        _completionCoordinators.put(id, completionCoordinator);
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
			throws InvalidCreateParametersException,
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
                        wstxLogger.i18NLogger.get_mwlabs_wst_at_remote_UserTransaction11Imple__2());
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

			CompletionRPCStub completionStub = new CompletionRPCStub(id, completionCoordinator);

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

			CompletionRPCStub completionStub = new CompletionRPCStub(id, completionCoordinator);

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
