/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jtax.tests.implicit.client;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.junit.Test;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

public class ImplicitClient
{
    @Test
    public void test() throws Exception
    {
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        ORB orb = ORB.getInstance("implicitserver-orb");
        OA oa = OA.getRootOA(orb);

        orb.initORB(new String[] {}, null);
        oa.initPOA(new String[] {});

        File file = new File("server.ior");
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String ior = br.readLine();
        org.omg.CORBA.Object obj = orb.orb().string_to_object(ior); // TODO orb.orb().string_to_object(getService("TODO"));

        Example.test test = Example.testHelper.narrow(obj);

        TransactionManager.transactionManager().begin();

        test.invoke();

        TransactionManager.transactionManager().commit();

        oa.destroy();
        orb.destroy();
    }
}