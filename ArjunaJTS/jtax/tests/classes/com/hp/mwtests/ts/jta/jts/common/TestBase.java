/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.common;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.orbportability.common.opPropertyManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.junit.BeforeClass;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestBase
{
    @BeforeClass
    public static void setUp () throws Exception
    {
        final Map<String, String> orbInitializationProperties = new HashMap<String, String>();
        orbInitializationProperties.put("com.arjuna.orbportability.orb.PreInit1",
                "com.arjuna.ats.internal.jts.recovery.RecoveryInit");
        opPropertyManager.getOrbPortabilityEnvironmentBean()
                .setOrbInitializationProperties(orbInitializationProperties);

        final Properties initORBProperties = new Properties();
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBServerId", "1");
        initORBProperties.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""
                + jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort());

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);
        myORB.initORB(new String[] {}, initORBProperties);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());


        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryListener(true);

        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(2);
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(1);

        final List<String> recoveryActivatorClassNames = new ArrayList<String>();
        recoveryActivatorClassNames.add(RecoveryEnablement.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryActivatorClassNames(recoveryActivatorClassNames);

        recoveryManager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        recoveryManager.initialize();
    }
    
    @AfterClass
    public static void tearDown () throws Exception
    {
        recoveryManager.terminate();
        myOA.destroy();
        myORB.shutdown(true);
    }

    public void emptyObjectStore()
    {
        String objectStoreDirName = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        System.out.println("Emptying " + objectStoreDirName);

        File objectStoreDir = new File(objectStoreDirName);

        removeContents(objectStoreDir);
    }

    public boolean removeContents(File directory)
    {
        boolean deleteParent = true;
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            if (contents != null) {
                boolean canDelete = true;
                for (File content : contents) {
                    if (content.isDirectory()) {
                        if (!content.getName().equals("RecoveryCoordinator")) {
                            canDelete = removeContents(content) && canDelete;

                            if (!canDelete) {
                                deleteParent = false;
                            } else {
                                content.delete();
                            }
                        } else {
                            deleteParent = false;
                        }
                    } else {
                        content.delete();
                    }
                }
            }
        }
        return deleteParent;
    }

    private static ORB myORB = null;
    private static RootOA myOA = null;
    protected static RecoveryManager recoveryManager;
}