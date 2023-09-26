/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst.tests.common;

import com.arjuna.wst.Participant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

public class TestNoExceptionParticipant implements Participant
{
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void commit()
        throws WrongStateException, SystemException
    {
    }

    public void rollback()
        throws WrongStateException, SystemException
    {
    }

    public void commitOnePhase()
        throws TransactionRolledBackException, WrongStateException, SystemException
    {
    }

    public void unknown()
        throws SystemException
    {
    }

    public void error()
        throws SystemException
    {
    }
}