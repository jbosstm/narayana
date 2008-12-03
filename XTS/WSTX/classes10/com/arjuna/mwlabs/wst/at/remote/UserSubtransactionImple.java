package com.arjuna.mwlabs.wst.at.remote;

import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wsc.context.Context;
import com.arjuna.mwlabs.wst.at.ContextImple;
import com.arjuna.mwlabs.wst.at.context.TxContextImple;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ActivationCoordinator;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.messaging.MessageId;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;

/**
 * @message com.arjuna.mwlabs.wst.at.remote.UserSubTransactionImple_1
 *          [com.arjuna.mwlabs.wst.at.remote.UserSubTransactionImple_1] - Received
 *          context is null!
 */
public class UserSubtransactionImple extends UserTransactionImple
{

	public UserSubtransactionImple()
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



            final CoordinationContextType current = currentCoordinationContext();
            final Long expires = (timeout > 0 ? new Long(timeout) : null) ;
            final String messageId = MessageId.getMessageId() ;
            final CoordinationContextType coordinationContext = ActivationCoordinator.createCoordinationContext(
                    _activationCoordinatorService, messageId, AtomicTransactionConstants.WSAT_PROTOCOL, expires, current) ;
            if (coordinationContext == null)
            {
                throw new SystemException(
                    wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.at.remote.UserSubTransactionImple_1"));
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
    public CoordinationContextType currentCoordinationContext() throws SystemException
    {
        final TxContextImple txContext = (TxContextImple)_ctxManager.currentTransaction();
        final CoordinationContextType current = txContext.context().getCoordinationContext();
        return current;
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