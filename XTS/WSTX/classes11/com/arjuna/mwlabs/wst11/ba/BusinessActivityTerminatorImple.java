package com.arjuna.mwlabs.wst11.ba;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * Local business activity terminator which can be used to close a business activity from the terminator
 * side.
 */
public class BusinessActivityTerminatorImple
        implements BusinessActivityTerminator
{
    private W3CEndpointReference participantEndpoint;

    /**
     * create a business activity terminator for use on the server side with an empty participant endpoint
     * which gets set at registration time
     * @throws SystemException
     */
    public BusinessActivityTerminatorImple()
            throws SystemException
    {
        try
        {
            _coordManager = CoordinatorManagerFactory.coordinatorManager();
            _hier = _coordManager.currentActivity();
        }
        catch (ProtocolNotRegisteredException pnre)
        {
            throw new SystemException(pnre.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }

        participantEndpoint = null;
    }

    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        try
        {
            if (_hier == null)
            throw new UnknownTransactionException();

            _coordManager.resume(_hier);

            _coordManager.close();
        }
        catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
        {
            throw new UnknownTransactionException();
        }
        catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException ex)
        {
            throw new TransactionRolledBackException();
        }
        catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
        {
            throw new UnknownTransactionException();
        }
        catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (UnknownTransactionException ex)
        {
            throw ex;
        }
        finally
        {
            TerminationCoordinatorProcessor.getProcessor().deactivateParticipant(this) ;
        }
    }

    public void cancel () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);

	    _coordManager.cancel();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException();
	}
	catch (UnknownTransactionException ex)
	{
	    throw ex;
	}
	finally
	{
        TerminationCoordinatorProcessor.getProcessor().deactivateParticipant(this) ;
	}
    }

    /**
     * Complete doesn't mean go away, it just means that all work you need to
     * accomplish the commit/rollback has been received.
     */

    public void complete () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);

	    _coordManager.complete();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException();
	}
	catch (UnknownTransactionException ex)
	{
	    throw ex;
	}
    }

    /**
     * update the business activity terminator with an endpoint supplied at registration time
     *
     * @param participantEndpoint
     */
    public void setEndpoint(W3CEndpointReference participantEndpoint)
    {
        this.participantEndpoint = participantEndpoint;
    }

    /**
     * retrieve the participant endpoint associated with this business activity terminator
     * 
     * @return
     */
    public W3CEndpointReference getEndpoint()
    {
        return participantEndpoint;
    }

    private CoordinatorManager _coordManager = null;
    private ActivityHierarchy _hier = null;
}
