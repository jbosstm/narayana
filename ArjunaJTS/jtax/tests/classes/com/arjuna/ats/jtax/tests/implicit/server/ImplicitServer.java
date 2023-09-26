/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jtax.tests.implicit.server;



import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jtax.tests.implicit.impl.RemoteImpl;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

public class ImplicitServer
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

        RemoteImpl impl = new RemoteImpl();

        oa.objectIsReady(impl);

        org.omg.CORBA.Object obj = oa.corbaReference(impl);
        
        File file = new File("server.ior");
        file.delete();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(orb.orb().object_to_string(obj));
        fileWriter.close();
        
        // TODO registerService(args[0], orb.orb().object_to_string(obj));

        orb.orb().run();
    }
}