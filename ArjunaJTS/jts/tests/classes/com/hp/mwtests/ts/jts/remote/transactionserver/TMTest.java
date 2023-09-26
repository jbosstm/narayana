/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.transactionserver;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;

public class TMTest
{
    public static void main (String[] args) throws Exception
    {
        TMTest theTest = new TMTest();
        
        theTest.test();
    }
    
    @Test
    public void test() throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        TransactionFactory theOTS = null;
        Control topLevelControl = null;
        Services serv = new Services(myORB);

        int resolver = Services.getResolver();
        
        try
        {
            String[] params = new String[1];

            params[0] = Services.otsKind;

            org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params, resolver);

            params = null;
            theOTS = TransactionFactoryHelper.narrow(obj);
        }
        catch (Exception e)
        {
            fail("Unexpected bind exception: "+e);
            e.printStackTrace(System.err);
        }

        System.out.println("Creating transaction.");

        try
        {
            topLevelControl = theOTS.create(0);
        }
        catch (Exception e)
        {
            fail("Create call failed: "+e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();
    }
}