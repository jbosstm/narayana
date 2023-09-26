/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.ba.remote;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.InvalidActivityException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;

import javax.xml.namespace.QName;

/**
 * This is the interface that the core exposes in order to allow different
 * types of participants to be enrolled. The messaging layer continues to
 * work in terms of the registrar, but internally we map to one of these
 * methods.
 *
 * This could also be the interface that high-level users see (e.g., at the
 * application Web Service).
 */

public class BAParticipantManagerImple implements BAParticipantManager
{

    public BAParticipantManagerImple(String participantId)
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + " constructor. Participant id: " + participantId);
        }

	try
	{
	    _coordManager = CoordinatorManagerFactory.coordinatorManager();
	    _hier = _coordManager.currentActivity();
	    _participantId = participantId;
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    public void exit () throws WrongStateException, UnknownTransactionException, SystemException
    {
	try
	{
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + ".exit. Participant id: " + _participantId);
        }

	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);

	    _coordManager.delistParticipant(_participantId);

	    _coordManager.suspend();
	}
	catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
	{
	    throw new SystemException("UnknownParticipantException");
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new WrongStateException();
	}
	catch (InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    public void completed () throws WrongStateException, UnknownTransactionException, SystemException
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + ".completed. Participant id: " + _participantId);
        }

	try
	{
	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);

	    _coordManager.participantCompleted(_participantId);

	    _coordManager.suspend();
	}
    	catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
	{
	    throw new SystemException("UnknownParticipantException");
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new WrongStateException();
	}
	catch (InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    public void fail (final QName exceptionIdentifier) throws SystemException
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + ".fail. Participant id: " + _participantId
                    + ", exceptionIdentifier: " + exceptionIdentifier);
        }

	try
	{
        if (_hier == null)
            throw new UnknownTransactionException();

            _coordManager.resume(_hier);

        // fail means faulted as far as the coordinator manager is concerned
            _coordManager.participantFaulted(_participantId);

            _coordManager.suspend();
	}
    catch (final InvalidActivityException iae)
    {
        throw new SystemException("UnknownTransactionException");
    }
    catch (final UnknownTransactionException ute)
    {
        throw new SystemException("UnknownTransactionException");
    }
	catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
	{
	    throw new SystemException("UnknownParticipantException");
	}
    	catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
	{
	    throw new SystemException("UnknownTransactionException");
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    public void cannotComplete () throws WrongStateException, UnknownTransactionException, SystemException
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + ".cannotComplete. Participant id: " + _participantId);
        }

        try
        {
            if (_hier == null)
                throw new UnknownTransactionException();

                _coordManager.resume(_hier);

                _coordManager.participantCannotComplete(_participantId);

                _coordManager.suspend();
        }
        catch (final InvalidActivityException iae)
        {
            throw new SystemException("UnknownTransactionException");
        }
        catch (final UnknownTransactionException ute)
        {
            throw new SystemException("UnknownTransactionException");
        }
        catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex)
        {
            throw new SystemException("UnknownParticipantException");
        }
        catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
        {
            throw new WrongStateException();
        }
            catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
        {
            throw new SystemException("UnknownTransactionException");
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
    }

    public void error () throws SystemException
    {
        if (wstxLogger.logger.isTraceEnabled()) {
            wstxLogger.logger.trace(getClass().getSimpleName() + ".error. Participant id: " + _participantId);
        }

	try
	{
	    _coordManager.setCancelOnly();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    private CoordinatorManager _coordManager = null;
    private ActivityHierarchy  _hier = null;
    private String             _participantId = null;

}