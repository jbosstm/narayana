package com.arjuna.mwlabs.wst11.at.participants;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.SuppressedExceptionHelper;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.CompletionCoordinatorParticipant;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Oct 30, 2007
 * Time: 2:37:26 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class CompletionCoordinatorImple implements CompletionCoordinatorParticipant
{
    public CompletionCoordinatorImple(CoordinatorManager cm, ActivityHierarchy hier, final boolean deactivate, W3CEndpointReference participant)
    {
        _cm = cm;
        _hier = hier;
        this.deactivate = deactivate ;
        this.participant = participant;
    }

    public void commit () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);

	    _cm.confirm();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
	    throw SuppressedExceptionHelper.addSuppressedThrowable(ute, ex);
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
		throw SuppressedExceptionHelper.addSuppressedThrowable(ute, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorCancelledException ex)
	{
	    TransactionRolledBackException tre = new TransactionRolledBackException();
		throw SuppressedExceptionHelper.addSuppressedThrowable(tre, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	finally
	{
	    if (deactivate)
	        CompletionCoordinatorProcessor.getProcessor().deactivateParticipant(this);
	}
    }

    public void rollback () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);

	    _cm.cancel();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
		throw SuppressedExceptionHelper.addSuppressedThrowable(ute, ex);
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
		throw SuppressedExceptionHelper.addSuppressedThrowable(ute, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorConfirmedException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    SystemException se = new SystemException(ex.toString());
		throw SuppressedExceptionHelper.addSuppressedThrowable(se, ex);
	}
	finally
	{
        if (deactivate)
            CompletionCoordinatorProcessor.getProcessor().deactivateParticipant(this);
	}
    }

    public W3CEndpointReference getParticipant()
    {
        return participant;
    }

    private CoordinatorManager   _cm;
    private ActivityHierarchy    _hier;
    private final boolean deactivate ;
    private W3CEndpointReference participant;
}
