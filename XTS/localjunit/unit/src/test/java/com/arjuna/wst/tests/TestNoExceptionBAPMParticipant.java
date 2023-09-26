/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst.tests;

import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.UnknownTransactionException;

import javax.xml.namespace.QName;

public class TestNoExceptionBAPMParticipant implements BAParticipantManager
{

    public void completed ()
        throws WrongStateException, SystemException
    {
    }

    /**
     * Cannot complete.
     */

    public void cannotComplete() throws WrongStateException, UnknownTransactionException, SystemException {
    }

    /**
     * Fault.
     */

    public void fail(QName exceptionIdentifier) throws SystemException {
    }

    public void exit ()
        throws WrongStateException, SystemException
    {
    }

    public void unknown ()
        throws SystemException
    {
    }

    public void error ()
        throws SystemException
    {
    }
}