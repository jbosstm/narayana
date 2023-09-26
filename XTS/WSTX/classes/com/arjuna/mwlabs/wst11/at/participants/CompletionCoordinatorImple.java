/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.mwlabs.wst11.at.participants;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst11.CompletionCoordinatorParticipant;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

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
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException |
	       com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
	    ute.addSuppressed(ex);
	    throw ute;
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorCancelledException ex)
	{
	    TransactionRolledBackException tre = new TransactionRolledBackException();
	    tre.addSuppressed(ex);
	    throw tre;
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException |
	       com.arjuna.mw.wsas.exceptions.ProtocolViolationException |
	       com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException |
	       com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException |
	       com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    SystemException se = new SystemException(ex.toString());
	    se.addSuppressed(ex);
	    throw se;
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    SystemException se = new SystemException(ex.toString());
	    se.addSuppressed(ex);
	    throw se;
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
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException |
	        com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    UnknownTransactionException ute = new UnknownTransactionException();
	    ute.addSuppressed(ex);
	    throw ute;
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException |
	       com.arjuna.mw.wsas.exceptions.ProtocolViolationException |
	       com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorConfirmedException |
	       com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException |
	       com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException |
	       com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    SystemException se = new SystemException(ex.toString());
	    se.addSuppressed(ex);
	    throw se;
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    SystemException se = new SystemException(ex.toString());
	    se.addSuppressed(ex);
	    throw se;
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
