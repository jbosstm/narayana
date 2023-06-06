/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.wst.tests.common;

import com.arjuna.wst.Participant;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

public class TestReadOnlyVoteParticipant implements Participant
{
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        return new ReadOnly();
    }

    public void commit()
        throws WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void rollback()
        throws WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void commitOnePhase()
        throws TransactionRolledBackException, WrongStateException, SystemException
    {
        throw new SystemException();
    }

    public void unknown()
        throws SystemException
    {
        throw new SystemException();
    }

    public void error()
        throws SystemException
    {
        throw new SystemException();
    }
}