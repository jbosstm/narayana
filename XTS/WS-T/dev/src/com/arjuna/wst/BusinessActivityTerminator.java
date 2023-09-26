/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wst;

/**
 * Not in the 1.1 specification. Supposed to use participant interface.
 */

public interface BusinessActivityTerminator
{

    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException;
    
    public void cancel () throws FaultedException, UnknownTransactionException, SystemException;

    public void complete () throws FaultedException, UnknownTransactionException, SystemException;
    
}