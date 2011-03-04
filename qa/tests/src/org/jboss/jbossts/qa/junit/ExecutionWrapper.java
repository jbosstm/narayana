/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.qa.junit;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jboss.jbossts.qa.Utils.EmptyObjectStore;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Wrapper which alters crash recovery configuration of the tests, such that they use a
 * uniq objectstore dir and in-process cr per process, rather than the previous behaviour
 * of having a shared dir and single cr mgr process per test.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-04
 */
public class ExecutionWrapper
{
    private static final File recMgrFile = new File(System.getProperty("java.io.tmpdir"), "recMgrLockFile");

    public static void main(String[] args) throws Exception
    {
        String className = args[0];
        String[] subArgs = new String[args.length-1];
        System.arraycopy(args, 1, subArgs, 0, args.length-1);

        if(className.equals("com.arjuna.ats.arjuna.recovery.RecoveryManager"))
        {
            if(!recMgrFile.createNewFile()) {
                System.err.println("Recovery manager already running?");
                System.exit(-1);
            }

            recMgrFile.deleteOnExit();
            System.out.println("Ready");
            Thread.sleep(Long.MAX_VALUE);
        }
        else if(className.equals("org.jboss.jbossts.qa.Utils.EmptyObjectStore"))
        {
            String objectStoreBaseDirBaseName = System.getProperty("ObjectStoreBaseDir");
            // strip off the trailing '/emptyObjectStore' to get the test rather than task dir
            objectStoreBaseDirBaseName = objectStoreBaseDirBaseName.substring(0, objectStoreBaseDirBaseName.lastIndexOf("/"));

            
            File directory = new File(objectStoreBaseDirBaseName);
            
            for(File candidateFile : directory.listFiles()) {
                if(candidateFile.isDirectory()) {
                    System.err.println("emptying "+candidateFile.getCanonicalPath());
                    EmptyObjectStore.removeContents(candidateFile);
                }
            }
            
            System.out.println("Passed");
        }
        else
        {
            int portOffset = Integer.valueOf(System.getProperty("portOffsetId"))*20;

            int recoveryOrbPortBase = jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort();
            int recoveryOrbPort = recoveryOrbPortBase+portOffset;
            jtsPropertyManager.getJTSEnvironmentBean().setRecoveryManagerPort(recoveryOrbPort);

            int recoveryManagerPortBase = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();
            int recoveryManagerPort = recoveryManagerPortBase+portOffset;
            recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryPort(recoveryManagerPort);

            System.out.println("using ports "+recoveryOrbPort+" and "+recoveryManagerPort);

            String objectStoreBaseDirBaseName = System.getProperty("ObjectStoreBaseDir");
            File directory = new File(objectStoreBaseDirBaseName); // full path incl taskName, see TestGroupBase

            File hornetqStoreDir = new File(directory, "HornetQStore");
            //additionalCommandLineElements.add("-DHornetqJournalEnvironmentBean.storeDir="+hornetqStoreDir);

            BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class)
                .setStoreDir(hornetqStoreDir.getCanonicalPath());
            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                    .setObjectStoreType("com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor");
/*

    [junit] Running org.jboss.jbossts.qa.junit.testgroup.TestGroup_txcore_recovery
    [junit] Tests run: 36, Failures: 24, Errors: 0, Time elapsed: 971.637 sec
    [junit] Test org.jboss.jbossts.qa.junit.testgroup.TestGroup_txcore_recovery FAILED
 */


            File ostoreDir = new File(directory, "ObjectStore");
            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                    .setObjectStoreDir(ostoreDir.getCanonicalPath());
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                    .setObjectStoreDir(ostoreDir.getCanonicalPath());
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                    .setObjectStoreDir(ostoreDir.getCanonicalPath());


            final Properties p = new Properties();
            p.setProperty("OAPort", ""+recoveryOrbPort);

            try {
                ORBInterface.initORB(args, p);
                // use correct call - OAInterface.initOA swallows exception!
                OAInterface.initializeOA();
            } catch(Exception e) {
                // probably recycling port allocation too fast. wait a bit and retry.
                ORBManager.getORB().shutdown();
                ORBManager.reset();
                Thread.sleep(2000);
                ORBInterface.initORB(args, p);
                OAInterface.initializeOA();
            }

            RecoveryManager manager = RecoveryManager.manager();

            Class clazz = Class.forName(className);

            Method mainMethod = clazz.getMethod("main", new Class[] {subArgs.getClass()});

            before();

            mainMethod.invoke(null, new Object[] {subArgs});

            after();

            manager.terminate();

            System.exit(0);
        }
    }

    private static void before()
    {
        //System.out.println("before");
    }

    private static void after()
    {
        //System.out.println("after");
        StoreManager.shutdown();
    }
}
