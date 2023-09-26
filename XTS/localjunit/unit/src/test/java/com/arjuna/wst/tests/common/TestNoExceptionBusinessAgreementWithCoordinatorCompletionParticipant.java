/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst.tests.common;

import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.Status;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;

public class TestNoExceptionBusinessAgreementWithCoordinatorCompletionParticipant implements BusinessAgreementWithCoordinatorCompletionParticipant
{

    public void close () throws WrongStateException, SystemException
    {
    }
    
    public void cancel () throws WrongStateException, SystemException
    {
    }

    public void compensate () throws WrongStateException, SystemException
    {
    }

    public String status () throws SystemException
    {
	return Status.STATUS_ACTIVE;
    }
    
    public void forget () throws WrongStateException, SystemException
    {
    }

    public void complete () throws WrongStateException, SystemException
    {
    }

    @Deprecated
    public void unknown () throws SystemException
    {
    }

    public void error () throws SystemException
    {
    }

}