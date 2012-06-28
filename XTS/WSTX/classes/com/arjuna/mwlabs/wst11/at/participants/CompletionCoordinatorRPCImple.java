package com.arjuna.mwlabs.wst11.at.participants;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorRPCProcessor;
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
public class CompletionCoordinatorRPCImple implements CompletionCoordinatorParticipant
{
    public CompletionCoordinatorRPCImple(CoordinatorManager cm, ActivityHierarchy hier, final boolean deactivate, W3CEndpointReference participant)
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
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    //	    throw new HeuristicHazardException();

	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorCancelledException ex)
	{
	    throw new TransactionRolledBackException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    //	    throw new HeuristicMixedException();

	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
	{
	    //	    throw new HeuristicHazardException();

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
	finally
	{
	    if (deactivate)
	        CompletionCoordinatorRPCProcessor.getProcessor().deactivateParticipant(this);
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
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
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
	finally
	{
        if (deactivate)
            CompletionCoordinatorRPCProcessor.getProcessor().deactivateParticipant(this);
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
