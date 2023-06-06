/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.wst.tests;

import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

public class TestTransactionRolledBackExceptionBusinessActivityTerminator implements BusinessActivityTerminator
{
    public TestTransactionRolledBackExceptionBusinessActivityTerminator(W3CEndpointReference endpointReference)
    {
        this.endpointReference = endpointReference;
    }

    public void close ()
        throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        throw new TransactionRolledBackException();
    }

    public void cancel ()
        throws UnknownTransactionException, SystemException
    {
        throw new UnknownTransactionException();
    }

    public void complete ()
        throws UnknownTransactionException, SystemException
    {
        throw new UnknownTransactionException();
    }

    public W3CEndpointReference getEndpoint() {
        return endpointReference;
    }

    private W3CEndpointReference endpointReference;
}