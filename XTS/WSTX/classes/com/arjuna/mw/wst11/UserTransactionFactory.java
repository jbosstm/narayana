/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wst11;


public class UserTransactionFactory
{
    public static UserTransaction userTransaction ()
    {
        return UserTransaction.getUserTransaction() ;
    }

    public static UserTransaction userSubordinateTransaction ()
    {
        return UserTransaction.getUserTransaction().getUserSubordinateTransaction() ;
    }
}