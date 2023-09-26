/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.basic;

import jakarta.transaction.UserTransaction;

import org.junit.Test;

public class UserTxUnitTest
{
    @Test
    public void test() throws Exception
    {
        UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();
        
        ut.begin();
        
        ut.commit();
    }
}