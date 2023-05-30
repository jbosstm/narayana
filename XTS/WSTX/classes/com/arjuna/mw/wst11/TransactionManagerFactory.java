/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.mw.wst11;


public class TransactionManagerFactory
{
    public static TransactionManager transactionManager ()
    {
    	return TransactionManager.getTransactionManager() ;
    }
}