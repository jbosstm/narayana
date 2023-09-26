/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
import org.omg.CORBA.ORBPackage.InvalidName;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
            objectStoreBaseDirBaseName = objectStoreBaseDirBaseName.substring(0, objectStoreBaseDirBaseName.lastIndexOf(System.getProperty("file.separator")));

            
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

            // for persistent servers the JavaIdl orb requires you to explicitly define which port the 
            // server will run on and to provide a unique id per server per machine:
            p.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""+recoveryOrbPort);
            p.setProperty("com.sun.CORBA.POA.ORBServerId", ""+recoveryOrbPort);

            initOrb(p, 10, recoveryOrbPort, args);

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

    /**
     * initialize the orb and OA
     * @param orbProps
     * @param retryCount when a socket is closed it transitions into the TIME_WAIT state so it is not always immediately
     *                   available the next time the orb is initialized. To avoid this problem of rapid recycling of
     *                   ports between tests we allow retries - note that there is a real time delay before
     *                   each retry attempt to give the socket close protocol sufficient time to complete.
     * @param recoveryOrbPort
     * @param args
     */
    private static void initOrb(Properties orbProps, int retryCount, int recoveryOrbPort, String... args) {
        int i = 0;

        while (!isAvailable("127.0.0.1", recoveryOrbPort)) {
            // probably recycling port allocation too fast. wait a bit and retry.
            if (i >= retryCount) {
                System.err.printf("orb port %d still in use after %d seconds%n", recoveryOrbPort, (retryCount * 10));
                fingerCulprit(recoveryOrbPort);
                throw new RuntimeException("Still cannot initialize ORB/OA on port " + recoveryOrbPort);
            }

            i += 1;
            System.err.printf("orb port %d is in use%n", recoveryOrbPort);

            try {
                Thread.sleep(10000);  // was 2000 * i  CrashRecovery02_2_Test26 seems to take quite a while
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            }
        }

        try {
            ORBInterface.initORB(args, orbProps);
            OAInterface.initializeOA(); // don't use initOA because it swallows exception!
        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();

            throw new RuntimeException("Cannot initialize ORB/OA on port - reason: " + invalidName.getMessage());
        }
    }

    private static boolean isAvailable(String host, int port) {
        try {
            (new Socket(host, port)).close();

            // Successful connection means the port is taken.
            return false;
        } catch (IOException e) {
            // Could not connect so it must be available
            return true;
        }
    }

    /**
     * Find out which process has a particular port open and print its pid, its command line and,
     * if its a JVM its stack traces.
     *
     * Only works on platforms that have the lsof command, the proc fs and the jstack command
     *
     * @param port port number to debug
     * @throws Exception
     */
    public static void fingerCulprit(int port) {
        // not supported on windows platforms
        if (System.getProperty("os.name", "Linux").contains("indows"))
            return;

        try {
            // who has tcp port 4731 open
            String pid = printLines(startProcess("lsof", "-t", "-i", "tcp:" + port), 1);
            System.out.printf("pid: %s%n", pid);
            // find its cmd line
            StringBuilder sb = new StringBuilder("/proc/").append(pid).append("/cmdline");
            String cmdline = printLines(startProcess("cat", sb.toString()), 1);
            System.out.printf("cmdline: %s%n", cmdline);
            // if its a JVM get its stack traces
            System.out.printf("jstack for JVM %s%n", pid);
            printLines(startProcess("jstack", pid), -1);
            System.out.printf("end of jstack for JVM %s%n", pid);
        } catch (Exception e) {
            System.out.printf("Exception %s whilst checking who has port %d open%n", e.getMessage(), port);
        }

    }

    private static String printLines(Process p, int howMany) {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s = null;

        try {
            if (howMany == -1) {
                while ((s = stdInput.readLine()) != null)
                    System.out.println(s);
            } else {
                for (int i = 0; i < howMany; i++)
                    s = stdInput.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stdInput.close();
            } catch (IOException e) {
            }
            p.destroy();
        }

        return s;
    }

    private static Process startProcess(String ... args) throws Exception {
        List<String> pArgs = new ArrayList<String>();

        for (String arg : args)
            pArgs.add(arg);

        Process p = new ProcessBuilder(args).start();
        p.waitFor();

        return p;
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