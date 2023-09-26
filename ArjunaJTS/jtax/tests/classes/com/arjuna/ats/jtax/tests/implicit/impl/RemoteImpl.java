/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jtax.tests.implicit.impl;



import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jtax.tests.resources.ExampleXAResource;

public class RemoteImpl extends Example.testPOA
{
    public void invoke()
    {
        try
        {
            TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            ExampleXAResource a = new ExampleXAResource();
            ExampleXAResource b = new ExampleXAResource();

            Transaction tx = tm.getTransaction();

            System.out.println("CurrentTx : "+tx+" >> "+OTSImpleManager.current().getControlWrapper().isLocal());

            tx.enlistResource(a);
            tx.delistResource(a,javax.transaction.xa.XAResource.TMSUCCESS);

            tx.enlistResource(b);
            tx.delistResource(b,javax.transaction.xa.XAResource.TMSUCCESS);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}