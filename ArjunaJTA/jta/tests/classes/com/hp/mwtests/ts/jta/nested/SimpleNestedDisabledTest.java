/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.nested;

import static org.junit.Assert.fail;

import jakarta.transaction.NotSupportedException;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;

public class SimpleNestedDisabledTest
{
    @Test
    public void testDisabled () throws Exception
    {
        jtaPropertyManager.getJTAEnvironmentBean().setSupportSubtransactions(false);

        jakarta.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();

        try
        {
            transactionManager.begin();

            fail();
        }
        catch (final NotSupportedException ex)
        {
        }

        transactionManager.commit();
    }
}