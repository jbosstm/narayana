/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst.tests;

import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

import javax.xml.namespace.QName;

public class TestWrongStateExceptionBAPMParticipant implements BAParticipantManager
{
    public void completed ()
        throws WrongStateException, SystemException
    {
        throw new WrongStateException();
    }

    public void exit ()
        throws WrongStateException, SystemException
    {
        throw new WrongStateException();
    }

    public void cannotComplete ()
        throws WrongStateException, SystemException
    {
        throw new WrongStateException();
    }

    public void unknown ()
        throws SystemException
    {
        throw new SystemException();
    }

    public void fail (QName exceptionIdentifier)
        throws SystemException
    {
        throw new SystemException();
    }

    public void error ()
        throws SystemException
    {
        throw new SystemException();
    }
}