/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst.tests;

import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class TestSystemExceptionBusinessActivityTerminator implements BusinessActivityTerminator
{
    public TestSystemExceptionBusinessActivityTerminator(W3CEndpointReference endpointReference)
    {
        this.endpointReference = endpointReference;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public void cancel ()
        throws UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public void complete ()
        throws UnknownTransactionException, SystemException
    {
        throw new SystemException();
    }

    public W3CEndpointReference getEndpoint() {
        return endpointReference;
    }

    private W3CEndpointReference endpointReference;
}