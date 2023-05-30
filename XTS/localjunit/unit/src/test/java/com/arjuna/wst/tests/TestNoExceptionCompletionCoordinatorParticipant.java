/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.wst.tests;

import com.arjuna.wst11.CompletionCoordinatorParticipant;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class TestNoExceptionCompletionCoordinatorParticipant implements CompletionCoordinatorParticipant
{
    public TestNoExceptionCompletionCoordinatorParticipant(W3CEndpointReference endpointReference)
    {
        this.endpointReference = endpointReference;
    }

    public void commit()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
    }

    public void rollback()
        throws UnknownTransactionException, SystemException
    {
    }

    public W3CEndpointReference getParticipant() {
        return endpointReference;
    }

    private W3CEndpointReference endpointReference;
}