/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wst11.ba.remote;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;

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

public class SubordinateBAParticipantManagerImple implements BAParticipantManager
{

    public SubordinateBAParticipantManagerImple(SubordinateBACoordinator theTx, String participantId)
    {
        _theTx = theTx;
        _participantId = participantId;
    }

    public void exit () throws WrongStateException, UnknownTransactionException, SystemException
    {
        try {
            _theTx.delistParticipant(_participantId);
        } catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex) {
            throw new SystemException("UnknownParticipantException");
        } catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex) {
            throw new WrongStateException();
        } catch (com.arjuna.mw.wsas.exceptions.SystemException ex) {
            throw new SystemException(ex.toString());
        }
    }

    public void completed () throws WrongStateException, UnknownTransactionException, SystemException
    {
        try {
            _theTx.participantCompleted(_participantId);
        } catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex) {
            throw new SystemException("UnknownParticipantException");
        } catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex) {
            throw new WrongStateException();
        } catch (com.arjuna.mw.wsas.exceptions.SystemException ex) {
            throw new SystemException(ex.toString());
        }
    }

    public void fail (final QName exceptionIdentifier) throws SystemException
    {
        try {
            _theTx.participantFaulted(_participantId);
        } catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex) {
            throw new SystemException("UnknownParticipantException");
        } catch (com.arjuna.mw.wsas.exceptions.SystemException ex) {
            throw new SystemException(ex.toString());
        }
    }

    // TODO -- continue from here
    
    public void cannotComplete () throws WrongStateException, UnknownTransactionException, SystemException
    {
        try {
            _theTx.participantCannotComplete(_participantId);
        } catch (com.arjuna.mw.wscf.exceptions.InvalidParticipantException ex) {
            throw new SystemException("UnknownParticipantException");
        }  catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex) {
            throw new WrongStateException();
        }   catch (com.arjuna.mw.wsas.exceptions.SystemException ex) {
            throw new SystemException(ex.toString());
        }
    }

    private SubordinateBACoordinator _theTx;
    private String _participantId;
}